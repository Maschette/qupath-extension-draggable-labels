package qupath.ext.draggablelabels;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Map;

import javafx.scene.input.MouseEvent;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.objects.PathObject;

/**
 * Handles mouse interactions for dragging annotation labels.
 */
public class DraggableLabelHandler {

    private static final String OFFSET_X_KEY = "draggable_label_offset_x";
    private static final String OFFSET_Y_KEY = "draggable_label_offset_y";

    private QuPathViewer viewer;
    private DraggableLabelOverlay overlay;
    private PathObject draggedObject;
    private Point2D dragStartPoint;
    private Point2D initialOffset;
    private boolean isDragging = false;

    public DraggableLabelHandler(QuPathViewer viewer) {
        this.viewer = viewer;
    }

    public void setOverlay(DraggableLabelOverlay overlay) {
        this.overlay = overlay;
    }

    public void enable() {
        if (overlay != null) {
            viewer.getCustomOverlayLayers().add(overlay);
            viewer.getView().addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
            viewer.getView().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
            viewer.getView().addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
        }
    }

    public void disable() {
        if (overlay != null) {
            viewer.getCustomOverlayLayers().remove(overlay);
            viewer.getView().removeEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
            viewer.getView().removeEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
            viewer.getView().removeEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
        }
    }

    private void handleMousePressed(MouseEvent event) {
        if (viewer.getImageData() == null) {
            return;
        }

        // Convert mouse coordinates to image coordinates
        Point2D imagePoint = viewer.componentPointToImagePoint(event.getX(), event.getY(), null, false);
        
        // Find the closest annotation
        PathObject closestObject = findClosestAnnotation(imagePoint);
        
        if (closestObject != null && isNearLabel(closestObject, imagePoint)) {
            draggedObject = closestObject;
            dragStartPoint = new Point2D.Double(event.getX(), event.getY());
            initialOffset = getLabelOffset(closestObject);
            isDragging = true;
            event.consume();
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (isDragging && draggedObject != null && dragStartPoint != null) {
            double deltaX = event.getX() - dragStartPoint.getX();
            double deltaY = event.getY() - dragStartPoint.getY();
            
            Point2D newOffset = new Point2D.Double(
                initialOffset.getX() + deltaX,
                initialOffset.getY() + deltaY
            );
            
            setLabelOffset(draggedObject, newOffset);
            viewer.repaint();
            event.consume();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (isDragging) {
            isDragging = false;
            draggedObject = null;
            dragStartPoint = null;
            initialOffset = null;
            event.consume();
        }
    }

    private PathObject findClosestAnnotation(Point2D imagePoint) {
        if (viewer.getImageData() == null) {
            return null;
        }

        return viewer.getImageData().getHierarchy().getObjects(null, PathObject.class)
                .stream()
                .filter(obj -> obj.hasROI() && obj.getDisplayedName() != null && !obj.getDisplayedName().isEmpty())
                .filter(obj -> obj.getROI().contains(imagePoint.getX(), imagePoint.getY()))
                .findFirst()
                .orElse(null);
    }

    private boolean isNearLabel(PathObject pathObject, Point2D imagePoint) {
        var roi = pathObject.getROI();
        double centerX = roi.getCentroidX();
        double centerY = roi.getCentroidY();
        
        // Convert center to component coordinates using viewer's transformation
        Point2D imageCenter = new Point2D.Double(centerX, centerY);
        Point2D canvasCenter = viewer.componentPointToImagePoint(imageCenter.getX(), imageCenter.getY(), null, true);
        
        Point2D offset = getLabelOffset(pathObject);
        double labelX = canvasCenter.getX() + offset.getX();
        double labelY = canvasCenter.getY() + offset.getY();
        
        // Convert image point to component coordinates for comparison
        Point2D componentPoint = viewer.componentPointToImagePoint(imagePoint.getX(), imagePoint.getY(), null, true);
        
        // Check if within reasonable distance of label (e.g., 20 pixels)
        double distance = Math.sqrt(
            Math.pow(componentPoint.getX() - labelX, 2) + 
            Math.pow(componentPoint.getY() - labelY, 2)
        );
        
        return distance < 20; // 20 pixel tolerance
    }

    public Point2D getLabelOffset(PathObject pathObject) {
        Map<String, String> metadata = pathObject.getMetadata();
        
        String offsetXStr = metadata.get(OFFSET_X_KEY);
        String offsetYStr = metadata.get(OFFSET_Y_KEY);
        
        double offsetX = offsetXStr != null ? Double.parseDouble(offsetXStr) : 0.0;
        double offsetY = offsetYStr != null ? Double.parseDouble(offsetYStr) : 0.0;
        
        return new Point2D.Double(offsetX, offsetY);
    }

    public void setLabelOffset(PathObject pathObject, Point2D offset) {
        pathObject.getMetadata().put(OFFSET_X_KEY, String.valueOf(offset.getX()));
        pathObject.getMetadata().put(OFFSET_Y_KEY, String.valueOf(offset.getY()));
    }

    public void resetAllLabelPositions() {
        if (viewer.getImageData() == null) {
            return;
        }

        viewer.getImageData().getHierarchy().getObjects(null, PathObject.class)
                .stream()
                .filter(obj -> obj.hasROI() && obj.getDisplayedName() != null)
                .forEach(obj -> {
                    obj.getMetadata().remove(OFFSET_X_KEY);
                    obj.getMetadata().remove(OFFSET_Y_KEY);
                });
        
        viewer.repaint();
    }
}