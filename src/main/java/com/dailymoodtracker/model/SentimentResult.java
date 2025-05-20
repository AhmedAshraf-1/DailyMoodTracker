package com.dailymoodtracker.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class to represent the result of sentiment analysis.
 */
public class SentimentResult {
    private double positiveScore;
    private double negativeScore;
    private double neutralScore;
    private String overallSentiment;
    private String messageText;
    private LocalDateTime timestamp;
    private int userId;
    
    // Added support for more specific emotions
    private String specificEmotion;
    private Map<String, Double> emotionScores;
    private String analysisSource;  // e.g., "openai", "watson", "dummy"
    private Map<String, String> metadata; // For storing additional analysis data

    public SentimentResult() {
        this.timestamp = LocalDateTime.now();
        this.emotionScores = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    public SentimentResult(String messageText, double positiveScore, double negativeScore, 
                          double neutralScore, String overallSentiment, int userId) {
        this.messageText = messageText;
        this.positiveScore = positiveScore;
        this.negativeScore = negativeScore;
        this.neutralScore = neutralScore;
        this.overallSentiment = overallSentiment;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
        this.emotionScores = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    public double getPositiveScore() {
        return positiveScore;
    }

    public void setPositiveScore(double positiveScore) {
        this.positiveScore = positiveScore;
    }

    public double getNegativeScore() {
        return negativeScore;
    }

    public void setNegativeScore(double negativeScore) {
        this.negativeScore = negativeScore;
    }

    public double getNeutralScore() {
        return neutralScore;
    }

    public void setNeutralScore(double neutralScore) {
        this.neutralScore = neutralScore;
    }

    public String getOverallSentiment() {
        return overallSentiment;
    }

    public void setOverallSentiment(String overallSentiment) {
        this.overallSentiment = overallSentiment;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
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
    
    /**
     * Get the specific emotion detected (e.g., happy, sad, angry, etc.)
     */
    public String getSpecificEmotion() {
        return specificEmotion;
    }
    
    /**
     * Set the specific emotion detected
     */
    public void setSpecificEmotion(String specificEmotion) {
        this.specificEmotion = specificEmotion;
    }
    
    /**
     * Get detailed emotion scores
     */
    public Map<String, Double> getEmotionScores() {
        return emotionScores;
    }
    
    /**
     * Set detailed emotion scores
     */
    public void setEmotionScores(Map<String, Double> emotionScores) {
        this.emotionScores = emotionScores;
    }
    
    /**
     * Add an emotion score
     */
    public void addEmotionScore(String emotion, double score) {
        this.emotionScores.put(emotion, score);
    }
    
    /**
     * Get the source of the sentiment analysis
     */
    public String getAnalysisSource() {
        return analysisSource;
    }
    
    /**
     * Set the source of the sentiment analysis
     */
    public void setAnalysisSource(String analysisSource) {
        this.analysisSource = analysisSource;
    }
    
    /**
     * Determine dominant sentiment based on scores
     */
    public String getDominantSentiment() {
        if (positiveScore > negativeScore && positiveScore > neutralScore) {
            return "positive";
        } else if (negativeScore > positiveScore && negativeScore > neutralScore) {
            return "negative";
        } else {
            return "neutral";
        }
    }
    
    /**
     * Get the highest score
     */
    public double getHighestScore() {
        return Math.max(Math.max(positiveScore, negativeScore), neutralScore);
    }
    
    /**
     * Get additional metadata from the sentiment analysis
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /**
     * Set additional metadata for the sentiment analysis
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Add a metadata entry
     */
    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * Get a specific metadata value
     */
    public String getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SentimentResult{");
        sb.append("positiveScore=").append(positiveScore);
        sb.append(", negativeScore=").append(negativeScore);
        sb.append(", neutralScore=").append(neutralScore);
        sb.append(", overallSentiment='").append(overallSentiment).append('\'');
        sb.append(", dominantSentiment='").append(getDominantSentiment()).append('\'');
        
        if (specificEmotion != null && !specificEmotion.isEmpty()) {
            sb.append(", specificEmotion='").append(specificEmotion).append('\'');
        }
        
        if (analysisSource != null && !analysisSource.isEmpty()) {
            sb.append(", source='").append(analysisSource).append('\'');
        }
        
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
} 