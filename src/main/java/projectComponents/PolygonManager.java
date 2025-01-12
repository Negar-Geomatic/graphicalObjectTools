package projectComponents;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PolygonManager extends ShapeManager {
    protected final List<Polygon> polygons = new ArrayList<>();
    protected List<Point> currentPoints = new ArrayList<>();
    protected List<Polygon> selectedPolygons = new ArrayList<>();

    @Override
    public void addShape(List<Double> xValues, List<Double> yValues) {
        int[] xPoints = xValues.stream().mapToInt(Double::intValue).toArray();
        int[] yPoints = yValues.stream().mapToInt(Double::intValue).toArray();

        polygons.add(new Polygon(xPoints, yPoints, xPoints.length));
    }

    @Override
    public void setCurrentMode(ActionMode actionMode) {
        if (this.currentMode == ActionMode.CREATE && actionMode != ActionMode.CREATE) currentPoints = new ArrayList<>();
        this.currentMode = actionMode;
    }

    @Override
    public void resetSelection() {
        selectedPolygons = new ArrayList<>();
    }

    @Override
    public void resetDrawing() {
        currentPoints.clear();
    }

    @Override
    public void newFrame() {
        super.newFrame();
        this.polygons.clear();
        this.currentPoints.clear();
        this.selectedPolygons.clear();
    }

    @Override
    public void setMouseClickedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.CREATE) {
            // Polygon creation logic
            currentPoints.add(e.getPoint());
            // Complete polygon with right-click
            if (SwingUtilities.isRightMouseButton(e)) {
                int[] xPoints = currentPoints.stream().mapToInt(p -> p.x).toArray();
                int[] yPoints = currentPoints.stream().mapToInt(p -> p.y).toArray();
                if (xPoints.length > 2) {
                    polygons.add(new Polygon(xPoints, yPoints, xPoints.length));
                }
                currentPoints.clear();
            }
        } else if (currentMode == ActionMode.DELETE) {
            Polygon selectedPolygon = polygons.stream().filter(p -> p.contains(e.getPoint())).findFirst().orElse(null);
            if (selectedPolygon != null) {
                polygons.remove(selectedPolygon);
                selectedPolygons.remove(selectedPolygon);
            }
        } else if (currentMode == ActionMode.SELECT) {
            Polygon selectedPolygon = polygons.stream().filter(p -> p.contains(e.getPoint())).findFirst().orElse(null);
            if (selectedPolygon != null && selectedPolygons.contains(selectedPolygon)) selectedPolygons.remove(selectedPolygon);
            else if (selectedPolygon != null) selectedPolygons.add(selectedPolygon);
        }
    }

    @Override
    public void setMouseDraggedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.MOVE) {
            for (Polygon selectedPolygon : selectedPolygons) {
                int dx = e.getPoint().x - selectedPoint.x;
                int dy = e.getPoint().y - selectedPoint.y;
                selectedPolygon.translate(dx, dy);
            }
            selectedPoint = e.getPoint();
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        for (Polygon polygon : polygons) {
            g2d.setColor(Color.BLACK);
            if (selectedPolygons.contains(polygon)) g2d.setColor(Color.BLUE);
            g2d.drawPolygon(polygon);
        }

        if (!currentPoints.isEmpty() && currentMousePosition != null) {
            g2d.setColor(Color.RED);
            int[] xPoints = new int[currentPoints.size() + 1];
            int[] yPoints = new int[currentPoints.size() + 1];

            for (int i = 0; i < currentPoints.size(); i++) {
                Point p = currentPoints.get(i);
                xPoints[i] = p.x;
                yPoints[i] = p.y;
            }

            xPoints[currentPoints.size()] = currentMousePosition.x;
            yPoints[currentPoints.size()] = currentMousePosition.y;

            g2d.drawPolyline(xPoints, yPoints, xPoints.length);
        }
    }

    public List<org.locationtech.jts.geom.Polygon> getGeometries() {
        GeometryFactory factory = new GeometryFactory();
        List<org.locationtech.jts.geom.Polygon> geometries = new ArrayList<>();

        for (Polygon awtPolygon : this.polygons) {
            // Extract x and y points
            int[] xPoints = awtPolygon.xpoints;
            int[] yPoints = awtPolygon.ypoints;
            int numPoints = awtPolygon.npoints;

            // Create an array of Coordinates
            Coordinate[] coordinates = new Coordinate[numPoints + 1];
            for (int i = 0; i < numPoints; i++) {
                coordinates[i] = new Coordinate(xPoints[i], yPoints[i]);
            }
            coordinates[numPoints] = coordinates[0];

            org.locationtech.jts.geom.Polygon newPolygon = factory.createPolygon(coordinates);
            geometries.add(newPolygon);
        }

        return geometries;
    }

    @Override
    public String toCSVString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Polygon polygon : this.polygons) {
            stringBuilder.append("Polygon,");
            int[] xPoints = polygon.xpoints;
            int[] yPoints = polygon.ypoints;
            for (int i = 0; i < polygon.npoints; i++) {
                stringBuilder.append("(").append(xPoints[i]).append(" ").append(yPoints[i]).append(") ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
