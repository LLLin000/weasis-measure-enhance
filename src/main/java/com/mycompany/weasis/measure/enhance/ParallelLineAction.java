package com.mycompany.weasis.measure.enhance;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.AbstractGraphicModel;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.imp.line.LineGraphic;
import org.weasis.core.ui.model.graphic.imp.line.ParallelLineGraphic;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;

/**
 * Creates a ParallelLineGraphic based on a selected LineGraphic.
 * The new parallel line can be dragged to adjust distance while maintaining parallelism.
 */
public class ParallelLineAction {

    private static final double DEFAULT_PARALLEL_DISTANCE = 50.0;

    /**
     * Create a parallel line based on the first selected LineGraphic.
     * 
     * @param view The ViewCanvas containing the graphics
     */
    public static void createParallelLine(ViewCanvas<?> view) {
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
                "请先选中一条线段。\nPlease select a line first.", 
                "创建平行线 / Create Parallel Line", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Point2D p1 = targetLine.getStartPoint();
        Point2D p2 = targetLine.getEndPoint();
        
        if (p1 == null || p2 == null) {
            JOptionPane.showMessageDialog(
                view.getJComponent(), 
                "选中的线段无效。\nInvalid line selected.", 
                "创建平行线 / Create Parallel Line", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Calculate the direction vector of the original line
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double lineLength = Math.sqrt(dx * dx + dy * dy);
        
        if (lineLength < 0.001) {
            return;
        }
        
        // Calculate perpendicular offset direction (normalized)
        double perpDx = -dy / lineLength;
        double perpDy = dx / lineLength;
        
        // Calculate offset distance (can be adjusted, default 50 pixels or line length * 0.3)
        double offsetDistance = Math.max(DEFAULT_PARALLEL_DISTANCE, lineLength * 0.3);
        
        // Calculate the parallel line points (offset perpendicular to original line)
        Point2D ptA = new Point2D.Double(p1.getX(), p1.getY());
        Point2D ptB = new Point2D.Double(p2.getX(), p2.getY());
        Point2D ptC = new Point2D.Double(p1.getX() + perpDx * offsetDistance, p1.getY() + perpDy * offsetDistance);
        Point2D ptD = new Point2D.Double(p2.getX() + perpDx * offsetDistance, p2.getY() + perpDy * offsetDistance);
        
        // Calculate midpoints
        Point2D ptE = GeomUtil.getMidPoint(ptA, ptB);
        Point2D ptF = GeomUtil.getMidPoint(ptC, ptD);
        
        // Create ParallelLineGraphic
        ParallelLineGraphic parallelLine = new ParallelLineGraphic();
        parallelLine.setLayerType(LayerType.MEASURE);
        parallelLine.setLineThickness(targetLine.getLineThickness());
        
        try {
            // ParallelLineGraphic needs 6 points: A, B, C, D, E (midpoint AB), F (midpoint CD)
            List<Point2D> points = new ArrayList<>(6);
            points.add(ptA);  // Point 0: A
            points.add(ptB);  // Point 1: B
            points.add(ptC);  // Point 2: C
            points.add(ptD);  // Point 3: D
            points.add(ptE);  // Point 4: E (midpoint of AB)
            points.add(ptF);  // Point 5: F (midpoint of CD)
            
            parallelLine.buildGraphic(points);
        } catch (InvalidShapeException e) {
            JOptionPane.showMessageDialog(
                view.getJComponent(), 
                "无法创建平行线: " + e.getMessage() + "\nFailed to create parallel line: " + e.getMessage(), 
                "错误 / Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Add to graphic model
        AbstractGraphicModel.addGraphicToModel(view, parallelLine);
        
        // Refresh view
        view.getJComponent().repaint();
    }
}
