package qupath.ext.draggablelabels;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.tools.MenuTools;
import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QuPath extension to enable draggable annotation labels
 */
public class DraggableLabelsExtension implements QuPathExtension {
    
    private static final Logger logger = LoggerFactory.getLogger(DraggableLabelsExtension.class);
    
    @Override
    public void installExtension(QuPathGUI qupath) {
        // Create menu items
        MenuItem enableItem = new MenuItem("Enable draggable labels");
        enableItem.setOnAction(e -> DraggableLabelsManager.enableDraggableLabels(qupath));
        
        MenuItem disableItem = new MenuItem("Disable draggable labels");
        disableItem.setOnAction(e -> DraggableLabelsManager.disableDraggableLabels(qupath));
        
        MenuItem resetItem = new MenuItem("Reset all label positions");
        resetItem.setOnAction(e -> DraggableLabelsManager.resetAllLabelPositions(qupath));
        
        // Add to menu
        MenuTools.addMenuItems(
            qupath.getMenu("Extensions", false),
            MenuTools.createMenu("Draggable Labels",
                enableItem,
                disableItem,
                null, // separator
                resetItem
            )
        );
        
        logger.info("Draggable Labels extension installed");
    }
    
    @Override
    public String getName() {
        return "Draggable Labels";
    }
    
    @Override
    public String getDescription() {
        return "Enable dragging of annotation labels to reposition them";
    }
    
    @Override
    public String getQuPathVersion() {
        return "0.6.0";
    }
}