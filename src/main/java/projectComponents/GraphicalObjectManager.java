package projectComponents;

import java.util.ArrayList;
import java.util.List;
public class GraphicalObjectManager {
	  private List<GraphicalObject> objects = new ArrayList<>();

	    public boolean addObject(GraphicalObject object) {
	        return objects.add(object);
	    }

	    public List<GraphicalObject> getObjects() {
	        return objects;
	    }
}
