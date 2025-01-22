package hska.javapractice.de;

import projectComponents.UserInputProcessor;
import projectComponents.GraphicalObjectManager;
import projectComponents.GraphicalObject;
/**
 * The `IntegrationTest` class serves as a test suite to verify the integration
 * of the `UserInputProcessor` and `GraphicalObjectManager` components.
 * <p>
 * This test checks:
 * <ul>
 *     <li>Whether the `UserInputProcessor` can correctly process user input to create graphical objects.</li>
 *     <li>Whether the `GraphicalObjectManager` can manage the created objects (e.g., adding and verifying objects).</li>
 * </ul>
 * The test is executed through the `main` method and provides console output for each step.
 */

public class IntegrationTest {
	/**
     * The entry point for the integration test.
     * <p>
     * This method performs the following:
     * <ul>
     *     <li>Simulates user input and processes it using the `UserInputProcessor`.</li>
     *     <li>Verifies that the processed input creates the expected graphical object (a Circle).</li>
     *     <li>Simulates adding the created object to the `GraphicalObjectManager`.</li>
     *     <li>Verifies that the object is correctly managed by the `GraphicalObjectManager`.</li>
     * </ul>
     *
     * @param args command-line arguments (not used in this test).
     */

	public static void main(String[] args) {
		System.out.println("Starting Integration Test...");

        // Simulating UserInputProcessor
        UserInputProcessor userInputProcessor = new UserInputProcessor();
        String userInput = "Create Circle";
        
        // Process user input to create a graphical object
        GraphicalObject circleObject = userInputProcessor.processInput(userInput);

        if (circleObject != null && "Circle".equals(circleObject.getType())) {
            System.out.println("Step 1 Passed: UserInputProcessor correctly created a Circle object.");
        } else {
            System.err.println("Step 1 Failed: UserInputProcessor failed to create a Circle object.");
        }

        // Simulating GraphicalObjectManager
        GraphicalObjectManager graphicalObjectManager = new GraphicalObjectManager();
        boolean added = graphicalObjectManager.addObject(circleObject);

        if (added && graphicalObjectManager.getObjects().contains(circleObject)) {
            System.out.println("Step 2 Passed: GraphicalObjectManager correctly added the Circle object.");
        } else {
            System.err.println("Step 2 Failed: GraphicalObjectManager failed to add the Circle object.");
        }

        System.out.println("Integration Test Completed.");

	}

}
