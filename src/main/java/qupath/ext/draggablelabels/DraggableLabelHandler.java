package qupath.ext.draggablelabels;

import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.objects.PathObject;
import qupath.lib.roi.interfaces.ROI;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * Handles mouse events for dragging annotation labels
 */
public class DraggableLabelHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(DraggableLabelHandler.class);
    private static final String OFFSET_X_KEY = "labelOffsetX";
    private static final String OFFSET_Y_KEY = "labelOffsetY";
    
    private final QuPathViewer viewer;
    private final Map<PathObject, Point2D> labelOffsets = new HashMap<>();
    
    // Drag state
    private PathObject draggingAnnotation = null;
    private Point2D dragStartPoint = null;
    private boolean isDragging = false;
    
    // Event handlers
    private EventHandler<MouseEvent> mousePressedHandler;
    private EventHandler<MouseEvent> mouseDraggedHandler;
    private EventHandler<MouseEvent> mouseReleasedHandler;
    
    private DraggableLabelOverlay overlay;
    
    public DraggableLabelHandler(QuPathViewer viewer) {
        this.viewer = viewer;
        initializeEventHandlers();
        loadOffsetsFromMetadata();
    }
    
    public void setOverlay(DraggableLabelOverlay overlay) {
        this.overlay = overlay;
        overlay.setLabelOffsets(labelOffsets);
    }
    
    private void initializeEventHandlers() {
        mousePressedHandler = this::handleMousePressed;
        mouseDraggedHandler = this::handleMouseDragged;
        mouseReleasedHandler = this::handleMouseReleased;
    }
    
    public void enable() {
        Node view = viewer.getView();
        view.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        view.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        view.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
    }
    
    public void disable() {
        Node view = viewer.getView();
        view.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        view.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        view.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        
        // Remove overlay
        if (overlay != null) {
            viewer.getOverlayOptions().removeOverlay(overlay);
        }
        
        // Reset cursor if dragging
        if (isDragging) {
            view.getScene().setCursor(Cursor.DEFAULT);
            isDragging = false;
            draggingAnnotation = null;
        }
    }
    
    private void handleMousePressed(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        
        Point2D canvasPoint = new Point2D(e.getX(), e.getY());
        PathObject annotation = getAnnotationAtLabelPoint(canvasPoint);
        
        if (annotation != null) {
            draggingAnnotation = annotation;
            dragStartPoint = canvasPoint;
            isDragging = true;
            
            viewer.getView().getScene().setCursor(Cursor.MOVE);
            e.consume();
            
            logger.debug("Started dragging label for: {}", annotation.getDisplayedName());
        }
    }
    
    private void handleMouseDragged(MouseEvent e) {
        if (!isDragging || draggingAnnotation == null) return;
        
        Point2D currentPoint = new Point2D(e.getX(), e.getY());
        Point2D delta = currentPoint.subtract(dragStartPoint);
        
        // Convert delta to image coordinates to maintain consistency across zoom levels
        double scale = viewer.getDownsampleFactor();
        Point2D imageOffset = new Point2D(delta.getX() * scale, delta.getY() * scale);
        
        // Update label offset
        Point2D currentOffset = getLabelOffset(draggingAnnotation);
        Point2D newOffset = currentOffset.add(imageOffset);
        setLabelOffset(draggingAnnotation, newOffset);
        
        // Update drag start point
        dragStartPoint = currentPoint;
        
        // Trigger repaint
        viewer.repaint();
        e.consume();
    }
    
    private void handleMouseReleased(MouseEvent e) {
        if (isDragging) {
            logger.debug("Finished dragging label for: {}", 
                draggingAnnotation != null ? draggingAnnotation.getDisplayedName() : "null");
            
            isDragging = false;
            draggingAnnotation = null;
            dragStartPoint = null;
            
            viewer.getView().getScene().setCursor(Cursor.DEFAULT);
            viewer.repaint();
            e.consume();
        }
    }
    
    private PathObject getAnnotationAtLabelPoint(Point2D canvasPoint) {
        if (viewer.getImageData() == null) return null;
        
        Collection<PathObject> annotations = viewer.getImageData().getHierarchy().getAnnotationObjects();
        
        for (PathObject annotation : annotations) {
            if (annotation.getDisplayedName() != null && 
                !annotation.getDisplayedName().isEmpty() && 
                isPointOnAnnotationLabel(annotation, canvasPoint)) {
                return annotation;
            }
        }
        return null;
    }
    
    private boolean isPointOnAnnotationLabel(PathObject annotation, Point2D canvasPoint) {
        ROI roi = annotation.getROI();
        if (roi == null) return false;
        
        // Get annotation center in image coordinates
        double centerX = roi.getCentroidX();
        double centerY = roi.getCentroidY();
        
        // Apply label offset
        Point2D offset = getLabelOffset(annotation);
        centerX += offset.getX();
        centerY += offset.getY();
        
        // Convert to canvas coordinates
        Point2D canvasCenter = viewer.imagePointToComponentPoint(centerX, centerY, null, false);
        if (canvasCenter == null) return false;
        
        // Estimate text bounds (rough approximation)
        String displayName = annotation.getDisplayedName();
        double textWidth = displayName.length() * 8.0; // Rough estimate
        double textHeight = 12.0;
        
        // Check if click point is within label bounds
        return canvasPoint.getX() >= canvasCenter.getX() - textWidth/2 &&
               canvasPoint.getX() <= canvasCenter.getX() + textWidth/2 &&
               canvasPoint.getY() >= canvasCenter.getY() - textHeight/2 &&
               canvasPoint.getY() <= canvasCenter.getY() + textHeight/2;
    }
    
    public Point2D getLabelOffset(PathObject annotation) {
        return labelOffsets.getOrDefault(annotation, new Point2D(0, 0));
    }
    
    public void setLabelOffset(PathObject annotation, Point2D offset) {
        labelOffsets.put(annotation, offset);
        
        // Store in annotation metadata for persistence
        annotation.getMetadata().put(OFFSET_X_KEY, offset.getX());
        annotation.getMetadata().put(OFFSET_Y_KEY, offset.getY());
        
        // Update overlay
        if (overlay != null) {
            overlay.setLabelOffsets(labelOffsets);
        }
    }
    
    private void loadOffsetsFromMetadata() {
        if (viewer.getImageData() == null) return;
        
        Collection<PathObject> annotations = viewer.getImageData().getHierarchy().getAnnotationObjects();
        for (PathObject annotation : annotations) {
            Map<String, Object> metadata = annotation.getMetadata();
            if (metadata.containsKey(OFFSET_X_KEY) && metadata.containsKey(OFFSET_Y_KEY)) {
                try {
                    double x = ((Number) metadata.get(OFFSET_X_KEY)).doubleValue();
                    double y = ((Number) metadata.get(OFFSET_Y_KEY)).doubleValue();
                    labelOffsets.put(annotation, new Point2D(x, y));
                } catch (Exception e) {
                    logger.warn("Failed to load label offset for annotation: {}", e.getMessage());
                }
            }
        }
    }
    
    public void resetAllPositions() {
        if (viewer.getImageData() == null) return;
        
        Collection<PathObject> annotations = viewer.getImageData().getHierarchy().getAnnotationObjects();
        for (PathObject annotation : annotations) {
            annotation.getMetadata().remove(OFFSET_X_KEY);
            annotation.getMetadata().remove(OFFSET_Y_KEY);
        }
        
        labelOffsets.clear();
        if (overlay != null) {
            overlay.setLabelOffsets(labelOffsets);
        }
        viewer.repaint();
    }
    
    public boolean isDragging(PathObject annotation) {
        return isDragging && draggingAnnotation == annotation;
    }
}