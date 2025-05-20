package com.dailymoodtracker.repository;

import com.dailymoodtracker.model.ChatMessage;
import java.util.List;

/**
 * Repository interface for chat messages.
 * Simplified version for Watson integration.
 */
public interface ChatMessageRepository {
    
    /**
     * Save a chat message.
     * 
     * @param message the message to save
     */
    void save(ChatMessage message);
    
    /**
     * Find recent chat messages for a user.
     * 
     * @param userId the user ID
     * @param limit the maximum number of messages to return
     * @return a list of recent chat messages
     */
    List<ChatMessage> findRecentByUserId(int userId, int limit);
} 