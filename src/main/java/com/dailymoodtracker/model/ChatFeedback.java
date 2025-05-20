package com.dailymoodtracker.model;

import java.time.LocalDateTime;

/**
 * Model class for storing user feedback about chatbot responses.
 * This will be used for improving the chatbot's responses over time.
 */
public class ChatFeedback {
    
    public enum FeedbackType {
        POSITIVE,
        NEGATIVE,
        SUGGESTION
    }
    
    private Long id;
    private String userMessage;
    private String botResponse;
    private FeedbackType type;
    private String feedbackContent;
    private LocalDateTime timestamp;
    private int userId;
    private String sentimentType;
    private String specificEmotion;
    
    public ChatFeedback() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ChatFeedback(String userMessage, String botResponse, FeedbackType type, 
                        String feedbackContent, int userId) {
        this.userMessage = userMessage;
        this.botResponse = botResponse;
        this.type = type;
        this.feedbackContent = feedbackContent;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
    
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }
    
    public String getBotResponse() {
        return botResponse;
    }
    
    public void setBotResponse(String botResponse) {
        this.botResponse = botResponse;
    }
    
    public FeedbackType getType() {
        return type;
    }
    
    public void setType(FeedbackType type) {
        this.type = type;
    }
    
    public String getFeedbackContent() {
        return feedbackContent;
    }
    
    public void setFeedbackContent(String feedbackContent) {
        this.feedbackContent = feedbackContent;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getSentimentType() {
        return sentimentType;
    }
    
    public void setSentimentType(String sentimentType) {
        this.sentimentType = sentimentType;
    }
    
    public String getSpecificEmotion() {
        return specificEmotion;
    }
    
    public void setSpecificEmotion(String specificEmotion) {
        this.specificEmotion = specificEmotion;
    }
    
    @Override
    public String toString() {
        return "ChatFeedback{" +
                "id=" + id +
                ", type=" + type +
                ", sentiment=" + sentimentType +
                ", emotion=" + specificEmotion +
                ", timestamp=" + timestamp +
                '}';
    }
} 