package com.dailymoodtracker.service;

import com.dailymoodtracker.model.User;
import com.dailymoodtracker.repository.UserRepository;
import com.dailymoodtracker.repository.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static UserService instance;
    private final UserRepository userRepository;
    
    private UserService() {
        this.userRepository = RepositoryFactory.getUserRepository();
    }
    
    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    public User registerUser(String username, String password) {
        // Check if user already exists
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            logger.warn("User already exists: {}", username);
            return null;
        }
        
        // Create and save new user
        User user = new User(username, password);
        logger.info("Registering new user: {}", username);
        return userRepository.save(user);
    }
    
    public User registerUser(String username, String email, String password) {
        // Check if user already exists
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            logger.warn("User already exists: {}", username);
            return null;
        }
        
        // Create and save new user with email
        User user = new User(username, password);
        user.setEmail(email);
        logger.info("Registering new user with email: {}", username);
        return userRepository.save(user);
    }
    
    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            logger.info("User authenticated: {}", username);
            return user;
        }
        
        logger.warn("Authentication failed for user: {}", username);
        return null;
    }
    
    public User getUser(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User updateUser(User user) {
        logger.info("Updating user: {}", user.getUsername());
        return userRepository.update(user);
    }
} 