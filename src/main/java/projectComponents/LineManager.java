package projectComponents;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
/**
 * The `LineManager` class is responsible for managing lines in a graphical application.
 * <p>
 * This class provides functionality for creating, selecting, deleting, moving, and rendering lines.
 * It also supports exporting lines as geometrical `LineString` representations or CSV-formatted data.
 */

public class LineManager extends ShapeManager {
    private final List<Line> lines = new ArrayList<>();
    private List<Line> selectedLines = new ArrayList<>();

    @Override
    public void addShape(List<Double> xValues, List<Double> yValues) {
        List<Point> points = IntStream.range(0, xValues.size())
                .mapToObj(i -> new Point(xValues.get(i).intValue(), yValues.get(i).intValue()))
                .toList();
        for (int i = 0; i < points.size() - 1; i++) {
            this.lines.add(new Line(points.get(i), points.get(i + 1)));
        }
    }

    @Override
    public void resetSelection() {
        selectedLines = new ArrayList<>();
    }

    @Override
    public void newFrame() {
        super.newFrame();
        this.lines.clear();
        this.selectedLines.clear();
    }

    @Override
    public void setMouseClickedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.CREATE) {
            if (startPoint == null) {
                startPoint = e.getPoint();
            } else {
                lines.add(new Line(startPoint, e.getPoint()));
                startPoint = null;
            }
        } else if (currentMode == ActionMode.DELETE) {
            lines.removeIf(line -> line.contains(e.getPoint()));
            selectedLines.removeIf(line -> line.contains(e.getPoint()));
        } else if (currentMode == ActionMode.SELECT) {
            Line sLine = lines.stream().filter(line -> line.contains(e.getPoint())).findFirst().orElse(null);
            if (sLine != null && selectedLines.contains(sLine)) selectedLines.remove(sLine);
            else if (sLine != null) selectedLines.add(sLine);
        }
    }

    @Override
    public void setMouseDraggedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.MOVE) {
            for (Line line : selectedLines) {
                int dx = e.getPoint().x - selectedPoint.x;
                int dy = e.getPoint().y - selectedPoint.y;

                line.start.translate(dx, dy);
                line.end.translate(dx, dy);
            }
            selectedPoint = e.getPoint();
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        for (Line line : lines) {
            g2d.setColor(Color.BLACK);
            if (selectedLines.contains(line)) g2d.setColor(Color.BLUE);
            g2d.drawLine(line.start.x, line.start.y, line.end.x, line.end.y);
        }

        if (startPoint != null) {
            g2d.setColor(Color.RED);
            g2d.drawLine(startPoint.x, startPoint.y, currentMousePosition.x, currentMousePosition.y);
        }
    }
    /**
     * Converts all managed lines into geometrical `LineString` objects for export or analysis.
     *
     * @return a list of `LineString` geometries representing the lines.
     */

    public List<org.locationtech.jts.geom.LineString> getGeometries() {
        GeometryFactory factory = new GeometryFactory();
        List<org.locationtech.jts.geom.LineString> geometries = new ArrayList<>();

        for (Line line : this.lines) {
            Coordinate startCoordinate = new Coordinate(line.start.x, line.start.y);
            Coordinate endCoordinate = new Coordinate(line.end.x, line.end.y);
            Coordinate[] coordsArray = new Coordinate[]{startCoordinate, endCoordinate};

            org.locationtech.jts.geom.LineString newLine = factory.createLineString(coordsArray);
            geometries.add(newLine);
        }

        return geometries;
    }

    @Override
    public String toCSVString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Line line : this.lines) {
            stringBuilder.append("Line,");
            stringBuilder.append("(").append(line.start.x).append(" ");
            stringBuilder.append(line.start.y).append(") ");
            stringBuilder.append("(").append(line.end.x).append(" ");
            stringBuilder.append(line.end.y).append(")");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private static class Line {
        Point start, end;

        Line(Point start, Point end) {
            this.start = start;
            this.end = end;
        }

        boolean contains(Point p) {
            // Calculate the components of the line equation Ax + By + C = 0
            double A = end.y - start.y;
            double B = start.x - end.x;
            double C = end.x * start.y - start.x * end.y;

            // Calculate the perpendicular distance from point p to the line
            double distance = Math.abs(A * p.x + B * p.y + C) / Math.sqrt(A * A + B * B);

            // Return true if the distance is less than 2
            return distance < 4;
        }
    }
}
