package com.dailymoodtracker.service;

import com.dailymoodtracker.config.OpenAIConfig;
import com.dailymoodtracker.model.ChatFeedback;
import com.dailymoodtracker.model.ChatMessage;
import com.dailymoodtracker.model.SentimentResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for training the chatbot with conversation data and user feedback.
 * This service uses OpenAI's GPT models to generate better responses based on 
 * previous conversations and feedback.
 */
public class ChatTrainingService {
    private static final Logger logger = LoggerFactory.getLogger(ChatTrainingService.class);
    
    private final OpenAIConfig config;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;
    private final AtomicInteger apiCallCounter = new AtomicInteger(0);
    
    // Store conversation history (limited to last 20 conversations for memory efficiency)
    private final List<Map<String, Object>> conversationHistory = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 20;
    
    // Store feedback data for training
    private final List<ChatFeedback> feedbackData = new ArrayList<>();
    private static final int MAX_FEEDBACK_SIZE = 50;
    
    // Singleton instance
    private static ChatTrainingService instance;
    
    /**
     * Get the singleton instance of ChatTrainingService.
     * @return ChatTrainingService instance
     */
    public static synchronized ChatTrainingService getInstance() {
        if (instance == null) {
            instance = new ChatTrainingService();
        }
        return instance;
    }
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private ChatTrainingService() {
        this.config = OpenAIConfig.getInstance();
        this.executorService = Executors.newCachedThreadPool();
        this.objectMapper = new ObjectMapper();
        
        logger.info("Chat training service initialized");
    }
    
    /**
     * Record a conversation for later training.
     * 
     * @param userMessage The user's message
     * @param botResponse The bot's response
     * @param sentiment The sentiment analysis result
     */
    public void recordConversation(String userMessage, String botResponse, SentimentResult sentiment) {
        if (userMessage == null || botResponse == null) {
            return;
        }
        
        Map<String, Object> conversationEntry = new HashMap<>();
        conversationEntry.put("userMessage", userMessage);
        conversationEntry.put("botResponse", botResponse);
        
        if (sentiment != null) {
            conversationEntry.put("dominantSentiment", sentiment.getDominantSentiment());
            conversationEntry.put("specificEmotion", sentiment.getSpecificEmotion());
            
            // Add emotion scores if available
            Map<String, Double> emotionScores = sentiment.getEmotionScores();
            if (emotionScores != null && !emotionScores.isEmpty()) {
                conversationEntry.put("emotionScores", new HashMap<>(emotionScores));
            }
            
            // Add metadata if available
            if (sentiment.getMetadata() != null && !sentiment.getMetadata().isEmpty()) {
                conversationEntry.put("metadata", new HashMap<>(sentiment.getMetadata()));
            }
        }
        
        // Add to history, maintaining maximum size
        synchronized (conversationHistory) {
            conversationHistory.add(conversationEntry);
            if (conversationHistory.size() > MAX_HISTORY_SIZE) {
                conversationHistory.remove(0);
            }
        }
        
        logger.debug("Recorded conversation for training: '{}' -> '{}'", 
            userMessage.length() > 30 ? userMessage.substring(0, 27) + "..." : userMessage,
            botResponse.length() > 30 ? botResponse.substring(0, 27) + "..." : botResponse);
    }
    
    /**
     * Add user feedback about a bot response.
     * 
     * @param feedback The feedback data
     */
    public void addFeedback(ChatFeedback feedback) {
        if (feedback == null) {
            return;
        }
        
        synchronized (feedbackData) {
            feedbackData.add(feedback);
            if (feedbackData.size() > MAX_FEEDBACK_SIZE) {
                feedbackData.remove(0);
            }
        }
        
        logger.info("Recorded user feedback: {}", feedback);
    }
    
    /**
     * Generate an improved response based on conversation history and feedback.
     * 
     * @param userMessage The current user message
     * @param previousMessages Recent messages in the conversation (if any)
     * @param sentiment The sentiment analysis of the user message
     * @return An improved response
     */
    public CompletableFuture<String> generateImprovedResponse(
            String userMessage, 
            List<ChatMessage> previousMessages,
            SentimentResult sentiment) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if OpenAI API is configured
                if (!config.isConfigValid()) {
                    return null; // Let the regular chatbot handle it
                }
                
                // Check API call limit
                if (apiCallCounter.get() >= config.getDailyLimit()) {
                    logger.warn("Daily API call limit reached: {}", config.getDailyLimit());
                    return null;
                }
                
                // Prepare the request
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("model", config.getModel());
                requestBody.put("temperature", 0.7); // A bit of creativity
                requestBody.put("max_tokens", config.getMaxTokens());
                
                ArrayNode messagesArray = requestBody.putArray("messages");
                
                // System message with instructions
                ObjectNode systemMessage = objectMapper.createObjectNode();
                systemMessage.put("role", "system");
                systemMessage.put("content", buildSystemPrompt(sentiment));
                messagesArray.add(systemMessage);
                
                // Add conversation context from history
                addConversationContext(messagesArray, previousMessages);
                
                // Add recent feedback data if available
                addFeedbackContext(messagesArray);
                
                // Add the user's current message
                ObjectNode userMessageNode = objectMapper.createObjectNode();
                userMessageNode.put("role", "user");
                userMessageNode.put("content", userMessage);
                messagesArray.add(userMessageNode);
                
                // Call the OpenAI API
                String responseJson = callOpenAI(requestBody.toString());
                if (responseJson == null || responseJson.isEmpty()) {
                    return null;
                }
                
                // Parse the response
                JsonNode responseNode = objectMapper.readTree(responseJson);
                JsonNode choicesNode = responseNode.path("choices");
                
                if (choicesNode.isArray() && choicesNode.size() > 0) {
                    JsonNode firstChoice = choicesNode.get(0);
                    JsonNode messageNode = firstChoice.path("message");
                    
                    if (messageNode.has("content")) {
                        String content = messageNode.get("content").asText();
                        
                        // Log success
                        logger.info("Generated improved response using ChatGPT");
                        return content;
                    }
                }
                
                logger.warn("Failed to parse response from OpenAI API");
                return null;
                
            } catch (Exception e) {
                logger.error("Error generating improved response: {}", e.getMessage(), e);
                return null;
            }
        }, executorService);
    }
    
    /**
     * Build the system prompt for OpenAI based on sentiment and training data.
     */
    private String buildSystemPrompt(SentimentResult sentiment) {
        StringBuilder prompt = new StringBuilder(config.getSystemResponsePrompt());
        
        // Add information about the user's current sentiment
        if (sentiment != null) {
            prompt.append("\n\nThe user's current message has been analyzed as ")
                .append(sentiment.getDominantSentiment());
            
            if (sentiment.getSpecificEmotion() != null && !sentiment.getSpecificEmotion().isEmpty()) {
                prompt.append(" with a specific emotion of ").append(sentiment.getSpecificEmotion());
            }
            
            // Add therapeutic approach if available
            String therapeuticApproach = sentiment.getMetadataValue("therapeutic_approach");
            if (therapeuticApproach != null && !therapeuticApproach.isEmpty()) {
                prompt.append(".\n\nRecommended therapeutic approach: ").append(therapeuticApproach);
            }
            
            // Add conversation needs if available
            String conversationNeeds = sentiment.getMetadataValue("conversation_needs");
            if (conversationNeeds != null && !conversationNeeds.isEmpty()) {
                prompt.append(".\nConversation needs: ").append(conversationNeeds);
            }
            
            prompt.append(".");
        }
        
        // Add general response guidance
        prompt.append("\n\nYour response should be empathetic, supportive, and helpful. " +
            "Avoid being repetitive or generic. Respond directly to what the user has shared " +
            "and provide thoughtful, personalized insights or gentle guidance when appropriate.");
        
        return prompt.toString();
    }
    
    /**
     * Add conversation context to the request.
     */
    private void addConversationContext(ArrayNode messagesArray, List<ChatMessage> previousMessages) {
        // First add recent messages from the current conversation
        if (previousMessages != null && !previousMessages.isEmpty()) {
            // Limit to last 5 messages to keep context manageable
            int startIndex = Math.max(0, previousMessages.size() - 5);
            
            for (int i = startIndex; i < previousMessages.size(); i++) {
                ChatMessage message = previousMessages.get(i);
                ObjectNode messageNode = objectMapper.createObjectNode();
                
                if (message.getType() == ChatMessage.MessageType.USER) {
                    messageNode.put("role", "user");
                } else {
                    messageNode.put("role", "assistant");
                }
                
                messageNode.put("content", message.getContent());
                messagesArray.add(messageNode);
            }
        }
    }
    
    /**
     * Add feedback context to the request.
     */
    private void addFeedbackContext(ArrayNode messagesArray) {
        if (feedbackData.isEmpty()) {
            return;
        }
        
        // Add a system message with feedback summaries
        StringBuilder feedbackSummary = new StringBuilder("Recent user feedback about your responses:\n");
        
        synchronized (feedbackData) {
            // Only use the last few pieces of feedback
            int startIndex = Math.max(0, feedbackData.size() - 3);
            
            for (int i = startIndex; i < feedbackData.size(); i++) {
                ChatFeedback feedback = feedbackData.get(i);
                feedbackSummary.append("\nUser message: \"").append(feedback.getUserMessage()).append("\"\n");
                feedbackSummary.append("Your response: \"").append(feedback.getBotResponse()).append("\"\n");
                feedbackSummary.append("Feedback type: ").append(feedback.getType()).append("\n");
                feedbackSummary.append("Feedback content: \"").append(feedback.getFeedbackContent()).append("\"\n");
                
                if (feedback.getSentimentType() != null) {
                    feedbackSummary.append("Sentiment: ").append(feedback.getSentimentType());
                    
                    if (feedback.getSpecificEmotion() != null) {
                        feedbackSummary.append(" (").append(feedback.getSpecificEmotion()).append(")");
                    }
                    
                    feedbackSummary.append("\n");
                }
                
                feedbackSummary.append("---\n");
            }
        }
        
        feedbackSummary.append("\nPlease use this feedback to improve your responses.");
        
        ObjectNode feedbackNode = objectMapper.createObjectNode();
        feedbackNode.put("role", "system");
        feedbackNode.put("content", feedbackSummary.toString());
        messagesArray.add(feedbackNode);
    }
    
    /**
     * Call the OpenAI API.
     */
    private String callOpenAI(String requestBody) throws IOException {
        URL url = new URL(config.getApiUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
        connection.setDoOutput(true);
        
        // Increment API call counter
        apiCallCounter.incrementAndGet();
        
        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Read response
        StringBuilder response = new StringBuilder();
        int responseCode = connection.getResponseCode();
        
        if (responseCode != 200) {
            // Handle error response
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            
            logger.error("API request failed with code {}: {}", responseCode, response.toString());
            return null;
        }
        
        // Handle success response
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        return response.toString();
    }
    
    /**
     * Get the current API call count.
     */
    public int getApiCallCount() {
        return apiCallCounter.get();
    }
    
    /**
     * Reset the API call counter.
     */
    public void resetApiCallCounter() {
        apiCallCounter.set(0);
    }
    
    /**
     * Clean up resources when the service is no longer needed.
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 