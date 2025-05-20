package com.dailymoodtracker.service;

import com.dailymoodtracker.model.SentimentResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;
import java.util.List;

/**
 * Local implementation of sentiment analysis service without Python dependency.
 * This is a simplified version that uses keyword matching similar to the Python service.
 */
public class PythonSentimentService implements SentimentAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(PythonSentimentService.class);
    
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();
    
    // Singleton instance
    private static PythonSentimentService instance;
    
    // Emotion keyword mappings
    private final Map<String, List<String>> emotionKeywords = new HashMap<>();
    private final Map<String, String> therapyApproaches = new HashMap<>();
    private final Map<String, String> conversationNeeds = new HashMap<>();
    
    // Bot response templates
    private final Map<String, List<String>> sentimentResponses = new HashMap<>();
    private final Map<String, String> emotionResponses = new HashMap<>();
    
    /**
     * Get the singleton instance of PythonSentimentService.
     * @return PythonSentimentService instance
     */
    public static synchronized PythonSentimentService getInstance() {
        if (instance == null) {
            instance = new PythonSentimentService();
        }
        return instance;
    }
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private PythonSentimentService() {
        this.executorService = Executors.newCachedThreadPool();
        this.objectMapper = new ObjectMapper();
        
        // Initialize emotion keywords
        initializeEmotionKeywords();
        initializeTherapyApproaches();
        initializeConversationNeeds();
        initializeResponses();
        
        // Log initialization
        logger.info("Local sentiment analysis service initialized successfully");
    }
    
    private void initializeEmotionKeywords() {
        emotionKeywords.put("joy", Arrays.asList(
            "happy", "joy", "delighted", "excited", "pleased", "glad", "content",
            "wonderful", "great", "amazing", "awesome", "excellent", "fantastic"
        ));
        
        emotionKeywords.put("sadness", Arrays.asList(
            "sad", "unhappy", "depressed", "gloomy", "miserable", "heartbroken",
            "down", "blue", "somber", "melancholy", "grief", "sorrow"
        ));
        
        emotionKeywords.put("anger", Arrays.asList(
            "angry", "mad", "furious", "irritated", "annoyed", "frustrated",
            "rage", "hate", "upset", "bitter", "enraged", "outraged"
        ));
        
        emotionKeywords.put("fear", Arrays.asList(
            "afraid", "scared", "terrified", "anxious", "worried", "nervous",
            "frightened", "horror", "panic", "dread", "concern", "stress"
        ));
        
        emotionKeywords.put("surprise", Arrays.asList(
            "surprised", "shocked", "amazed", "astonished", "stunned", "unexpected",
            "startled", "wow", "whoa", "unexpected", "disbelief"
        ));
        
        emotionKeywords.put("confusion", Arrays.asList(
            "confused", "perplexed", "puzzled", "uncertain", "unsure", "doubtful",
            "bewildered", "lost", "disoriented", "unclear", "ambiguous"
        ));
        
        emotionKeywords.put("gratitude", Arrays.asList(
            "grateful", "thankful", "appreciative", "blessed", "fortunate", "appreciate",
            "thanks", "blessing", "gratitude", "indebted"
        ));
        
        emotionKeywords.put("hope", Arrays.asList(
            "hopeful", "optimistic", "looking forward", "eager", "anticipate", "wish",
            "dream", "expect", "bright future", "promising"
        ));
    }
    
    private void initializeTherapyApproaches() {
        therapyApproaches.put("joy", "Positive reinforcement and appreciation of current positive state");
        therapyApproaches.put("sadness", "Empathetic listening and validation of feelings");
        therapyApproaches.put("anger", "Validation and safe expression of emotions");
        therapyApproaches.put("fear", "Grounding techniques and reassurance");
        therapyApproaches.put("surprise", "Processing and making meaning of unexpected events");
        therapyApproaches.put("confusion", "Clarification and providing structure");
        therapyApproaches.put("gratitude", "Savoring positive experiences and building on strengths");
        therapyApproaches.put("hope", "Goal-setting and future-oriented thinking");
        therapyApproaches.put("neutral", "Open-ended exploration of experiences");
    }
    
    private void initializeConversationNeeds() {
        conversationNeeds.put("joy", "Celebrate successes and savor positive emotions");
        conversationNeeds.put("sadness", "Provide comfort and space for expressing feelings");
        conversationNeeds.put("anger", "Acknowledge feelings and explore underlying causes");
        conversationNeeds.put("fear", "Offer reassurance and coping strategies");
        conversationNeeds.put("surprise", "Help process unexpected information");
        conversationNeeds.put("confusion", "Provide clarity and organize thoughts");
        conversationNeeds.put("gratitude", "Expand awareness of positive aspects");
        conversationNeeds.put("hope", "Encourage optimism while being realistic");
        conversationNeeds.put("neutral", "General exploration of thoughts and feelings");
    }
    
    private void initializeResponses() {
        // Sentiment-based responses
        sentimentResponses.put("positive", Arrays.asList(
            "I'm glad to hear you're feeling positive! What's contributing to these good feelings?",
            "That sounds wonderful! Would you like to share more about what's making you feel this way?",
            "It's great that you're in a positive mood. How can we build on these good feelings?",
            "I'm happy to hear that! What other positive things have been happening for you?"
        ));
        
        sentimentResponses.put("negative", Arrays.asList(
            "I'm sorry to hear you're not feeling your best. Would you like to talk more about what's going on?",
            "That sounds challenging. What support would be most helpful for you right now?",
            "I can understand why that might be difficult. How have you been coping with these feelings?",
            "Thank you for sharing those feelings with me. Is there anything specific you'd like to focus on today?"
        ));
        
        sentimentResponses.put("neutral", Arrays.asList(
            "Thank you for sharing. How else have you been feeling lately?",
            "I appreciate your thoughts. Is there anything specific on your mind today?",
            "Thank you for expressing that. What would you like to talk about next?",
            "I understand. Is there any particular area of your life you'd like to discuss?"
        ));
        
        // Specific emotion responses
        emotionResponses.put("joy", "It's wonderful to hear you're feeling joy! What's bringing you happiness right now?");
        emotionResponses.put("sadness", "I'm sorry you're feeling sad. It's okay to feel this way, and I'm here to listen.");
        emotionResponses.put("anger", "I can hear that you're feeling angry. That's a valid emotion - would it help to talk about what triggered it?");
        emotionResponses.put("fear", "It sounds like you're experiencing some fear or anxiety. Would it help to explore what's causing these feelings?");
        emotionResponses.put("surprise", "That seems quite surprising! How are you processing this unexpected situation?");
        emotionResponses.put("confusion", "It seems like you might be feeling uncertain or confused. Let's try to bring some clarity together.");
        emotionResponses.put("gratitude", "I love that you're expressing gratitude. Appreciating the positive things can be so powerful.");
        emotionResponses.put("hope", "It's great to hear that hopeful tone in your message. What are you looking forward to?");
    }
    
    @Override
    public SentimentResult analyzeSentiment(String text, int userId) {
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Cannot analyze sentiment: empty text");
            return createDefaultSentimentResult(text, userId);
        }
        
        try {
            logger.debug("Analyzing sentiment for text: {}", text.length() > 50 ? text.substring(0, 47) + "..." : text);
            
            // Convert to lowercase for keyword matching
            String lowerText = text.toLowerCase();
            
            // Count positive and negative words
            int positiveCount = 0;
            int negativeCount = 0;
            
            // Check for positive words (simplified approach)
            List<String> positiveWords = Arrays.asList(
                "good", "happy", "great", "excellent", "wonderful", "love", "joy",
                "excited", "amazing", "fantastic", "delighted", "glad", "pleased"
            );
            
            for (String word : positiveWords) {
                if (lowerText.contains(word)) {
                    positiveCount++;
                }
            }
            
            // Check for negative words
            List<String> negativeWords = Arrays.asList(
                "bad", "sad", "terrible", "awful", "horrible", "hate", "disappointed",
                "upset", "angry", "depressed", "worried", "anxious", "stressed"
            );
            
            for (String word : negativeWords) {
                if (lowerText.contains(word)) {
                    negativeCount++;
                }
            }
            
            // Calculate scores
            double positiveScore;
            double negativeScore; 
            double neutralScore;
            
            if (positiveCount > 0 || negativeCount > 0) {
                // Calculate base scores from keyword counts
                positiveScore = Math.min(0.1 + (positiveCount * 0.1), 0.9);
                negativeScore = Math.min(0.1 + (negativeCount * 0.1), 0.9);
                
                // Add some randomness for variety (like the Python version)
                positiveScore += (random.nextDouble() * 0.1) - 0.05;
                negativeScore += (random.nextDouble() * 0.1) - 0.05;
                
                // Ensure scores are within bounds
                positiveScore = Math.max(0.05, Math.min(0.95, positiveScore));
                negativeScore = Math.max(0.05, Math.min(0.95, negativeScore));
                
                // Calculate neutral score
                neutralScore = Math.max(0.1, 1.0 - (positiveScore + negativeScore));
                
                // Normalize to ensure they sum to 1.0
                double total = positiveScore + negativeScore + neutralScore;
                positiveScore = positiveScore / total;
                negativeScore = negativeScore / total;
                neutralScore = neutralScore / total;
            } else {
                // Default to mostly neutral if no keywords found
                neutralScore = 0.7;
                positiveScore = 0.15;
                negativeScore = 0.15;
            }
            
            // Determine dominant sentiment
            String dominantSentiment;
            if (positiveScore > negativeScore && positiveScore > neutralScore) {
                dominantSentiment = "positive";
            } else if (negativeScore > positiveScore && negativeScore > neutralScore) {
                dominantSentiment = "negative";
            } else {
                dominantSentiment = "neutral";
            }
            
            // Determine specific emotion
            String specificEmotion = analyzeSpecificEmotion(lowerText);
            
            // Calculate emotional intensity (0.3-0.9)
            double emotionalIntensity = 0.5;
            if (!specificEmotion.equals("neutral")) {
                // More intense if more keywords matched
                int totalEmotionKeywords = positiveCount + negativeCount;
                emotionalIntensity = Math.min(0.3 + (totalEmotionKeywords * 0.05), 0.9);
            }
            
            // Get therapeutic approach and conversation needs
            String therapeuticApproach = therapyApproaches.getOrDefault(specificEmotion, 
                    therapyApproaches.get("neutral"));
            
            String conversationNeed = conversationNeeds.getOrDefault(specificEmotion,
                    conversationNeeds.get("neutral"));
            
            // Create the sentiment result
            SentimentResult result = new SentimentResult(text, positiveScore, negativeScore, neutralScore, 
                    dominantSentiment, userId);
            
            // Add specific emotion if available
            if (!specificEmotion.equals("neutral")) {
                result.setSpecificEmotion(specificEmotion);
            }
            
            // Add enhanced sentiment data
            result.addEmotionScore("intensity", emotionalIntensity);
            
            // Set analysis source
            result.setAnalysisSource("local");
            
            // Store therapeutic approach and conversation needs
            Map<String, String> metadata = new HashMap<>();
            metadata.put("therapeutic_approach", therapeuticApproach);
            metadata.put("conversation_needs", conversationNeed);
            result.setMetadata(metadata);
            
            logger.info("Sentiment analysis complete: {} ({}), intensity: {}", 
                    dominantSentiment, specificEmotion, emotionalIntensity);
            return result;
            
        } catch (Exception e) {
            logger.error("Error analyzing sentiment: {}", e.getMessage(), e);
            return createDefaultSentimentResult(text, userId);
        }
    }
    
    /**
     * Analyze text for specific emotions using keyword matching.
     */
    private String analyzeSpecificEmotion(String text) {
        // Count emotion keywords
        Map<String, Integer> emotionCounts = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : emotionKeywords.entrySet()) {
            String emotion = entry.getKey();
            List<String> keywords = entry.getValue();
            
            int count = 0;
            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    count++;
                }
            }
            
            emotionCounts.put(emotion, count);
        }
        
        // Find the emotion with the most matches
        String dominantEmotion = "neutral";
        int maxCount = 0;
        
        for (Map.Entry<String, Integer> entry : emotionCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantEmotion = entry.getKey();
            }
        }
        
        // If no emotions detected or very weak signal, return neutral
        if (maxCount < 1) {
            return "neutral";
        }
        
        return dominantEmotion;
    }
    
    @Override
    public CompletableFuture<SentimentResult> analyzeSentimentAsync(String text, int userId) {
        return CompletableFuture.supplyAsync(() -> analyzeSentiment(text, userId), executorService);
    }
    
    @Override
    public String getBotResponse(SentimentResult sentiment) {
        if (sentiment == null) {
            return "I'm not sure how to respond to that. Could you tell me more?";
        }
        
        try {
            String dominantSentiment = sentiment.getDominantSentiment();
            String specificEmotion = sentiment.getSpecificEmotion();
            
            // First check if we have a response for the specific emotion
            if (specificEmotion != null && emotionResponses.containsKey(specificEmotion)) {
                return emotionResponses.get(specificEmotion);
            }
            
            // Otherwise, use the sentiment-based response
            List<String> responses = sentimentResponses.getOrDefault(dominantSentiment, 
                    sentimentResponses.get("neutral"));
            
            // Pick a random response
            return responses.get(random.nextInt(responses.size()));
            
        } catch (Exception e) {
            logger.error("Error getting bot response: {}", e.getMessage(), e);
            return getFallbackResponse(sentiment);
        }
    }
    
    /**
     * Creates a default sentiment result when analysis fails
     */
    private SentimentResult createDefaultSentimentResult(String text, int userId) {
        // Default to neutral when analysis fails
        return new SentimentResult(text, 0.1, 0.1, 0.8, "neutral", userId);
    }
    
    /**
     * Returns a fallback response when we can't get a response from the service
     */
    private String getFallbackResponse(SentimentResult sentiment) {
        String dominantSentiment = sentiment.getDominantSentiment();
        
        if ("positive".equalsIgnoreCase(dominantSentiment)) {
            return "I'm glad to hear you're feeling positive! How can I help maintain that good energy?";
        } else if ("negative".equalsIgnoreCase(dominantSentiment)) {
            return "I'm sorry to hear you're not feeling great. Would it help to talk more about what's going on?";
        } else {
            return "I'm here to listen. Is there something specific you'd like to discuss today?";
        }
    }
    
    /**
     * Cleanly shut down resources
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            logger.info("Sentiment service executor shut down");
        }
    }
    
    /**
     * Check if the service is available (always returns true for local implementation)
     */
    public boolean isServiceAvailable() {
        return true;
    }
} 