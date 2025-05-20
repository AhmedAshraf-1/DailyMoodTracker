package com.dailymoodtracker.repository;

import com.dailymoodtracker.model.User;

/**
 * Repository interface for User-related database operations
 */
public interface UserRepository {
    /**
     * Find a user by their username
     * 
     * @param username The username to search for
     * @return The user if found, null otherwise
     */
    User findByUsername(String username);
    
    /**
     * Save a user to the database
     * 
     * @param user The user to save
     * @return The saved user with updated ID
     */
    User save(User user);
    
    /**
     * Delete a user from the database
     * 
     * @param userId The ID of the user to delete
     * @return true if successful, false otherwise
     */
    boolean delete(int userId);
    
    /**
     * Update a user's information
     * 
     * @param user The user with updated information
     * @return The updated user
     */
    User update(User user);
} 