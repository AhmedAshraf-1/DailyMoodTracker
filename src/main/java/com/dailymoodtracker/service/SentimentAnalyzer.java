package com.dailymoodtracker.service;

import com.dailymoodtracker.model.SentimentResult;

/**
 * Interface for sentiment analysis.
 */
public interface SentimentAnalyzer {
    
    /**
     * Analyze the sentiment of the given text.
     * 
     * @param text the text to analyze
     * @param userId the ID of the user who wrote the text
     * @return the sentiment analysis result
     */
    SentimentResult analyzeSentiment(String text, int userId);
} 