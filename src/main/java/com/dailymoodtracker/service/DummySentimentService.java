package com.dailymoodtracker.service;

import com.dailymoodtracker.model.SentimentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A dummy implementation of SentimentAnalysisService for testing
 * without actual API credentials. Uses keyword matching
 * and random generation to simulate sentiment analysis.
 */
public class DummySentimentService implements SentimentAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(DummySentimentService.class);
    
    private static DummySentimentService instance;
    private final Map<String, String> botResponses;
    private final Random random = new Random();
    private final ScheduledExecutorService executor;
    
    // Positive keywords
    private static final String[] POSITIVE_WORDS = {
        "happy", "good", "great", "excellent", "wonderful", "love", "joy",
        "excited", "amazing", "fantastic", "delighted", "glad", "pleased"
    };
    
    // Negative keywords
    private static final String[] NEGATIVE_WORDS = {
        "sad", "bad", "terrible", "awful", "horrible", "hate", "disappointed",
        "upset", "angry", "depressed", "worried", "anxious", "stressed"
    };
    
    private DummySentimentService() {
        botResponses = initializeBotResponses();
        executor = Executors.newScheduledThreadPool(1);
        logger.info("Dummy sentiment service initialized (for testing without API keys)");
    }
    
    public static synchronized DummySentimentService getInstance() {
        if (instance == null) {
            instance = new DummySentimentService();
        }
        return instance;
    }
    
    /**
     * Initialize the bot responses for different sentiment types
     */
    private Map<String, String> initializeBotResponses() {
        Map<String, String> responses = new HashMap<>();
        
        // Positive sentiment responses
        responses.put("positive_high", "I'm thrilled to hear you're feeling so positive! What's been going especially well for you?");
        responses.put("positive_medium", "That sounds really good. It's nice to hear positive things from you.");
        responses.put("positive_low", "I can sense some positivity in your message. Would you like to share more about it?");
        
        // Negative sentiment responses
        responses.put("negative_high", "I'm really sorry to hear you're feeling this way. Would it help to talk more about what's troubling you?");
        responses.put("negative_medium", "That sounds challenging. I'm here to listen if you want to talk more about these feelings.");
        responses.put("negative_low", "I notice you might be feeling a bit down. Is there anything specific on your mind?");
        
        // Neutral sentiment responses
        responses.put("neutral_high", "Thank you for sharing that with me. How else can I support you today?");
        responses.put("neutral_medium", "I see. Would you like to tell me more about that?");
        responses.put("neutral_low", "I understand. Is there anything specific you'd like to discuss?");
        
        // Fallback response
        responses.put("fallback", "I'm here to listen. How can I help you today?");
        
        return responses;
    }

    @Override
    public SentimentResult analyzeSentiment(String text, int userId) {
        if (text == null || text.trim().isEmpty()) {
            return createDefaultSentimentResult("", userId);
        }
        
        // Convert text to lowercase for keyword matching
        String lowerText = text.toLowerCase();
        
        // Count positive and negative keywords
        int positiveMatches = 0;
        int negativeMatches = 0;
        
        for (String word : POSITIVE_WORDS) {
            if (lowerText.contains(word)) {
                positiveMatches++;
            }
        }
        
        for (String word : NEGATIVE_WORDS) {
            if (lowerText.contains(word)) {
                negativeMatches++;
            }
        }
        
        // Calculate sentiment scores
        double positiveScore;
        double negativeScore;
        double neutralScore;
        String overallSentiment;
        
        // Base scores on keyword matches plus random factor
        if (positiveMatches > 0 || negativeMatches > 0) {
            double positiveBase = 0.3 * positiveMatches / POSITIVE_WORDS.length;
            double negativeBase = 0.3 * negativeMatches / NEGATIVE_WORDS.length;
            
            // Add random component to simulate complex analysis
            positiveScore = positiveBase + (random.nextDouble() * 0.2);
            negativeScore = negativeBase + (random.nextDouble() * 0.2);
            
            // Ensure neutralScore is calculated so all add up to 1.0
            neutralScore = 1.0 - (positiveScore + negativeScore);
            if (neutralScore < 0) {
                // Normalize if needed
                double total = positiveScore + negativeScore;
                positiveScore = positiveScore / total;
                negativeScore = negativeScore / total;
                neutralScore = 0;
            }
        } else {
            // No keywords found, generate balanced random scores
            neutralScore = 0.4 + (random.nextDouble() * 0.3); // 0.4-0.7
            double remainder = 1.0 - neutralScore;
            positiveScore = remainder * random.nextDouble();
            negativeScore = remainder - positiveScore;
        }
        
        // Determine overall sentiment
        if (positiveScore > negativeScore && positiveScore > neutralScore) {
            overallSentiment = "positive";
        } else if (negativeScore > positiveScore && negativeScore > neutralScore) {
            overallSentiment = "negative";
        } else {
            overallSentiment = "neutral";
        }
        
        logger.debug("Dummy sentiment analysis: positive={}, negative={}, neutral={}, overall={}",
            positiveScore, negativeScore, neutralScore, overallSentiment);
            
        return new SentimentResult(text, positiveScore, negativeScore, neutralScore, 
            overallSentiment, userId);
    }

    @Override
    public CompletableFuture<SentimentResult> analyzeSentimentAsync(String text, int userId) {
        // Add a small delay to simulate network request
        CompletableFuture<SentimentResult> future = new CompletableFuture<>();
        
        executor.schedule(() -> {
            try {
                SentimentResult result = analyzeSentiment(text, userId);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, 300 + random.nextInt(700), TimeUnit.MILLISECONDS);
        
        return future;
    }
    
    @Override
    public String getBotResponse(SentimentResult sentiment) {
        if (sentiment == null) {
            return botResponses.get("fallback");
        }
        
        String dominantSentiment = sentiment.getDominantSentiment();
        double score = sentiment.getHighestScore();
        
        // Determine intensity level
        String intensity;
        if (score > 0.7) {
            intensity = "high";
        } else if (score > 0.4) {
            intensity = "medium";
        } else {
            intensity = "low";
        }
        
        // Construct response key
        String responseKey = dominantSentiment + "_" + intensity;
        
        // Return appropriate response or fallback
        return botResponses.getOrDefault(responseKey, botResponses.get("fallback"));
    }
    
    /**
     * Creates a default sentiment result when analysis fails
     */
    private SentimentResult createDefaultSentimentResult(String text, int userId) {
        return new SentimentResult(text, 0.1, 0.1, 0.8, "neutral", userId);
    }
    
    /**
     * Shutdown the service gracefully
     */
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
} 