package projectComponents;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public abstract class ShapeManager {
    protected Point currentMousePosition = null;
    protected ActionMode currentMode = ActionMode.CREATE;
    protected Point startPoint = null;
    protected Point selectedPoint = null;

    /**
     * Sets the current action mode
     * 
     * @param actionMode the current action mode
     */
    public void setCurrentMode(ActionMode actionMode) {
        if (this.currentMode == ActionMode.CREATE && actionMode != ActionMode.CREATE) startPoint = null;
        this.currentMode = actionMode;
    }

    /**
     * resets drawing that has been already drawn
     */
    public void resetDrawing() {
        this.startPoint = null;
    }
    
    /**
     * resets the selected shapes
     */
    public abstract void resetSelection();
    
    /**
     * resets the frame
     */
    public void newFrame() {
        startPoint = null;
        selectedPoint = null;
        currentMousePosition = null;
        currentMode = ActionMode.CREATE;
    }

    /**
     * sets the current mouse position
     * 
     * @param e the mouse event
     */
    public void setCurrentMousePosition(MouseEvent e) {
        this.currentMousePosition = e.getPoint();
    }
    
    /**
     * sets up the mouse behaviour in case mouse clicked
     * 
     * @param e the mouse event
     */
    public abstract void setMouseClickedBehaviour(MouseEvent e);
    
    /**
     * sets up the mouse behaviour in case mouse pressed
     * 
     * @param e the mouse event
     */
    public void setMousePressedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.MOVE) {
            selectedPoint = e.getPoint();
        }
    }
    
    /**
     * sets up the mouse behaviour in case mouse dragged
     * 
     * @param e the mouse event
     */
    public abstract void setMouseDraggedBehaviour(MouseEvent e);
    
    /**
     * sets up the mouse behaviour in case mouse released
     * 
     * @param e the mouse event
     */
    public void setMouseReleasedBehaviour(MouseEvent e) {
        if (currentMode == ActionMode.MOVE) {
            selectedPoint = null;
        }
    }

    /**
     * adds shapes based on xValues and yValues
     * 
     * @param xValues the x coordinates
     * @param yValues the y coordinates
     */
    public void addShape(List<Double> xValues, List<Double> yValues) {}

    /**
     * paints a shape
     * 
     * @param g2d the graphical drawer
     */
    public abstract void paint(Graphics2D g2d);

    /**
     * converts shape(s) to .csv
     *  
     * @return the string of shape description
     */
    public abstract String toCSVString();
}
