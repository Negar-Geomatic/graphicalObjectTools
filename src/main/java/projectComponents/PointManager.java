package projectComponents;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
/**
 * The `PointManager` class manages the creation, selection, deletion, movement, and rendering of points.
 * <p>
 * This class extends `ShapeManager` and provides specific implementations for managing points
 * as graphical shapes within the application.
 */

public class PointManager extends ShapeManager {
    private final List<Point> points = new ArrayList<>();
    private List<Point> selectedPoints = new ArrayList<>();

    @Override
    public void addShape(List<Double> xValues, List<Double> yValues) {
        for (int i = 0; i < xValues.size(); i++) {
            points.add(new Point(xValues.get(i).intValue(), yValues.get(i).intValue()));
        }
    }

    @Override
    public void resetSelection() {
        selectedPoints = new ArrayList<>();
    }

    @Override
    public void newFrame() {
        super.newFrame();
        this.points.clear();
        this.selectedPoints.clear();
    }

    @Override
    public void setMouseClickedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.CREATE) {
            points.add(e.getPoint());
        } else if (currentMode == ActionMode.DELETE) {
            points.removeIf(point -> point.distance(e.getPoint()) < 5);
            selectedPoints.removeIf(point -> point.distance(e.getPoint()) < 5);
        } else if (currentMode == ActionMode.SELECT) {
            Point sPoint = points.stream()
                    .filter(point -> point.distance(e.getPoint()) < 5)
                    .findFirst()
                    .orElse(null);
            if (sPoint != null && selectedPoints.contains(sPoint)) {
                selectedPoints.remove(sPoint);
            } else if (sPoint != null){
                selectedPoints.add(sPoint);
            }
        }
    }

    @Override
    public void setMouseDraggedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.MOVE) {
            for (Point point : selectedPoints) {
                int dx = e.getPoint().x - selectedPoint.x;
                int dy = e.getPoint().y - selectedPoint.y;
                point.translate(dx, dy);
            }
            selectedPoint = e.getPoint();
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        for (Point point : points) {
            g2d.fillOval(point.x - 3, point.y - 3, 6, 6);
        }
        for (Point point: selectedPoints) {
            g2d.setColor(Color.BLUE);
            g2d.drawOval(point.x - 5, point.y - 5, 10, 10);
        }
    }
    /**
     * Converts all managed points into geometrical `Point` objects for export or analysis.
     *
     * @return a list of `Point` geometries representing the points.
     */

    public List<org.locationtech.jts.geom.Point> getGeometries() {
        GeometryFactory factory = new GeometryFactory();
        List<org.locationtech.jts.geom.Point> geometries = new ArrayList<>();

        for (Point point : this.points) {
            org.locationtech.jts.geom.Point newPoint = factory.createPoint(new Coordinate(point.x, point.y));
            geometries.add(newPoint);
        }

        return geometries;
    }

    @Override
    public String toCSVString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Point point : this.points) {
            stringBuilder.append("Point,");
            stringBuilder.append("(").append(point.x).append(" ");
            stringBuilder.append(point.y).append(")");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
