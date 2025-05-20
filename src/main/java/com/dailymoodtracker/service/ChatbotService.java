package com.dailymoodtracker.service;

import com.dailymoodtracker.model.ChatMessage;
import com.dailymoodtracker.model.SentimentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling chatbot functionality.
 */
public class ChatbotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    
    private final SentimentAnalysisService sentimentService;
    private final ChatTrainingService trainingService;
    private final Random random = new Random();
    
    // Track last response to avoid repetition
    private String lastResponse = "";
    
    // Recent conversation history (for context)
    private final List<ChatMessage> recentMessages = new ArrayList<>();
    private static final int MAX_RECENT_MESSAGES = 10;
    
    // Responses for different topics
    private static final List<String> GREETING_RESPONSES = Arrays.asList(
        "Hello! How are you feeling today?",
        "Hi there! How is your day going?",
        "Greetings! How's your mood right now?",
        "Hello! It's nice to chat with you. How are you doing today?",
        "Hi! Thanks for checking in. How has your day been so far?"
    );
    
    private static final List<String> MOOD_INQUIRY_RESPONSES = Arrays.asList(
        "Would you like to tell me more about how you're feeling?",
        "I'm here to listen. What's on your mind?",
        "Would you like to record your current mood in the tracker?",
        "I'd love to hear more about your emotions right now.",
        "Feel free to share whatever is on your mind. I'm here to listen."
    );
    
    private static final List<String> POSITIVE_RESPONSES = Arrays.asList(
        "That's wonderful to hear! What's making you feel good today?",
        "I'm glad you're feeling positive! Would you like to record this in your mood tracker?",
        "That's great! What activities have contributed to your good mood?",
        "It's fantastic that you're feeling this way! What's bringing you joy?",
        "I'm happy to hear you're doing well! Would you like to reflect on what's helping you feel positive?"
    );
    
    private static final List<String> NEGATIVE_RESPONSES = Arrays.asList(
        "I'm sorry to hear that. Would you like to talk about what's bothering you?",
        "That sounds difficult. Remember that tracking your moods can help identify patterns.",
        "I understand. Would recording your feelings in the mood tracker help?",
        "It can be tough when you're feeling this way. Is there anything specific that's troubling you?",
        "I'm here for you during these challenging feelings. Would it help to explore them a bit more?"
    );
    
    private static final List<String> NEUTRAL_RESPONSES = Arrays.asList(
        "I see. Is there anything specific you'd like to discuss today?",
        "Thanks for sharing. Would you like to add this to your mood tracking?",
        "I understand. Is there anything I can help you with today?",
        "Thanks for letting me know. What's been on your mind today?",
        "I appreciate you sharing that. Would you like to talk about anything in particular?"
    );
    
    private static final List<String> FALLBACK_RESPONSES = Arrays.asList(
        "I'm not sure I understand. Could you tell me more?",
        "I'd like to help you better. Can you explain what you mean?",
        "I'm still learning. Could you phrase that differently?",
        "I want to be helpful, but I need a bit more information. Could you elaborate?",
        "I might need more context to give you a good response. Can you provide more details?"
    );
    
    // Map of different greeting variations
    private static final Map<String, List<String>> GREETING_PATTERNS = new HashMap<>();
    
    static {
        GREETING_PATTERNS.put("hello", Arrays.asList("hello", "hi", "hey", "hiya", "howdy", "greetings"));
        GREETING_PATTERNS.put("good_time", Arrays.asList("good morning", "good afternoon", "good evening", "good day"));
        GREETING_PATTERNS.put("whatsup", Arrays.asList("what's up", "whats up", "wassup", "what up", "sup"));
        GREETING_PATTERNS.put("howareyou", Arrays.asList("how are you", "how r u", "how're you", "how you doing", "how's it going"));
    }
    
    public ChatbotService() {
        this.sentimentService = SentimentServiceFactory.getService();
        this.trainingService = ChatTrainingService.getInstance();
    }
    
    /**
     * Process a user message and generate a response.
     * 
     * @param userMessage the user's message
     * @return the chatbot's response
     */
    public ChatMessage processMessage(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return new ChatMessage(getRandomResponse(FALLBACK_RESPONSES), ChatMessage.MessageType.BOT);
        }
        
        // Create and store user message
        ChatMessage userChatMessage = new ChatMessage(userMessage, ChatMessage.MessageType.USER);
        addToRecentMessages(userChatMessage);
        
        String lowerMessage = userMessage.toLowerCase().trim();
        
        // Check for greetings
        if (isGreeting(lowerMessage)) {
            String response = getNonRepeatingResponse(GREETING_RESPONSES);
            ChatMessage botMessage = new ChatMessage(response, ChatMessage.MessageType.BOT);
            addToRecentMessages(botMessage);
            return botMessage;
        }
        
        // Simple messages that might not need sentiment analysis
        if (lowerMessage.length() < 5) {
            // For very short messages that might not give good sentiment results
            String response = getNonRepeatingResponse(MOOD_INQUIRY_RESPONSES);
            ChatMessage botMessage = new ChatMessage(response, ChatMessage.MessageType.BOT);
            addToRecentMessages(botMessage);
            return botMessage;
        }
        
        // Analyze sentiment
        SentimentResult sentiment = sentimentService.analyzeSentiment(userMessage, 1); // Default user ID
        
        // Set sentiment for user message
        userChatMessage.setSentiment(sentiment);
        
        // Try to generate an enhanced response using the training service
        CompletableFuture<String> enhancedResponseFuture = 
            trainingService.generateImprovedResponse(userMessage, recentMessages, sentiment);
        
        String response;
        try {
            // Wait up to 5 seconds for an enhanced response
            String enhancedResponse = enhancedResponseFuture.get(5, TimeUnit.SECONDS);
            
            if (enhancedResponse != null && !enhancedResponse.isEmpty()) {
                // Use enhanced response from GPT
                response = enhancedResponse;
                logger.info("Using enhanced GPT response");
            } else {
                // Fall back to regular sentiment-based response
                response = sentimentService.getBotResponse(sentiment);
                logger.info("Using standard sentiment-based response");
            }
        } catch (Exception e) {
            // If anything goes wrong with enhanced response, fall back to standard
            logger.warn("Error getting enhanced response: {}", e.getMessage());
            response = sentimentService.getBotResponse(sentiment);
        }
        
        // Log for debugging
        logger.debug("Generated response for sentiment {}: {}", sentiment.getDominantSentiment(), response);
        
        // Check if response is empty or the same as the last time
        if (response == null || response.isEmpty() || response.equals(lastResponse)) {
            response = getResponseBasedOnSentiment(sentiment);
        }
        
        // Store last response to avoid repetition
        lastResponse = response;
        
        // Create bot message
        ChatMessage botMessage = new ChatMessage(response, ChatMessage.MessageType.BOT);
        botMessage.setRelatedSentiment(sentiment);
        
        // Add to recent messages
        addToRecentMessages(botMessage);
        
        // Record conversation for training
        trainingService.recordConversation(userMessage, response, sentiment);
        
        return botMessage;
    }
    
    /**
     * Add a message to recent messages, maintaining maximum size.
     */
    private void addToRecentMessages(ChatMessage message) {
        recentMessages.add(message);
        if (recentMessages.size() > MAX_RECENT_MESSAGES) {
            recentMessages.remove(0);
        }
    }
    
    /**
     * Check if the message is a greeting.
     */
    private boolean isGreeting(String message) {
        // Direct word match
        for (List<String> patterns : GREETING_PATTERNS.values()) {
            for (String pattern : patterns) {
                if (message.equals(pattern) || message.startsWith(pattern + " ")) {
                    return true;
                }
            }
        }
        
        // If the message is very short, check for partial matches
        if (message.length() < 10) {
            for (List<String> patterns : GREETING_PATTERNS.values()) {
                for (String pattern : patterns) {
                    if (message.contains(pattern)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get a response based on the detected sentiment.
     */
    private String getResponseBasedOnSentiment(SentimentResult sentiment) {
        String dominantSentiment = sentiment.getDominantSentiment();
        
        if ("positive".equals(dominantSentiment)) {
            return getNonRepeatingResponse(POSITIVE_RESPONSES);
        } else if ("negative".equals(dominantSentiment)) {
            return getNonRepeatingResponse(NEGATIVE_RESPONSES);
        } else {
            return getNonRepeatingResponse(NEUTRAL_RESPONSES);
        }
    }
    
    /**
     * Get a random response from a list that's different from the last response.
     */
    private String getNonRepeatingResponse(List<String> responses) {
        if (responses.size() <= 1) {
            return responses.get(0);
        }
        
        String response;
        do {
            response = responses.get(random.nextInt(responses.size()));
        } while (response.equals(lastResponse) && responses.size() > 1);
        
        return response;
    }
    
    /**
     * Get a random response from a list.
     */
    private String getRandomResponse(List<String> responses) {
        return responses.get(random.nextInt(responses.size()));
    }
} 