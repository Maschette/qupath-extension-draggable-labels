package qupath.ext.draggablelabels;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;

/**
 * Manager class for the draggable labels functionality.
 */
public class DraggableLabelsManager {

    private QuPathGUI qupath;
    private DraggableLabelHandler handler;
    private DraggableLabelOverlay overlay;
    private boolean isEnabled = false;

    public DraggableLabelsManager(QuPathGUI qupath) {
        this.qupath = qupath;
    }

    public void installMenuItems() {
        Menu extensionsMenu = qupath.getMenu("Extensions", true);
        Menu draggableLabelsMenu = new Menu("Draggable Labels");
        
        MenuItem enableItem = new MenuItem("Enable draggable labels");
        enableItem.setOnAction(e -> enableDraggableLabels());
        
        MenuItem disableItem = new MenuItem("Disable draggable labels");
        disableItem.setOnAction(e -> disableDraggableLabels());
        
        MenuItem resetItem = new MenuItem("Reset all label positions");
        resetItem.setOnAction(e -> resetAllLabelPositions());
        
        draggableLabelsMenu.getItems().addAll(enableItem, disableItem, resetItem);
        extensionsMenu.getItems().add(draggableLabelsMenu);
    }

    private void enableDraggableLabels() {
        QuPathViewer viewer = qupath.getViewer();
        if (viewer == null) {
            return;
        }

        if (!isEnabled) {
            overlay = new DraggableLabelOverlay(viewer.getOverlayOptions());
            handler = new DraggableLabelHandler(viewer);
            
            overlay.setHandler(handler);
            handler.setOverlay(overlay);
            
            viewer.getCustomOverlayLayers().add(overlay);
            handler.enable();
            
            isEnabled = true;
            viewer.repaint();
        }
    }

    private void disableDraggableLabels() {
        if (isEnabled && handler != null) {
            handler.disable();
            QuPathViewer viewer = qupath.getViewer();
            if (viewer != null && overlay != null) {
                viewer.getCustomOverlayLayers().remove(overlay);
                viewer.repaint();
            }
            isEnabled = false;
            handler = null;
            overlay = null;
        }
    }

    private void resetAllLabelPositions() {
        if (handler != null) {
            handler.resetAllLabelPositions();
        }
    }
}