package org.example.bl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {
    private static Connection connection;
    public static void connectToDatabase(String dbUrl, String dsUsername, String dbPassword) {
        try {
            connection = DriverManager.getConnection(dbUrl, dsUsername, dbPassword);
            System.out.println("Connected to the database!");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: \n" + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            throw new NullPointerException("No connection to the database, connection is null");
        }
        return connection;
    }

    public static void shutdown() throws SQLException {
        getConnection().close();
    }
}
