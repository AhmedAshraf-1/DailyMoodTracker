package com.dailymoodtracker.repository;

import com.dailymoodtracker.config.DatabaseConfig;
import com.dailymoodtracker.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * MySQL implementation of the UserRepository interface
 */
public class MySQLUserRepository implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(MySQLUserRepository.class);
    
    /**
     * Find a user by their username
     * 
     * @param username The username to search for
     * @return The user if found, null otherwise
     */
    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
        }
        
        return null;
    }
    
    /**
     * Save a user to the database
     * 
     * @param user The user to save
     * @return The saved user with updated ID
     */
    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (username, password, email, created_at) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving user: {}", user.getUsername(), e);
        }
        
        return null;
    }
    
    /**
     * Delete a user from the database
     * 
     * @param userId The ID of the user to delete
     * @return true if successful, false otherwise
     */
    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting user with ID: {}", userId, e);
            return false;
        }
    }
    
    /**
     * Update a user's information
     * 
     * @param user The user with updated information
     * @return The updated user
     */
    @Override
    public User update(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setInt(4, user.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                return user;
            }
        } catch (SQLException e) {
            logger.error("Error updating user: {}", user.getUsername(), e);
        }
        
        return null;
    }
    
    /**
     * Map a ResultSet to a User object
     * 
     * @param rs The ResultSet containing user data
     * @return The mapped User object
     * @throws SQLException if there is an error accessing the ResultSet
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User(rs.getString("username"), rs.getString("password"));
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return user;
    }
} 