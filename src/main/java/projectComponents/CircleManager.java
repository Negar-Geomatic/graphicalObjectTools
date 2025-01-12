package projectComponents;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

public class CircleManager extends EllipseManager {

    @Override
    public void setMouseClickedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.CREATE) {
            if (startPoint == null) {
                startPoint = e.getPoint();
            } else {
                int radius = (int) startPoint.distance(e.getPoint());
                ellipses.add(new Ellipse2D.Double(
                        startPoint.x - radius, startPoint.y - radius, radius * 2, radius * 2
                ));
                startPoint = null; // Reset the start point for the next circle
            }
        } else if (currentMode == ActionMode.DELETE) {
            Ellipse2D selectedCircle = ellipses.stream()
                    .filter(circle -> circle.contains(e.getPoint()))
                    .findFirst()
                    .orElse(null);
            if (selectedCircle != null) {
                ellipses.remove(selectedCircle);
                selectedEllipses.remove(selectedCircle);
            }
        } else if (currentMode == ActionMode.SELECT) {
            Ellipse2D selectedCircle = ellipses.stream()
                    .filter(circle -> circle.contains(e.getPoint()))
                    .findFirst()
                    .orElse(null);
            if (selectedCircle != null && selectedEllipses.contains(selectedCircle))
                selectedEllipses.remove(selectedCircle);
            else if (selectedCircle != null) selectedEllipses.add(selectedCircle);
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        for (Ellipse2D circle : ellipses) {
            g2d.setColor(Color.BLACK);
            if (selectedEllipses.contains(circle)) g2d.setColor(Color.BLUE);
            g2d.draw(circle);
        }

        if (startPoint != null && currentMousePosition != null) {
            g2d.setColor(Color.RED);
            int radius = (int) startPoint.distance(currentMousePosition);
            g2d.draw(new Ellipse2D.Double(startPoint.x - radius, startPoint.y - radius, radius * 2, radius * 2));
        }
    }

    @Override
    public String toCSVString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Ellipse2D circle : this.ellipses) {
            stringBuilder.append("Circle,");
            stringBuilder.append(circle.getX()).append(" ");
            stringBuilder.append(circle.getY()).append(" ");
            stringBuilder.append(circle.getWidth()).append(" ");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}