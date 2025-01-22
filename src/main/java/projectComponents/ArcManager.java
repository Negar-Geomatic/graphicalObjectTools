package projectComponents;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;
/**
 * The `ArcManager` class manages the creation, selection, deletion, and movement of arc-shaped graphical objects.
 * <p>
 * This class extends the `ShapeManager` class and provides specific implementations for managing `Arc2D` objects.
 * It also supports exporting arcs to geometrical representations and CSV format.
 */
public class ArcManager extends ShapeManager {
    private final List<Arc2D> arcs = new ArrayList<>();
    private List<Arc2D> selectedArcs = new ArrayList<>();
    /**
     * Adds a new arc to the manager.
     *
     * @param arc2d the `Arc2D` object to be added.
     */
    public void addShape(Arc2D arc2d) {
        this.arcs.add(arc2d);
    }

    @Override
    public void resetSelection() {
        selectedArcs = new ArrayList<>();
        startPoint = null;
    }

    @Override
    public void newFrame() {
        super.newFrame();
        this.selectedArcs.clear();
        this.arcs.clear();
    }

    @Override
    public void setMouseClickedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.CREATE) {
            if (startPoint == null) {
                startPoint = e.getPoint();
            } else {
                int width = Math.abs(startPoint.x - e.getPoint().x);
                int height = Math.abs(startPoint.y - e.getPoint().y);
                arcs.add(new Arc2D.Double(
                    Math.min(startPoint.x, e.getPoint().x),
                    Math.min(startPoint.y, e.getPoint().y),
                    width, height, 0, 180, 0
                ));
                startPoint = null;
            }
        } else if (currentMode == ActionMode.DELETE) {
            Arc2D selectedArc = arcs.stream()
                    .filter(arc -> arc.contains(e.getPoint()))
                    .findFirst()
                    .orElse(null);
            if (selectedArc != null) {
                arcs.remove(selectedArc);
                selectedArcs.remove(selectedArc);
            }
        } else if (currentMode == ActionMode.SELECT) {
            Arc2D selectedArc = arcs.stream()
                .filter(arc -> arc.contains(e.getPoint()))
                .findFirst()
                .orElse(null);
            if (selectedArc != null && selectedArcs.contains(selectedArc))
                selectedArcs.remove(selectedArc);
            else if (selectedArc != null) selectedArcs.add(selectedArc);
        }
    }

    @Override
    public void setMouseDraggedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.MOVE) {
            for (Arc2D selectedArc : selectedArcs) {
                double dx = e.getPoint().x - selectedPoint.x;
                double dy = e.getPoint().y - selectedPoint.y;

                selectedArc.setFrame(
                        selectedArc.getX() + dx,
                        selectedArc.getY() + dy,
                        selectedArc.getWidth(),
                        selectedArc.getHeight()
                );

            }
            selectedPoint = e.getPoint();
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        for (Arc2D arc : arcs) {
            g2d.setColor(Color.BLACK);
            if (selectedArcs.contains(arc)) g2d.setColor(Color.BLUE);
            g2d.draw(arc);
        }

        if (startPoint != null && currentMousePosition != null) {
            g2d.setColor(Color.RED);
            int width = Math.abs(startPoint.x - currentMousePosition.x);
            int height = Math.abs(startPoint.y - currentMousePosition.y);
            g2d.draw(new Arc2D.Double(
                    Math.min(startPoint.x, currentMousePosition.x),
                    Math.min(startPoint.y, currentMousePosition.y),
                    width, height, 0, 180, Arc2D.OPEN
            ));
        }
    }
    /**
     * Converts all managed arcs into `LineString` geometries for export or analysis.
     * <p>
     * Each arc is approximated using a specified number of points to create a `LineString`.
     *
     * @return a list of `LineString` geometries representing the arcs.
     */

    public List<LineString> getGeometries() {
        GeometryFactory factory = new GeometryFactory();
        List<LineString> geometries = new ArrayList<>();

        final int numPoints = 50;  // Number of points to approximate the arc

        for (Arc2D arc : this.arcs) {
            List<Coordinate> coordinates = new ArrayList<>();
            double centerX = arc.getCenterX();
            double centerY = arc.getCenterY();
            double a = arc.getWidth() / 2.0;  // Semi-major axis
            double b = arc.getHeight() / 2.0; // Semi-minor axis

            // Calculate the start angle and extent in radians
            double startTheta = Math.toRadians(arc.getAngleStart());
            double endTheta = startTheta + Math.toRadians(arc.getAngleExtent());

            // Ensure we are correctly representing the arc from the left to the right
            for (int i = 0; i <= numPoints; i++) {
                double theta = startTheta + ((endTheta - startTheta) * i / numPoints);
                double x = centerX + a * Math.cos(theta);
                double y = centerY - b * Math.sin(theta); // Adjust y-coordinate for top semi-circle
                coordinates.add(new Coordinate(x, y));
            }

            // Create a LineString from the sampled coordinates
            LineString lineString = factory.createLineString(coordinates.toArray(new Coordinate[0]));
            geometries.add(lineString);
        }

        return geometries;
    }

    @Override
    public String toCSVString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Arc2D arc2d : this.arcs) {
            stringBuilder.append("Arc,");
            stringBuilder.append(arc2d.getX()).append(" ");
            stringBuilder.append(arc2d.getY()).append(" ");
            stringBuilder.append(arc2d.getWidth()).append(" ");
            stringBuilder.append(arc2d.getHeight()).append(" ");
            stringBuilder.append(0 + " ");
            stringBuilder.append(180 + " ");
            stringBuilder.append(0);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}