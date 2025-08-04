package qupath.ext.draggablelabels;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collection;

import qupath.lib.gui.viewer.OverlayOptions;
import qupath.lib.gui.viewer.overlays.AbstractOverlay;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.regions.ImageRegion;

/**
 * Overlay for rendering draggable annotation labels in QuPath.
 */
public class DraggableLabelOverlay extends AbstractOverlay {

    private DraggableLabelHandler handler;

    public DraggableLabelOverlay(OverlayOptions overlayOptions) {
        super(overlayOptions);
    }

    public void setHandler(DraggableLabelHandler handler) {
        this.handler = handler;
    }

    @Override
    public void paintOverlay(Graphics2D g2d, ImageRegion imageRegion, 
                           double downsampleFactor, ImageData<BufferedImage> imageData, boolean paintCompletely) {
        
        if (imageData == null || handler == null) {
            return;
        }

        Collection<PathObject> pathObjects = imageData.getHierarchy().getObjects(null, PathObject.class);
        
        for (PathObject pathObject : pathObjects) {
            if (pathObject.hasROI() && pathObject.getDisplayedName() != null && !pathObject.getDisplayedName().isEmpty()) {
                
                // Get the center point of the object
                var roi = pathObject.getROI();
                double centerX = roi.getCentroidX();
                double centerY = roi.getCentroidY();
                
                Point2D p = new Point2D.Double(centerX, centerY);
                
                // Transform point to display coordinates
                AffineTransform transform = g2d.getTransform();
                Point2D transformedPoint = new Point2D.Double();
                transform.transform(p, transformedPoint);
                
                if (transformedPoint != null) {
                    // Get any custom offset for this annotation
                    Point2D offset = handler.getLabelOffset(pathObject);
                    
                    // Apply offset
                    double displayX = transformedPoint.getX() + offset.getX();
                    double displayY = transformedPoint.getY() + offset.getY();
                    
                    // Draw the label
                    g2d.drawString(pathObject.getDisplayedName(), (float)displayX, (float)displayY);
                }
            }
        }
    }

    public boolean supportsImageDataChange() {
        return true;
    }
}