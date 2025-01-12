package projectComponents;

import java.awt.*;
import java.awt.event.MouseEvent;

public class SquareManager extends RectangleManager {

    @Override
    public void setMouseClickedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.CREATE) {
            if (startPoint == null) {
                startPoint = e.getPoint(); // First click sets the starting point
            } else {
                // Create a square using the larger distance as the side length
                int sideLength = Math.max(
                    Math.abs(startPoint.x - e.getPoint().x),
                    Math.abs(startPoint.y - e.getPoint().y)
                );
                rectangles.add(new Rectangle(
                    Math.min(startPoint.x, e.getPoint().x),
                    Math.min(startPoint.y, e.getPoint().y),
                    sideLength, sideLength
                ));
                startPoint = null; // Reset startPoint for the next square
            }
        } else if (currentMode == ActionMode.DELETE) {
            Rectangle selectedSquare = rectangles.stream()
                    .filter(square -> square.contains(e.getPoint()))
                    .findFirst()
                    .orElse(null);
            if (selectedSquare != null) {
                rectangles.remove(selectedSquare);
                selectedRectangles.remove(selectedSquare);
            }
        } else if (currentMode == ActionMode.SELECT) {
            Rectangle selectedSquare = rectangles.stream()
                    .filter(square -> square.contains(e.getPoint()))
                    .findFirst()
                    .orElse(null);
            if (selectedSquare != null && selectedRectangles.contains(selectedSquare)) selectedRectangles.remove(selectedSquare);
            else if (selectedSquare != null) selectedRectangles.add(selectedSquare);
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        for (Rectangle square : rectangles) {
            g2d.setColor(Color.BLACK);
            if (selectedRectangles.contains(square)) g2d.setColor(Color.BLUE);
            g2d.draw(square); // Draw each square
        }

        if (startPoint != null && currentMousePosition != null) {
            // Preview the square being created
            g2d.setColor(Color.RED);
            int sideLength = Math.max(
                Math.abs(startPoint.x - currentMousePosition.x),
                Math.abs(startPoint.y - currentMousePosition.y)
            );
            int x = Math.min(startPoint.x, currentMousePosition.x);
            int y = Math.min(startPoint.y, currentMousePosition.y);
            g2d.drawRect(x, y, sideLength, sideLength);
        }
    }

    @Override
    public String toCSVString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Rectangle square : this.rectangles) {
            stringBuilder.append("Square,");
            stringBuilder.append(square.getX()).append(" ");
            stringBuilder.append(square.getY()).append(" ");
            stringBuilder.append(square.getWidth()).append(" ");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
