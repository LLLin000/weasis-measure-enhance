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
 * Utility class to draw perpendicular lines at the trisection points of a selected line.
 */
public class TrisectionPerpendicularAction {

    private static final double DEFAULT_PERPENDICULAR_LENGTH = 100.0;

    /**
     * Draw two perpendicular lines at the 1/3 and 2/3 points of the first selected line.
     *
     * @param view The ViewCanvas containing the graphics
     */
    public static void drawTrisectionPerpendiculars(ViewCanvas<?> view) {
        if (view == null) {
            return;
        }

        GraphicModel model = view.getGraphicManager();
        if (model == null) {
            return;
        }

        List<Graphic> selected = model.getSelectedGraphics();
        LineGraphic targetLine = null;

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
                "Trisection Perpendiculars",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Point2D p1 = targetLine.getStartPoint();
        Point2D p2 = targetLine.getEndPoint();
        if (p1 == null || p2 == null) {
            JOptionPane.showMessageDialog(
                view.getJComponent(),
                "Invalid line selected.",
                "Trisection Perpendiculars",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double lineLength = Math.sqrt(dx * dx + dy * dy);
        if (lineLength < 0.001) {
            return;
        }

        Point2D oneThird = new Point2D.Double(p1.getX() + dx / 3.0, p1.getY() + dy / 3.0);
        Point2D twoThird = new Point2D.Double(p1.getX() + (2.0 * dx) / 3.0, p1.getY() + (2.0 * dy) / 3.0);

        double perpendicularLength = Math.max(DEFAULT_PERPENDICULAR_LENGTH, lineLength * 0.5);
        if (!createPerpendicularLine(view, targetLine, oneThird, dx, dy, lineLength, perpendicularLength)) {
            return;
        }
        if (!createPerpendicularLine(view, targetLine, twoThird, dx, dy, lineLength, perpendicularLength)) {
            return;
        }

        view.getJComponent().repaint();
    }

    private static boolean createPerpendicularLine(
        ViewCanvas<?> view,
        LineGraphic sourceLine,
        Point2D anchor,
        double dx,
        double dy,
        double lineLength,
        double perpendicularLength) {

        double halfLen = perpendicularLength / 2.0;
        double scale = halfLen / lineLength;

        double perpDx = -dy * scale;
        double perpDy = dx * scale;

        Point2D start = new Point2D.Double(anchor.getX() - perpDx, anchor.getY() - perpDy);
        Point2D end = new Point2D.Double(anchor.getX() + perpDx, anchor.getY() + perpDy);

        LineGraphic perpendicular = new LineGraphic();
        perpendicular.setLayerType(LayerType.MEASURE);
        perpendicular.setLineThickness(sourceLine.getLineThickness());

        try {
            perpendicular.buildGraphic(Arrays.asList(start, end));
        } catch (InvalidShapeException e) {
            JOptionPane.showMessageDialog(
                view.getJComponent(),
                "Failed to create trisection perpendicular: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        AbstractGraphicModel.addGraphicToModel(view, perpendicular);
        return true;
    }
}
