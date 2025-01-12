package projectComponents;

import java.awt.*;
import java.awt.event.MouseEvent;

public class TriangleManager extends PolygonManager {

    @Override
    public void setMouseClickedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.CREATE) {
            currentPoints.add(e.getPoint());
            if (currentPoints.size() == 3) {
                // Create a triangle from the three points
                int[] xPoints = {currentPoints.get(0).x, currentPoints.get(1).x, currentPoints.get(2).x};
                int[] yPoints = {currentPoints.get(0).y, currentPoints.get(1).y, currentPoints.get(2).y};
                polygons.add(new Polygon(xPoints, yPoints, 3));
                currentPoints.clear(); // Clear points for the next triangle
            }
        } else if (currentMode == ActionMode.DELETE) {
            Polygon selectedTriangle = polygons.stream()
                    .filter(triangle -> triangle.contains(e.getPoint()))
                    .findFirst()
                    .orElse(null);
            if (selectedTriangle != null) {
                polygons.remove(selectedTriangle);
                selectedPolygons.remove(selectedTriangle);
            }
        } else if (currentMode == ActionMode.SELECT) {
            Polygon selectedTriangle = polygons.stream()
                    .filter(triangle -> triangle.contains(e.getPoint()))
                    .findFirst()
                    .orElse(null);
            if (selectedTriangle != null && selectedPolygons.contains(selectedTriangle))
                selectedPolygons.remove(selectedTriangle);
            else if (selectedTriangle != null) selectedPolygons.add(selectedTriangle);
        }
    }

    @Override
    public String toCSVString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Polygon triangle : this.polygons) {
            stringBuilder.append("Triangle,");
            int[] xPoints = triangle.xpoints;
            int[] yPoints = triangle.ypoints;
            for (int i = 0; i < triangle.npoints; i++) {
                stringBuilder.append("(").append(xPoints[i]).append(" ").append(yPoints[i]).append(") ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
