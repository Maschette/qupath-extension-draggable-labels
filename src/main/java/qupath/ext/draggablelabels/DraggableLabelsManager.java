package qupath.ext.draggablelabels;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import javafx.geometry.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager class for handling draggable label functionality
 */
public class DraggableLabelsManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DraggableLabelsManager.class);
    private static final Map<QuPathViewer, DraggableLabelHandler> activeHandlers = new ConcurrentHashMap<>();
    
    public static void enableDraggableLabels(QuPathGUI qupath) {
        QuPathViewer viewer = qupath.getViewer();
        if (viewer == null) {
            logger.warn("No active viewer found");
            return;
        }
        
        if (activeHandlers.containsKey(viewer)) {
            logger.info("Draggable labels already enabled for this viewer");
            return;
        }
        
        DraggableLabelHandler handler = new DraggableLabelHandler(viewer);
        handler.enable();
        activeHandlers.put(viewer, handler);
        
        // Add overlay for custom label rendering
        DraggableLabelOverlay overlay = new DraggableLabelOverlay();
        viewer.getOverlayOptions().addOverlay(overlay);
        handler.setOverlay(overlay);
        
        logger.info("Draggable labels enabled");
    }
    
    public static void disableDraggableLabels(QuPathGUI qupath) {
        QuPathViewer viewer = qupath.getViewer();
        if (viewer == null) {
            logger.warn("No active viewer found");
            return;
        }
        
        DraggableLabelHandler handler = activeHandlers.remove(viewer);
        if (handler != null) {
            handler.disable();
            logger.info("Draggable labels disabled");
        } else {
            logger.info("Draggable labels were not enabled for this viewer");
        }
    }
    
    public static void resetAllLabelPositions(QuPathGUI qupath) {
        QuPathViewer viewer = qupath.getViewer();
        if (viewer == null || viewer.getImageData() == null) {
            logger.warn("No active viewer or image data found");
            return;
        }
        
        DraggableLabelHandler handler = activeHandlers.get(viewer);
        if (handler != null) {
            handler.resetAllPositions();
            logger.info("All label positions reset");
        } else {
            logger.warn("Draggable labels not enabled - cannot reset positions");
        }
    }
}