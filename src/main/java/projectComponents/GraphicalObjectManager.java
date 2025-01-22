package projectComponents;

import java.util.ArrayList;
import java.util.List;
/**
 * The `GraphicalObjectManager` class is responsible for managing a collection of `GraphicalObject` instances.
 * <p>
 * This class provides functionality to add graphical objects and retrieve the current list of managed objects.
 */
public class GraphicalObjectManager {
	  private List<GraphicalObject> objects = new ArrayList<>();
	  /**
	     * Adds a `GraphicalObject` to the manager's collection.
	     *
	     * @param object the `GraphicalObject` to be added.
	     * @return `true` if the object was successfully added, `false` otherwise.
	     */
	    public boolean addObject(GraphicalObject object) {
	        return objects.add(object);
	    }
	    /**
	     * Retrieves the list of all managed graphical objects.
	     *
	     * @return a `List` of `GraphicalObject` instances.
	     */
	    public List<GraphicalObject> getObjects() {
	        return objects;
	    }
}
