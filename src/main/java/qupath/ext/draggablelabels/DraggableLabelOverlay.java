package qupath.ext.draggablelabels;

import qupath.lib.gui.viewer.overlays.AbstractOverlay;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.regions.ImageRegion;
import qupath.lib.awt.common.AwtTools;
import javafx.geometry.Point2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
// import java.awt.geom.Point2D.Double as AwtPoint2D; // Removed invalid import
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * Overlay for rendering annotation labels at custom positions
 */
public class DraggableLabelOverlay extends AbstractOverlay {
    
    private Map<PathObject, Point2D> labelOffsets = new HashMap<>();
    
    public void setLabelOffsets(Map<PathObject, Point2D> labelOffsets) {
        this.labelOffsets = new HashMap<>(labelOffsets);
    }
    
    @Override
    public void paintOverlay(Graphics2D g, ImageRegion imageRegion, double downsampleFactor, 
                           ImageData<?, ?> imageData, boolean paintCompletely) {
        
        if (imageData == null) return;
        
        Collection<PathObject> annotations = imageData.getHierarchy().getAnnotationObjects();
        
        // Set up graphics for text rendering
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics metrics = g.getFontMetrics();
        
        // Draw each annotation label with offset
        for (PathObject annotation : annotations) {
            String displayName = annotation.getDisplayedName();
            if (displayName == null || displayName.isEmpty() || annotation.getROI() == null) {
                continue;
            }
            
            // Get annotation center
            double centerX = annotation.getROI().getCentroidX();
            double centerY = annotation.getROI().getCentroidY();
            
            // Apply stored offset
            Point2D offset = labelOffsets.getOrDefault(annotation, new Point2D(0, 0));
            centerX += offset.getX();
            centerY += offset.getY();
            
            // Convert to graphics coordinates
            java.awt.geom.Point2D.Double p = new java.awt.geom.Point2D.Double(centerX, centerY);
            if (!AwtTools.transformPoint(p, imageRegion.getImagePlane(), 
                                       g.getTransform(), downsampleFactor)) {
                continue;
            }
            
            // Draw text with outline for visibility
            int textWidth = metrics.stringWidth(displayName);
            int x = (int)(p.x - textWidth/2.0);
            int y = (int)(p.y);
            
            // Black outline
            g.setColor(Color.BLACK);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {
                        g.drawString(displayName, x + dx, y + dy);
                    }
                }
            }
            
            // White text
            g.setColor(Color.WHITE);
            g.drawString(displayName, x, y);
        }
    }
}