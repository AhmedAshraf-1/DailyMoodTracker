package com.dailymoodtracker.repository;

import com.dailymoodtracker.service.DatabaseService;
import com.dailymoodtracker.service.MySQLDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for creating repository instances.
 * Uses MySQL implementations by default.
 */
public class RepositoryFactory {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryFactory.class);
    private static final DatabaseService databaseService = MySQLDatabaseService.getInstance();
    
    // Singleton instances of repositories
    private static ChatMessageRepository chatMessageRepository;
    private static MoodEntryRepository moodEntryRepository;
    private static UserRepository userRepository;
    
    /**
     * Get the ChatMessageRepository instance.
     * @return ChatMessageRepository instance
     */
    public static synchronized ChatMessageRepository getChatMessageRepository() {
        if (chatMessageRepository == null) {
            chatMessageRepository = new MySQLChatMessageRepository(databaseService);
            logger.info("Created MySQL chat message repository");
        }
        return chatMessageRepository;
    }
    
    /**
     * Get the MoodEntryRepository instance.
     * @return MoodEntryRepository instance
     */
    public static synchronized MoodEntryRepository getMoodEntryRepository() {
        if (moodEntryRepository == null) {
            moodEntryRepository = new MySQLMoodEntryRepository(databaseService);
            logger.info("Created MySQL mood entry repository");
        }
        return moodEntryRepository;
    }
    
    /**
     * Get the user repository instance.
     * @return A UserRepository implementation
     */
    public static UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new MySQLUserRepository();
        }
        return userRepository;
    }
} 