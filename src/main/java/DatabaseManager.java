import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private String dbName = "graphical_objects";
    private final String tableName = "object_description";
    private final String username = "root";
    private Connection connection;
    private boolean isConnected = false;

    // Establishes a connection to the database
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

    // Closes the database connection
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

    public void clearTable() {
        String clearTableSQL = "DELETE FROM " + tableName;
        try {
            connection.createStatement().executeUpdate(clearTableSQL);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    private String getDBurl(String dbName) {
        return "jdbc:mysql://localhost:3306/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
    }
}