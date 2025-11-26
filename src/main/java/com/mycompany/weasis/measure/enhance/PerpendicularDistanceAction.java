package com.mycompany.weasis.measure.enhance;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
 * Tool to measure perpendicular distance from a point to a selected line.
 * User selects a line first, then clicks a point to measure the perpendicular distance.
 */
public class PerpendicularDistanceAction {

    private static MouseAdapter currentListener = null;
    private static ViewCanvas<?> currentView = null;
    private static Cursor originalCursor = null;

    /**
     * Start the perpendicular distance measurement mode.
     * User must have a line selected, then click a point to measure distance.
     * 
     * @param view The ViewCanvas containing the graphics
     */
    public static void startMeasurement(ViewCanvas<?> view) {
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
                "请先选中一条参考线", 
                "垂直距离测量", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final LineGraphic refLine = targetLine;
        
        // Cancel any previous listener
        cancelMeasurement();
        
        // Store current view
        currentView = view;
        
        // Change cursor to crosshair
        originalCursor = view.getJComponent().getCursor();
        view.getJComponent().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        
        // Add mouse listener to capture click
        currentListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Get click point in image coordinates
                    Point2D clickPoint = view.getImageCoordinatesFromMouse(e.getX(), e.getY());
                    
                    if (clickPoint != null) {
                        createPerpendicularLine(view, refLine, clickPoint);
                    }
                    
                    // Remove listener and restore cursor
                    cancelMeasurement();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // Right click to cancel
                    cancelMeasurement();
                }
            }
        };
        
        view.getJComponent().addMouseListener(currentListener);
    }
    
    /**
     * Cancel the measurement mode.
     */
    public static void cancelMeasurement() {
        if (currentListener != null && currentView != null) {
            currentView.getJComponent().removeMouseListener(currentListener);
            if (originalCursor != null) {
                currentView.getJComponent().setCursor(originalCursor);
            }
        }
        currentListener = null;
        currentView = null;
        originalCursor = null;
    }
    
    /**
     * Create a perpendicular line from the click point to the reference line.
     */
    private static void createPerpendicularLine(ViewCanvas<?> view, LineGraphic refLine, Point2D clickPoint) {
        Point2D p1 = refLine.getStartPoint();
        Point2D p2 = refLine.getEndPoint();
        
        if (p1 == null || p2 == null) {
            return;
        }
        
        // Calculate the foot of perpendicular (closest point on line to click point)
        Point2D footPoint = getPerpendicularFoot(p1, p2, clickPoint);
        
        // Create the perpendicular line from click point to foot point
        LineGraphic perpLine = new LineGraphic();
        perpLine.setLayerType(LayerType.MEASURE);
        perpLine.setLineThickness(refLine.getLineThickness());
        
        try {
            perpLine.buildGraphic(Arrays.asList(clickPoint, footPoint));
        } catch (InvalidShapeException e) {
            JOptionPane.showMessageDialog(
                view.getJComponent(), 
                "创建垂直线失败: " + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Add to graphic model
        AbstractGraphicModel.addGraphicToModel(view, perpLine);
        
        // Refresh view
        view.getJComponent().repaint();
    }
    
    /**
     * Calculate the foot of perpendicular from point P to line AB.
     * This is the closest point on line AB to point P.
     * 
     * @param lineStart Start point of line (A)
     * @param lineEnd End point of line (B)
     * @param point The point (P)
     * @return The foot of perpendicular on line AB
     */
    private static Point2D getPerpendicularFoot(Point2D lineStart, Point2D lineEnd, Point2D point) {
        double ax = lineStart.getX();
        double ay = lineStart.getY();
        double bx = lineEnd.getX();
        double by = lineEnd.getY();
        double px = point.getX();
        double py = point.getY();
        
        // Direction vector of line AB
        double dx = bx - ax;
        double dy = by - ay;
        
        // Length squared of AB
        double lenSq = dx * dx + dy * dy;
        
        if (lenSq < 1e-10) {
            // Line is essentially a point
            return new Point2D.Double(ax, ay);
        }
        
        // Parameter t for the foot point: F = A + t * (B - A)
        // t = dot(AP, AB) / |AB|^2
        double t = ((px - ax) * dx + (py - ay) * dy) / lenSq;
        
        // Note: We don't clamp t to [0,1] so the foot can be on the extended line
        // If you want to restrict to the line segment, uncomment below:
        // t = Math.max(0, Math.min(1, t));
        
        // Calculate foot point
        double footX = ax + t * dx;
        double footY = ay + t * dy;
        
        return new Point2D.Double(footX, footY);
    }
}
