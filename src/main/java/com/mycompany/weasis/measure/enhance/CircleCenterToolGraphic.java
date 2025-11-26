package com.mycompany.weasis.measure.enhance;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.weasis.core.ui.model.graphic.imp.area.ThreePointsCircleGraphic;
import org.weasis.core.ui.util.MouseEventDouble;

public class CircleCenterToolGraphic extends ThreePointsCircleGraphic {

    public CircleCenterToolGraphic() {
        super();
    }

    @Override
    public void buildShape(MouseEventDouble mouseEvent) {
        super.buildShape(mouseEvent);
        
        if (Objects.nonNull(centerPt) && !Objects.equals(radiusPt, 0d)) {
            Shape circle = getShape();
            if (circle != null) {
                Path2D path = new Path2D.Double(circle);
                
                // Add a cross at the center
                double crossSize = 2.5; // smaller cross marker
                path.append(new Line2D.Double(centerPt.getX() - crossSize, centerPt.getY(), centerPt.getX() + crossSize, centerPt.getY()), false);
                path.append(new Line2D.Double(centerPt.getX(), centerPt.getY() - crossSize, centerPt.getX(), centerPt.getY() + crossSize), false);
                
                setShape(path, mouseEvent);
            }
        }
    }

    @Override
    public List<Point2D> getHandlePointList() {
        List<Point2D> handles = new ArrayList<>(super.getHandlePointList());
        if (centerPt != null) {
            handles.add(centerPt);
        }
        return handles;
    }

    @Override
    public Integer moveAndResizeOnDrawing(Integer handlePointIndex, Double deltaX, Double deltaY, MouseEventDouble mouseEvent) {
        if (handlePointIndex != null && handlePointIndex >= getPts().size()) {
            // Move the whole circle when dragging the center
            for (Point2D pt : getPts()) {
                pt.setLocation(pt.getX() + deltaX, pt.getY() + deltaY);
            }
            buildShape(mouseEvent);
            return handlePointIndex;
        }
        return super.moveAndResizeOnDrawing(handlePointIndex, deltaX, deltaY, mouseEvent);
    }
    
    @Override
    public String getUIName() {
        return "Circle Center";
    }

    @Override
    public CircleCenterToolGraphic copy() {
        return new CircleCenterToolGraphic();
    }
}