import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 * The `DatabaseManager` class handles interactions with a MySQL database.
 * It provides methods to establish connections, insert and retrieve data,
 * and manage a table for graphical object descriptions.
 */

public class DatabaseManager {
    private String dbName = "graphical_objects";
    private final String tableName = "object_description";
    private final String username = "root";
    private Connection connection;
    private boolean isConnected = false;

    /**
     * Establishes a connection to the database for exporting data.
     * If the specified database does not exist, it creates the database and initializes the table.
     *
     * @param dbName   the name of the database to connect to (if null or empty, a default name is used).
     * @param password the password for the database user.
     */
    public void exportConnect(String dbName, String password) {
        if (dbName != null && (!dbName.trim().isEmpty())) this.dbName = dbName;
        else this.dbName = "graphical_objects";
        
        try {
            String jdbcUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true";
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            String sqlCreateDB = "CREATE DATABASE IF NOT EXISTS " + this.dbName;
            connection.createStatement().executeUpdate(sqlCreateDB);

            connection = DriverManager.getConnection(getDBurl(this.dbName), username, password);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + this.tableName + " (" +
                                "type VARCHAR(15) NOT NULL, " + 
                                "description TEXT NOT NULL" +
                                ");";
            connection.createStatement().executeUpdate(createTableSQL);
            isConnected = true;
        } catch (SQLException e) {
            connection = null;
            isConnected = false;
        }
    }
    /**
     * Establishes a connection to the database for importing data.
     *
     * @param dbName   the name of the database to connect to (if null or empty, a default name is used).
     * @param password the password for the database user.
     */

    public void importConnect(String dbName, String password) {
        if (dbName != null && (!dbName.trim().isEmpty())) this.dbName = dbName;
        else this.dbName = "graphical_objects";
        
        try {
            connection = DriverManager.getConnection(getDBurl(this.dbName), username, password);

            isConnected = true;
        } catch (SQLException e) {
            connection = null;
            isConnected = false;
        }
    }
    /**
     * Closes the current database connection, if any.
     * Ensures proper resource cleanup.
     */
    public void abortConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                this.isConnected = false;
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }
    /**
     * Inserts object data into the database.
     * Each line of the input string represents an object with its type and description.
     *
     * @param objectInfo a string containing object data in the format "type,description" per line.
     */
    public void insertObject(String objectInfo) {
        String[] lines = objectInfo.trim().split("\n");
        for (String line : lines) {
            try {
                String[] data = line.trim().split(",");
                String type = data[0].trim();
                String description = data[1].trim();

                String sqlInsert = "INSERT INTO " + tableName + " (type, description) VALUES (?, ?)";
                PreparedStatement statement = connection.prepareStatement(sqlInsert);
                statement.setString(1, type);
                statement.setString(2, description);
    
                statement.executeUpdate();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }
    /**
     * Retrieves all objects from the database table.
     *
     * @return a list of strings, where each string represents an object in the format "type,description".
     */

    public List<String> getAllObjects() {
        List<String> objects = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + tableName;

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String type = resultSet.getString("type");
                String description = resultSet.getString("description");

                String objectInfo = String.format("%s,%s", type, description);
                objects.add(objectInfo);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        return objects;
    }
    /**
     * Clears all data from the database table.
     */

    public void clearTable() {
        String clearTableSQL = "DELETE FROM " + tableName;
        try {
            connection.createStatement().executeUpdate(clearTableSQL);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
    /**
     * Checks if the database is currently connected.
     *
     * @return true if connected, false otherwise.
     */

    public boolean isConnected() {
        return this.isConnected;
    }
    /**
     * Constructs the URL for connecting to a specific database.
     *
     * @param dbName the name of the database.
     * @return the JDBC URL for the database.
     */

    private String getDBurl(String dbName) {
        return "jdbc:mysql://localhost:3306/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
    }
}