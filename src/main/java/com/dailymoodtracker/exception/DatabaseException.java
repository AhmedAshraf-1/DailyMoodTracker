package com.dailymoodtracker.exception;

/**
 * Exception for database-related errors.
 */
public class DatabaseException extends RuntimeException {
    
    /**
     * Create a new DatabaseException with a message.
     * 
     * @param message the error message
     */
    public DatabaseException(String message) {
        super(message);
    }
    
    /**
     * Create a new DatabaseException with a message and cause.
     * 
     * @param message the error message
     * @param cause the cause of the exception
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Create a new DatabaseException with a cause.
     * 
     * @param cause the cause of the exception
     */
    public DatabaseException(Throwable cause) {
        super(cause);
    }
} 