package com.dailymoodtracker.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class to represent a chat message in the chatbot.
 */
public class ChatMessage {
    public enum MessageType {
        USER, BOT
    }
    
    private String content;
    private LocalDateTime timestamp;
    private MessageType type;
    private SentimentResult sentiment;
    private SentimentResult relatedSentiment;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public ChatMessage(String content, MessageType type) {
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }
    
    public ChatMessage(String content, MessageType type, SentimentResult sentiment) {
        this(content, type);
        this.sentiment = sentiment;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
    
    public SentimentResult getSentiment() {
        return sentiment;
    }
    
    public void setSentiment(SentimentResult sentiment) {
        this.sentiment = sentiment;
    }
    
    public SentimentResult getRelatedSentiment() {
        return relatedSentiment;
    }
    
    public void setRelatedSentiment(SentimentResult relatedSentiment) {
        this.relatedSentiment = relatedSentiment;
    }
    
    /**
     * Checks if this message has sentiment analysis results
     */
    public boolean hasSentiment() {
        return sentiment != null;
    }
    
    /**
     * Checks if this message has related sentiment analysis results (for bot responses)
     */
    public boolean hasRelatedSentiment() {
        return relatedSentiment != null;
    }
    
    /**
     * Get formatted timestamp for display
     */
    public String getFormattedTime() {
        return timestamp.format(TIME_FORMATTER);
    }
    
    /**
     * Returns CSS style class based on message type
     */
    public String getStyleClass() {
        return type == MessageType.USER ? "user-message" : "bot-message";
    }
    
    /**
     * Returns emoji based on sentiment
     */
    public String getSentimentEmoji() {
        if (sentiment == null) {
            return "";
        }
        
        switch (sentiment.getDominantSentiment()) {
            case "positive": return "😊";
            case "negative": return "😔";
            case "neutral": return "😐";
            default: return "";
        }
    }
} 