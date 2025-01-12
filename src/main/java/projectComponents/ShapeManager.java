package projectComponents;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public abstract class ShapeManager {
    protected Point currentMousePosition = null;
    protected ActionMode currentMode = ActionMode.CREATE;
    protected Point startPoint = null;
    protected Point selectedPoint = null;

    public void setCurrentMode(ActionMode actionMode) {
        if (this.currentMode == ActionMode.CREATE && actionMode != ActionMode.CREATE) startPoint = null;
        this.currentMode = actionMode;
    }

    // Method for resetting any necessary state
    public void resetDrawing() {
        this.startPoint = null;
    }
    public abstract void resetSelection();
    public void newFrame() {
        startPoint = null;
        selectedPoint = null;
        currentMousePosition = null;
        currentMode = ActionMode.CREATE;
    }

    // Mouse event behaviors
    public void setCurrentMousePosition(MouseEvent e) {
        this.currentMousePosition = e.getPoint();
    }
    public abstract void setMouseClickedBehaviour(MouseEvent e);
    public void setMousePressedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.MOVE) {
            selectedPoint = e.getPoint();
        }
    }
    public abstract void setMouseDraggedBehaviour(MouseEvent e);
    public void setMouseReleasedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.MOVE) {
            selectedPoint = null;
        }
    }

    // Method for adding shapes
    public void addShape(List<Double> xValues, List<Double> yValues) {}

    // Method for painting shape(s)
    public abstract void paint(Graphics2D g2d);

    // Method for representing object as CSV strin
    public abstract String toCSVString();
}
