package com.mycompany.weasis.measure.enhance;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.imp.line.LineGraphic;

/**
 * Utility class to calculate angle between two selected lines.
 * Call calculateAngle() when you want to compute the angle.
 */
public class AngleCalculationAction {

    /**
     * Calculate the angle between two selected LineGraphic objects.
     * Shows a dialog with the result.
     * 
     * @param view The ViewCanvas containing the graphics
     */
    public static void calculateAngle(ViewCanvas<?> view) {
        if (view == null) {
            return;
        }
        
        GraphicModel model = view.getGraphicManager();
        if (model == null) {
            return;
        }
        
        List<Graphic> selected = model.getSelectedGraphics();
        List<Line2D> lines = new ArrayList<>();
        
        for (Graphic g : selected) {
            if (g instanceof LineGraphic lineGraphic) {
                List<Point2D> pts = lineGraphic.getPts();
                if (pts != null && pts.size() >= 2) {
                    Point2D p1 = pts.get(0);
                    Point2D p2 = pts.get(1);
                    if (p1 != null && p2 != null) {
                        lines.add(new Line2D.Double(p1, p2));
                    }
                }
            }
        }
        
        if (lines.size() >= 2) {
            Line2D line1 = lines.get(0);
            Line2D line2 = lines.get(1);
            
            // Calculate direction vectors
            double dx1 = line1.getX2() - line1.getX1();
            double dy1 = line1.getY2() - line1.getY1();
            double dx2 = line2.getX2() - line2.getX1();
            double dy2 = line2.getY2() - line2.getY1();
            
            // Calculate angle using dot product
            double dot = dx1 * dx2 + dy1 * dy2;
            double len1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
            double len2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);
            
            if (len1 > 0 && len2 > 0) {
                double cosAngle = dot / (len1 * len2);
                // Clamp to [-1, 1] to avoid NaN from acos
                cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle));
                double angle = Math.toDegrees(Math.acos(cosAngle));
                
                // Also calculate the supplementary angle
                double supplementary = 180 - angle;
                
                String message = String.format(
                    "Angle between the two lines:\n" +
                    "  Acute/Obtuse: %.2f°\n" +
                    "  Supplementary: %.2f°",
                    angle, supplementary);
                JOptionPane.showMessageDialog(
                    view.getJComponent(), 
                    message, 
                    "Angle Calculation", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(
                view.getJComponent(), 
                "Please select exactly 2 lines (use Ctrl+Click to multi-select).", 
                "Angle Calculation", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
}
