package projectComponents;
/**
 * The `ActionMode` enum represents the various modes of interaction available for graphical objects.
 * <p>
 * These modes define the type of operation that can be performed on graphical objects in the application.
 */
public enum ActionMode {
	/**
     * The `CREATE` mode is used to create new graphical objects.
     */
    CREATE,
    /**
     * The `SELECT` mode is used to select existing graphical objects for further operations.
     */
    SELECT,
    /**
     * The `DELETE` mode is used to delete selected graphical objects.
     */
    DELETE,
    /**
     * The `MOVE` mode is used to move selected graphical objects to a new position.
     */
    MOVE
}
