package com.dailymoodtracker.service;

import com.dailymoodtracker.model.SentimentResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of SentimentAnalysisService that connects to the Python Flask service.
 */
public class HttpSentimentService implements SentimentAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(HttpSentimentService.class);
    
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;
    
    // Service URL
    private final String serviceBaseUrl;
    private final HttpClient httpClient;
    
    // Singleton instance
    private static HttpSentimentService instance;
    
    /**
     * Get the singleton instance of HttpSentimentService.
     * @return HttpSentimentService instance
     */
    public static synchronized HttpSentimentService getInstance() {
        if (instance == null) {
            instance = new HttpSentimentService();
        }
        return instance;
    }
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private HttpSentimentService() {
        this.executorService = Executors.newCachedThreadPool();
        this.objectMapper = new ObjectMapper();
        this.serviceBaseUrl = "http://localhost:8080"; // Default URL
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        
        logger.info("HTTP Sentiment Analysis Service initialized with URL: {}", serviceBaseUrl);
    }
    
    @Override
    public SentimentResult analyzeSentiment(String text, int userId) {
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Cannot analyze sentiment: empty text");
            return createDefaultSentimentResult(text, userId);
        }
        
        try {
            logger.debug("Analyzing sentiment using HTTP service for text: {}", 
                    text.length() > 50 ? text.substring(0, 47) + "..." : text);
            
            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);
            requestBody.put("user_id", userId);
            
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            
            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serviceBaseUrl + "/analyze"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Handle response
            if (response.statusCode() == 200) {
                return parseResponse(response.body(), text, userId);
            } else {
                logger.error("Error from sentiment service: HTTP {}", response.statusCode());
                return createDefaultSentimentResult(text, userId);
            }
            
        } catch (Exception e) {
            logger.error("Error analyzing sentiment via HTTP: {}", e.getMessage(), e);
            return createDefaultSentimentResult(text, userId);
        }
    }
    
    private SentimentResult parseResponse(String responseBody, String text, int userId) throws IOException {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        
        // Extract sentiment scores
        double positiveScore = rootNode.path("positive_score").asDouble();
        double negativeScore = rootNode.path("negative_score").asDouble();
        double neutralScore = rootNode.path("neutral_score").asDouble();
        String dominantSentiment = rootNode.path("dominant_sentiment").asText();
        
        // Create basic sentiment result
        SentimentResult result = new SentimentResult(text, positiveScore, negativeScore, neutralScore, 
                dominantSentiment, userId);
        
        // Add specific emotion if available
        if (rootNode.has("specific_emotion")) {
            String specificEmotion = rootNode.path("specific_emotion").asText();
            result.setSpecificEmotion(specificEmotion);
        }
        
        // Add emotional intensity if available
        if (rootNode.has("emotional_intensity")) {
            double intensity = rootNode.path("emotional_intensity").asDouble();
            result.addEmotionScore("intensity", intensity);
        }
        
        // Set analysis source
        result.setAnalysisSource("python_service");
        
        // Store therapeutic approach and conversation needs if available
        Map<String, String> metadata = new HashMap<>();
        if (rootNode.has("therapeutic_approach")) {
            metadata.put("therapeutic_approach", rootNode.path("therapeutic_approach").asText());
        }
        if (rootNode.has("conversation_needs")) {
            metadata.put("conversation_needs", rootNode.path("conversation_needs").asText());
        }
        result.setMetadata(metadata);
        
        logger.info("Sentiment analysis complete via HTTP: {} ({}), intensity: {}", 
                dominantSentiment, 
                result.getSpecificEmotion() != null ? result.getSpecificEmotion() : "unknown",
                result.getEmotionScores().getOrDefault("intensity", 0.0));
                
        return result;
    }
    
    @Override
    public CompletableFuture<SentimentResult> analyzeSentimentAsync(String text, int userId) {
        return CompletableFuture.supplyAsync(() -> analyzeSentiment(text, userId), executorService);
    }
    
    @Override
    public String getBotResponse(SentimentResult sentiment) {
        try {
            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dominant_sentiment", sentiment.getDominantSentiment());
            if (sentiment.getSpecificEmotion() != null) {
                requestBody.put("specific_emotion", sentiment.getSpecificEmotion());
            }
            
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            
            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serviceBaseUrl + "/bot_response"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Parse response
            if (response.statusCode() == 200) {
                JsonNode responseNode = objectMapper.readTree(response.body());
                if (responseNode.has("response")) {
                    return responseNode.get("response").asText();
                }
            }
            
            logger.error("Failed to get bot response from service: HTTP {}", response.statusCode());
            return getFallbackResponse(sentiment);
            
        } catch (Exception e) {
            logger.error("Error getting bot response: {}", e.getMessage(), e);
            return getFallbackResponse(sentiment);
        }
    }
    
    private String getFallbackResponse(SentimentResult sentiment) {
        String dominantSentiment = sentiment.getDominantSentiment();
        
        if ("positive".equalsIgnoreCase(dominantSentiment)) {
            return "I'm glad to hear you're feeling positive! What's contributing to these good feelings?";
        } else if ("negative".equalsIgnoreCase(dominantSentiment)) {
            return "I'm sorry to hear you're not feeling your best. Would you like to talk more about what's going on?";
        } else {
            return "Thank you for sharing. How else have you been feeling lately?";
        }
    }
    
    private SentimentResult createDefaultSentimentResult(String text, int userId) {
        return new SentimentResult(text, 0.1, 0.1, 0.8, "neutral", userId);
    }
    
    /**
     * Shutdown the service.
     */
    public void shutdown() {
        executorService.shutdown();
        logger.info("HTTP Sentiment Analysis Service shutdown complete");
    }
    
    /**
     * Check if the service is available.
     * @return true if service is available
     */
    public boolean isServiceAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serviceBaseUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            logger.warn("Service unavailable: {}", e.getMessage());
            return false;
        }
    }
} 