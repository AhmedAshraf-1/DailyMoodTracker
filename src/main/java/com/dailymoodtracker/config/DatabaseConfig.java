package com.dailymoodtracker.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for database connections.
 * Uses H2 embedded database for easier setup.
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    // Database connection details
    private static final String DB_TYPE = "h2";
    
    // H2 embedded database settings - file based for persistence
    private static final String DB_FILE_PATH = "./data/mood_tracker";
    
    private static Connection connection;
    
    /**
     * Get a database connection.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        
        try {
            // Ensure data directory exists
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            // Delete lock file if it exists (to avoid locking issues)
            File lockFile = new File(DB_FILE_PATH + ".lock.db");
            if (lockFile.exists()) {
                lockFile.delete();
                logger.info("Deleted database lock file");
            }
            
            connection = createConnection();
            initDatabase();
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to create database connection", e);
            throw e;
        }
    }
    
    /**
     * Create a database connection based on the configured database type.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    private static Connection createConnection() throws SQLException {
        switch (DB_TYPE.toLowerCase()) {
            case "h2":
                return createH2Connection();
            default:
                throw new SQLException("Unsupported database type: " + DB_TYPE);
        }
    }
    
    /**
     * Create an H2 embedded database connection.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    private static Connection createH2Connection() throws SQLException {
        try {
            // Load H2 JDBC driver
            Class.forName("org.h2.Driver");
            
            // Create connection to file-based H2 database
            String jdbcUrl = "jdbc:h2:file:" + DB_FILE_PATH + 
                    ";DB_CLOSE_DELAY=-1" +
                    ";DB_CLOSE_ON_EXIT=FALSE" +
                    ";FILE_LOCK=NO" +
                    ";LOCK_TIMEOUT=10000" +
                    ";WRITE_DELAY=0";
            logger.info("Connecting to H2 database: {}", jdbcUrl);
            return DriverManager.getConnection(jdbcUrl, "sa", "");
        } catch (ClassNotFoundException e) {
            logger.error("H2 JDBC driver not found", e);
            throw new SQLException("H2 JDBC driver not found", e);
        }
    }
    
    /**
     * Initialize the database schema if it doesn't exist.
     * @throws SQLException if initialization fails
     */
    private static void initDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "username VARCHAR(255) UNIQUE NOT NULL," +
                         "password VARCHAR(255) NOT NULL," +
                         "email VARCHAR(255)," +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Create MoodEntries table
            stmt.execute("CREATE TABLE IF NOT EXISTS mood_entries (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "user_id INT NOT NULL," +
                         "mood_level INT NOT NULL," +
                         "notes TEXT," +
                         "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                         "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // Create Activities table
            stmt.execute("CREATE TABLE IF NOT EXISTS activities (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "activity_name VARCHAR(255) NOT NULL," +
                         "category VARCHAR(100) NOT NULL)");
            
            // Create MoodActivities junction table (for many-to-many relationship)
            stmt.execute("CREATE TABLE IF NOT EXISTS mood_activities (" +
                         "mood_entry_id INT NOT NULL," +
                         "activity_id INT NOT NULL," +
                         "PRIMARY KEY (mood_entry_id, activity_id)," +
                         "FOREIGN KEY (mood_entry_id) REFERENCES mood_entries(id)," +
                         "FOREIGN KEY (activity_id) REFERENCES activities(id))");
            
            // Create Goals table
            stmt.execute("CREATE TABLE IF NOT EXISTS goals (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "user_id INT NOT NULL," +
                         "description TEXT NOT NULL," +
                         "creation_date DATE NOT NULL," +
                         "completion_date DATE," +
                         "completed BOOLEAN DEFAULT FALSE," +
                         "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // Create Reminders table
            stmt.execute("CREATE TABLE IF NOT EXISTS reminders (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "user_id INT NOT NULL," +
                         "title VARCHAR(255) NOT NULL," +
                         "message TEXT," +
                         "time TIME NOT NULL," +
                         "days_of_week VARCHAR(50)," +
                         "active BOOLEAN DEFAULT TRUE," +
                         "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // Create UserPreferences table
            stmt.execute("CREATE TABLE IF NOT EXISTS user_preferences (" +
                         "user_id INT PRIMARY KEY," +
                         "theme VARCHAR(50) NOT NULL," +
                         "accent_color VARCHAR(50) NOT NULL," +
                         "mood_scale INT NOT NULL," +
                         "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // Create chat_messages table for the chatbot
            stmt.execute("CREATE TABLE IF NOT EXISTS chat_messages (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "user_id INT NOT NULL," +
                         "sender VARCHAR(50) NOT NULL," +
                         "content TEXT NOT NULL," +
                         "sentiment VARCHAR(50)," +
                         "positive_score DOUBLE," +
                         "negative_score DOUBLE," +
                         "neutral_score DOUBLE," +
                         "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                         "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            logger.info("Database schema initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database schema", e);
            throw e;
        }
    }
    
    /**
     * Close the database connection.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }
} 