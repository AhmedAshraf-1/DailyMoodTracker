package com.dailymoodtracker.service;

import com.dailymoodtracker.config.DatabaseConfig;
import com.dailymoodtracker.exception.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQL implementation of the DatabaseService interface.
 */
public class MySQLDatabaseService implements DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(MySQLDatabaseService.class);
    private static MySQLDatabaseService instance;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private MySQLDatabaseService() {
        // Private constructor for singleton
    }
    
    /**
     * Get the singleton instance of MySQLDatabaseService.
     * @return MySQLDatabaseService instance
     */
    public static synchronized MySQLDatabaseService getInstance() {
        if (instance == null) {
            instance = new MySQLDatabaseService();
        }
        return instance;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
    
    @Override
    public void closeConnection() {
        DatabaseConfig.closeConnection();
    }
    
    @Override
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            logger.debug("Executing SQL: {}", sql);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error executing update SQL: {}", sql, e);
            throw new DatabaseException("Error executing database update", e);
        }
    }
    
    @Override
    public int[] executeTransaction(String[] sqlStatements, Object[][] params) throws SQLException {
        Connection conn = null;
        int[] results = new int[sqlStatements.length];
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            for (int i = 0; i < sqlStatements.length; i++) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlStatements[i])) {
                    // Set parameters for this statement
                    if (params[i] != null) {
                        for (int j = 0; j < params[i].length; j++) {
                            stmt.setObject(j + 1, params[i][j]);
                        }
                    }
                    
                    results[i] = stmt.executeUpdate();
                }
            }
            
            conn.commit();
            return results;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    logger.warn("Transaction failed, rolling back", e);
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
            logger.error("Error executing transaction", e);
            throw new DatabaseException("Error executing database transaction", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Error resetting auto-commit", e);
                }
            }
        }
    }
    
    @Override
    public boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Error checking database connection", e);
            return false;
        }
    }
    
    @Override
    public void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection()) {
            // Database initialization is handled in the DatabaseConfig class
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
            throw new DatabaseException("Error initializing database", e);
        }
    }
} 