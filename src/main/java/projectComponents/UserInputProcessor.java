package projectComponents;
/**
 * The {@code UserInputProcessor} class processes user input strings to create corresponding {@code GraphicalObject} instances.
 * <p>
 * This class provides a simple mechanism for converting specific input commands into graphical objects.
 * </p>
 */

public class UserInputProcessor {
	/**
     * Processes the given user input and creates a corresponding {@code GraphicalObject}.
     * <p>
     * Supported commands:
     * <ul>
     *     <li>{@code "Create Circle"}: Creates a {@code GraphicalObject} with the type "Circle".</li>
     * </ul>
     * If the input does not match any supported commands, {@code null} is returned.
     * </p>
     *
     * @param input the input string from the user.
     * @return a {@code GraphicalObject} based on the input command, or {@code null} if the command is unsupported.
     */
	 public GraphicalObject processInput(String input) {
	        if ("Create Circle".equalsIgnoreCase(input)) {
	            return new GraphicalObject("Circle");
	        }
	        return null;
	    }
}
