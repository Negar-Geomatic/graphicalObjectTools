package projectComponents;
/**
 * The `GraphicalObject` class represents a basic graphical object with a specific type.
 * <p>
 * This class provides a foundation for managing objects in graphical applications by
 * associating them with a type that identifies their nature (e.g., Circle, Rectangle, etc.).
 */

public class GraphicalObject {
	private String type;// The type of the graphical object
	 /**
     * Constructs a new `GraphicalObject` with the specified type.
     *
     * @param type the type of the graphical object (e.g., "Circle", "Rectangle").
     */
    public GraphicalObject(String type) {
        this.type = type;
    }
    /**
     * Retrieves the type of this graphical object.
     *
     * @return the type of the graphical object as a `String`.
     */

    public String getType() {
        return type;
    }

}
