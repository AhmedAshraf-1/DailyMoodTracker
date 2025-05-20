package com.dailymoodtracker.service;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for database operations.
 * Defines standard methods that any database implementation should provide.
 */
public interface DatabaseService {
    
    /**
     * Get a database connection.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    Connection getConnection() throws SQLException;
    
    /**
     * Close the database connection.
     */
    void closeConnection();
    
    /**
     * Execute a database query that doesn't return results (INSERT, UPDATE, DELETE).
     * @param sql SQL statement to execute
     * @param params Parameters for the SQL statement
     * @return Number of rows affected
     * @throws SQLException if query execution fails
     */
    int executeUpdate(String sql, Object... params) throws SQLException;
    
    /**
     * Execute a transaction with multiple SQL statements.
     * @param sqlStatements Array of SQL statements to execute
     * @param params 2D array of parameters for each SQL statement
     * @return Array with number of rows affected for each statement
     * @throws SQLException if transaction execution fails
     */
    int[] executeTransaction(String[] sqlStatements, Object[][] params) throws SQLException;
    
    /**
     * Check if the database is connected.
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Initialize the database schema if it doesn't exist.
     * @throws SQLException if initialization fails
     */
    void initializeDatabase() throws SQLException;
} 