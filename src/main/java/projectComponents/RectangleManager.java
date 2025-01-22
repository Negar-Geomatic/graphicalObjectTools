package projectComponents;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
/**
 * The {@code RectangleManager} class manages the creation, selection, deletion, movement, and rendering of rectangles.
 * <p>
 * This class extends {@code ShapeManager} and provides functionality specific to rectangles. It supports operations
 * such as dynamically creating, selecting, deleting, and moving rectangles, as well as exporting them as JTS
 * {@code Polygon} objects or CSV-formatted data.
 */

public class RectangleManager extends ShapeManager {
	 /** List of all rectangles managed by this instance. */
    protected final List<Rectangle> rectangles = new ArrayList<>();
    /** List of currently selected rectangles. */
    protected List<Rectangle> selectedRectangles = new ArrayList<>();
    /**
     * Adds a rectangle to the manager.
     *
     * @param rectangle the rectangle to be added.
     */

    public void addShape(Rectangle rectangle) {
        this.rectangles.add(rectangle);
    }

    @Override
    public void resetSelection() {
        selectedRectangles = new ArrayList<>();
    }

    @Override
    public void newFrame() {
        super.newFrame();
        this.rectangles.clear();
        this.selectedRectangles.clear();
    }

    @Override
    public void setMouseClickedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.CREATE) {
            if (startPoint == null) {
                startPoint = e.getPoint(); // First click sets the starting point
            } else {
                // Second click creates the rectangle
                int x = Math.min(startPoint.x, e.getPoint().x);
                int y = Math.min(startPoint.y, e.getPoint().y);
                int width = Math.abs(startPoint.x - e.getPoint().x);
                int height = Math.abs(startPoint.y - e.getPoint().y);
                rectangles.add(new Rectangle(x, y, width, height));
                startPoint = null; // Reset startPoint for the next rectangle
            }
        } else if (currentMode == ActionMode.DELETE) {
            Rectangle selectedRectangle = rectangles.stream()
                    .filter(rectangle -> rectangle.contains(e.getPoint()))
                    .findFirst()
                    .orElse(null);
            if (selectedRectangle != null) {
                rectangles.remove(selectedRectangle);
                selectedRectangles.remove(selectedRectangle);
            }
        } else if (currentMode == ActionMode.SELECT) {
            Rectangle selectedRectangle = rectangles.stream()
                    .filter(rectangle -> rectangle.contains(e.getPoint()))
                    .findFirst()
                    .orElse(null);
            if (selectedRectangle != null && selectedRectangles.contains(selectedRectangle))
                selectedRectangles.remove(selectedRectangle);
            else if (selectedRectangle != null) selectedRectangles.add(selectedRectangle);
        }
    }

    @Override
    public void setMouseDraggedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.MOVE) {
            for (Rectangle rectangle : selectedRectangles) {
                int dx = e.getPoint().x - selectedPoint.x;
                int dy = e.getPoint().y - selectedPoint.y;
                rectangle.setLocation(rectangle.x + dx, rectangle.y + dy);
            }
            selectedPoint = e.getPoint(); // Update the last mouse position
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        for (Rectangle rect : rectangles) {
            g2d.setColor(Color.BLACK);
            if (selectedRectangles.contains(rect)) g2d.setColor(Color.BLUE);
            g2d.draw(rect); // Draw each rectangle
        }

        if (startPoint != null && currentMousePosition != null) {
            // Preview the rectangle being created
            g2d.setColor(Color.RED);
            int x = Math.min(startPoint.x, currentMousePosition.x);
            int y = Math.min(startPoint.y, currentMousePosition.y);
            int width = Math.abs(startPoint.x - currentMousePosition.x);
            int height = Math.abs(startPoint.y - currentMousePosition.y);
            g2d.drawRect(x, y, width, height);
        }

    }
    /**
     * Converts all managed rectangles into JTS {@code Polygon} objects for export or analysis.
     *
     * @return a list of JTS {@code Polygon} geometries representing the rectangles.
     */

    public List<org.locationtech.jts.geom.Polygon> getGeometries() {
        GeometryFactory factory = new GeometryFactory();
        List<org.locationtech.jts.geom.Polygon> geometries = new ArrayList<>();

        for (Rectangle rectangle : this.rectangles) {
            // Define the coordinates for the rectangle corners
            Coordinate[] coordinates = new Coordinate[5];
            coordinates[0] = new Coordinate(rectangle.getMinX(), rectangle.getMinY());
            coordinates[1] = new Coordinate(rectangle.getMaxX(), rectangle.getMinY());
            coordinates[2] = new Coordinate(rectangle.getMaxX(), rectangle.getMaxY());
            coordinates[3] = new Coordinate(rectangle.getMinX(), rectangle.getMaxY());
            coordinates[4] = coordinates[0]; // Closing the ring

            // Create a LinearRing and then the Polygon
            LinearRing shell = factory.createLinearRing(coordinates);
            org.locationtech.jts.geom.Polygon polygon = factory.createPolygon(shell, null);
            geometries.add(polygon);
        }

        return geometries;
    }

    @Override
    public String toCSVString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Rectangle rectangle : this.rectangles) {
            stringBuilder.append("Rectangle,");
            stringBuilder.append(rectangle.getX()).append(" ");
            stringBuilder.append(rectangle.getY()).append(" ");
            stringBuilder.append(rectangle.getWidth()).append(" ");
            stringBuilder.append(rectangle.getHeight()).append(" ");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
