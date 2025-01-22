package projectComponents;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
/**
 * The `EllipseManager` class manages the creation, selection, deletion, and movement of ellipses.
 * <p>
 * This class extends `ShapeManager` and provides functionality for drawing, manipulating, and exporting ellipses.
 */

public class EllipseManager extends ShapeManager {
    protected final List<Ellipse2D> ellipses = new ArrayList<>();
    protected List<Ellipse2D> selectedEllipses = new ArrayList<>();
    /**
     * Adds a new ellipse to the manager.
     *
     * @param ellipse2d the `Ellipse2D` object to be added.
     */
    public void addShape(Ellipse2D ellipse2d) {
        this.ellipses.add(ellipse2d);
    }

    @Override
    public void resetSelection() {
        selectedEllipses = new ArrayList<>();
    }

    @Override
    public void newFrame() {
        super.newFrame();
        this.ellipses.clear();
        this.selectedEllipses.clear();
    }

    @Override
    public void setMouseClickedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.CREATE) {
            if (startPoint == null) {
                startPoint = e.getPoint();
            } else {
                int width = Math.abs(startPoint.x - e.getPoint().x);
                int height = Math.abs(startPoint.y - e.getPoint().y);
                ellipses.add(new Ellipse2D.Double(
                    Math.min(startPoint.x, e.getPoint().x),
                    Math.min(startPoint.y, e.getPoint().y),
                    width, height
                ));
                startPoint = null;
            }
        } else if (currentMode == ActionMode.DELETE) {
            Ellipse2D selectedEllipse = ellipses.stream()
                    .filter(ellipse -> ellipse.contains(e.getPoint()))
                    .findFirst()
                    .orElse(null);
            if (selectedEllipse != null) {
                ellipses.remove(selectedEllipse);
                selectedEllipses.remove(selectedEllipse);
            }
            ellipses.removeIf(ellipse -> ellipse.contains(e.getPoint()));
        } else if (currentMode == ActionMode.SELECT) {
            Ellipse2D selectedEllipse = ellipses.stream()
                .filter(ellipse -> ellipse.contains(e.getPoint()))
                .findFirst()
                .orElse(null);
            if (selectedEllipse != null && selectedEllipses.contains(selectedEllipse))
                selectedEllipses.remove(selectedEllipse);
            else if (selectedEllipse != null) selectedEllipses.add(selectedEllipse);
        }
    }

    @Override
    public void setMouseDraggedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.MOVE) {
            for (Ellipse2D selectedEllipse : selectedEllipses) {
                double dx = e.getPoint().x - selectedPoint.x;
                double dy = e.getPoint().y - selectedPoint.y;

                selectedEllipse.setFrame(
                        selectedEllipse.getX() + dx,
                        selectedEllipse.getY() + dy,
                        selectedEllipse.getWidth(),
                        selectedEllipse.getHeight()
                );
            }
            selectedPoint = e.getPoint();
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        for (Ellipse2D ellipse : ellipses) {
            g2d.setColor(Color.BLACK);
            if (selectedEllipses.contains(ellipse)) g2d.setColor(Color.BLUE);
            g2d.draw(ellipse);
        }

        if (startPoint != null && currentMousePosition != null) {
            g2d.setColor(Color.RED);
            int width = Math.abs(startPoint.x - currentMousePosition.x);
            int height = Math.abs(startPoint.y - currentMousePosition.y);
            g2d.draw(new Ellipse2D.Double(
                    Math.min(startPoint.x, currentMousePosition.x),
                    Math.min(startPoint.y, currentMousePosition.y),
                    width, height
            ));
        }
    }
    /**
     * Converts all managed ellipses into `Polygon` geometries for export or analysis.
     * <p>
     * Each ellipse is approximated using a specified number of points to create a polygon.
     *
     * @return a list of `Polygon` geometries representing the ellipses.
     */

    public List<org.locationtech.jts.geom.Polygon> getGeometries() {
        GeometryFactory factory = new GeometryFactory();
        List<org.locationtech.jts.geom.Polygon> geometries = new ArrayList<>();

        final int numPoints = 100;  // Number of points to approximate the ellipse

        for (Ellipse2D ellipse : this.ellipses) {
            Coordinate[] coordinates = new Coordinate[numPoints + 1];
            double centerX = ellipse.getCenterX();
            double centerY = ellipse.getCenterY();
            double a = ellipse.getWidth() / 2.0;  // Semi-major axis
            double b = ellipse.getHeight() / 2.0; // Semi-minor axis

            for (int i = 0; i < numPoints; i++) {
                double theta = 2 * Math.PI * i / numPoints;
                double x = centerX + a * Math.cos(theta);
                double y = centerY + b * Math.sin(theta);
                coordinates[i] = new Coordinate(x, y);
            }
            coordinates[numPoints] = coordinates[0]; // Closing the ring

            LinearRing shell = factory.createLinearRing(coordinates);
            org.locationtech.jts.geom.Polygon polygon = factory.createPolygon(shell, null);
            geometries.add(polygon);
        }

        return geometries;
    }

    @Override
    public String toCSVString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Ellipse2D ellipse : this.ellipses) {
            stringBuilder.append("Ellipse,");
            stringBuilder.append(ellipse.getX()).append(" ");
            stringBuilder.append(ellipse.getY()).append(" ");
            stringBuilder.append(ellipse.getWidth()).append(" ");
            stringBuilder.append(ellipse.getHeight()).append(" ");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}