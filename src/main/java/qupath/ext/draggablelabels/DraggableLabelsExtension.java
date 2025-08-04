package qupath.ext.draggablelabels;

import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;

/**
 * QuPath extension for draggable annotation labels.
 */
public class DraggableLabelsExtension implements QuPathExtension {

    private DraggableLabelsManager manager;

    @Override
    public void installExtension(QuPathGUI qupath) {
        manager = new DraggableLabelsManager(qupath);
        manager.installMenuItems();
    }

    @Override
    public String getName() {
        return "Draggable Labels";
    }

    @Override
    public String getDescription() {
        return "Extension that allows users to drag annotation labels to reposition them for better visualization";
    }

    public Version getQuPathVersion() {
        return Version.parse("0.6.0");
    }
}