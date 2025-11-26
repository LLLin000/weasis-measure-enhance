package com.mycompany.weasis.measure.enhance;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.weasis.core.ui.model.graphic.imp.line.LineGraphic;
import org.weasis.core.ui.util.MouseEventDouble;

public class ContinueLineToolGraphic extends LineGraphic {

    private boolean startPointSnapped = false;

    public ContinueLineToolGraphic() {
        super();
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        if (mouseEvent != null) {
            List<Point2D> points = getPts();
            if (points != null && !points.isEmpty()) {
                int lastIndex = points.size() - 1;
                Point2D currentPt = points.get(lastIndex);
                
                Point2D snapped = SnappingUtil.getSnapPoint(mouseEvent, currentPt, this);
                if (snapped != null) {
                    points.set(lastIndex, snapped);
                }
            }
        }

        super.buildShape(mouseEvent);
        
        List<Point2D> points = getPts();
        if (points.size() == 2) {
            Point2D A = points.get(0);
            Point2D B = points.get(1);
            
            if (A != null && B != null) {
                // Extend line from B
                double dx = B.getX() - A.getX();
                double dy = B.getY() - A.getY();
                
                // Extend by same length
                double endX = B.getX() + dx;
                double endY = B.getY() + dy;
                
                Shape currentShape = getShape();
                if (currentShape != null) {
                    Path2D path = new Path2D.Double(currentShape);
                    // Draw extension as dashed line? For now just solid.
                    path.append(new Line2D.Double(B.getX(), B.getY(), endX, endY), false);
                    setShape(path, mouseEvent);
                }
            }
        }
    }
    
    @Override
    public String getUIName() {
        return "Continue Line";
    }

    @Override
    public ContinueLineToolGraphic copy() {
        return new ContinueLineToolGraphic();
    }
}