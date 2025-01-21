package hska.javapractice.de;

import projectComponents.UserInputProcessor;
import projectComponents.GraphicalObjectManager;
import projectComponents.GraphicalObject;


public class IntegrationTest {

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
