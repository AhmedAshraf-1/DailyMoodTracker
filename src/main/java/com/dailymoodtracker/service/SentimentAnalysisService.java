package com.dailymoodtracker.service;

import com.dailymoodtracker.model.SentimentResult;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for sentiment analysis services.
 */
public interface SentimentAnalysisService {
    
    /**
     * Analyzes the sentiment of a text synchronously.
     * 
     * @param text The text to analyze
     * @param userId The ID of the user who wrote the text
     * @return The sentiment analysis result
     */
    SentimentResult analyzeSentiment(String text, int userId);
    
    /**
     * Analyzes the sentiment of a text asynchronously.
     * 
     * @param text The text to analyze
     * @param userId The ID of the user who wrote the text
     * @return A future that will complete with the sentiment analysis result
     */
    CompletableFuture<SentimentResult> analyzeSentimentAsync(String text, int userId);
    
    /**
     * Gets an appropriate bot response based on sentiment analysis.
     * 
     * @param sentiment The sentiment analysis result
     * @return A contextually appropriate response
     */
    String getBotResponse(SentimentResult sentiment);
} 