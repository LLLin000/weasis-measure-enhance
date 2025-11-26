package com.mycompany.weasis.measure.enhance;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.AbstractGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.util.MouseEventDouble;

public class SnappingUtil {
    
    private static final double SNAP_DISTANCE = 15.0; // Pixels

    public static Point2D getSnapPoint(MouseEventDouble mouseEvent, Point2D currentPoint, Graphic excludeGraphic) {
        if (mouseEvent != null && mouseEvent.getSource() instanceof ViewCanvas) {
            ViewCanvas<?> view = (ViewCanvas<?>) mouseEvent.getSource();
            GraphicModel model = view.getGraphicManager();
            if (model == null) return null;
            
            // Convert current point (Image Space) to Screen Space for distance check
            Point currentScreen = view.getMouseCoordinatesFromImage(currentPoint.getX(), currentPoint.getY());
            if (currentScreen == null) return null;

            Point2D bestMatch = null;
            double minDistance = Double.MAX_VALUE;

            for (Graphic graphic : model.getAllGraphics()) {
                if (graphic == excludeGraphic) continue;

                List<Point2D> handles = null;
                if (graphic instanceof AbstractGraphic) {
                    handles = ((AbstractGraphic) graphic).getHandlePointList();
                }
                
                if (handles == null) continue;
                
                for (Point2D handle : handles) {
                    // Convert handle (Image Space) to Screen Space
                    Point handleScreen = view.getMouseCoordinatesFromImage(handle.getX(), handle.getY());
                    if (handleScreen != null) {
                        double dist = handleScreen.distance(currentScreen);
                        if (dist < SNAP_DISTANCE && dist < minDistance) {
                            minDistance = dist;
                            bestMatch = handle; // Return the Image Space point
                        }
                    }
                }
            }
            return bestMatch;
        }
        return null;
    }
}