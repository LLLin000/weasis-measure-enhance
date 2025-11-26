package com.mycompany.weasis.measure.enhance;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;

import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.AbstractGraphicModel;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.imp.line.LineGraphic;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;

/**
 * Utility class to draw a perpendicular bisector for a selected line.
 * Creates a simple LineGraphic as the bisector.
 */
public class PerpendicularBisectorAction {

    private static final double DEFAULT_BISECTOR_LENGTH = 100.0;

    /**
     * Draw perpendicular bisector for the first selected LineGraphic.
     * 
     * @param view The ViewCanvas containing the graphics
     */
    public static void drawPerpendicularBisector(ViewCanvas<?> view) {
        if (view == null) {
            return;
        }
        
        GraphicModel model = view.getGraphicManager();
        if (model == null) {
            return;
        }
        
        List<Graphic> selected = model.getSelectedGraphics();
        LineGraphic targetLine = null;
        
        // Find the first selected LineGraphic
        for (Graphic g : selected) {
            if (g instanceof LineGraphic lineGraphic) {
                targetLine = lineGraphic;
                break;
            }
        }
        
        if (targetLine == null) {
            JOptionPane.showMessageDialog(
                view.getJComponent(), 
                "Please select a line first.", 
                "Perpendicular Bisector", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Point2D p1 = targetLine.getStartPoint();
        Point2D p2 = targetLine.getEndPoint();
        
        if (p1 == null || p2 == null) {
            JOptionPane.showMessageDialog(
                view.getJComponent(), 
                "Invalid line selected.", 
                "Perpendicular Bisector", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Calculate midpoint
        double midX = (p1.getX() + p2.getX()) / 2.0;
        double midY = (p1.getY() + p2.getY()) / 2.0;
        
        // Calculate direction vector of the original line
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        
        double lineLength = Math.sqrt(dx * dx + dy * dy);
        if (lineLength < 0.001) {
            return;
        }
        
        // Perpendicular direction (rotate 90 degrees)
        double perpDx = -dy;
        double perpDy = dx;
        
        // Normalize and scale
        double bisectorLength = Math.max(DEFAULT_BISECTOR_LENGTH, lineLength * 0.5);
        double halfLen = bisectorLength / 2.0;
        double scale = halfLen / lineLength;
        
        perpDx *= scale;
        perpDy *= scale;
        
        // Calculate endpoints
        Point2D bisectorStart = new Point2D.Double(midX - perpDx, midY - perpDy);
        Point2D bisectorEnd = new Point2D.Double(midX + perpDx, midY + perpDy);
        
        // Create LineGraphic using buildGraphic for proper initialization
        LineGraphic bisector = new LineGraphic();
        bisector.setLayerType(LayerType.MEASURE);
        bisector.setLineThickness(targetLine.getLineThickness());
        
        try {
            // buildGraphic properly initializes pts list and calls prepareShape
            bisector.buildGraphic(Arrays.asList(bisectorStart, bisectorEnd));
        } catch (InvalidShapeException e) {
            JOptionPane.showMessageDialog(
                view.getJComponent(), 
                "Failed to create bisector: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Add to graphic model using proper method that registers listeners
        AbstractGraphicModel.addGraphicToModel(view, bisector);
        
        // Refresh view
        view.getJComponent().repaint();
    }
}
