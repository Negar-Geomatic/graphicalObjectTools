package projectComponents;

public class UserInputProcessor {
	 public GraphicalObject processInput(String input) {
	        if ("Create Circle".equalsIgnoreCase(input)) {
	            return new GraphicalObject("Circle");
	        }
	        return null;
	    }
}
