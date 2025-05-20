package com.dailymoodtracker.controller;

import com.dailymoodtracker.model.ChatMessage;
import com.dailymoodtracker.model.SentimentResult;
import com.dailymoodtracker.model.User;
import com.dailymoodtracker.repository.ChatMessageRepository;
import com.dailymoodtracker.repository.RepositoryFactory;
import com.dailymoodtracker.service.SentimentAnalysisService;
import com.dailymoodtracker.service.SentimentServiceFactory;
import com.dailymoodtracker.ui.ChatFeedbackDialog;
import com.dailymoodtracker.controller.DashboardController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.lang.reflect.Method;

/**
 * Controller class for the chatbot interface.
 */
public class ChatbotController {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);
    
    @FXML private ListView<ChatMessage> chatListView;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Button dashboardButton;
    @FXML private Button historyButton;
    @FXML private Button settingsButton;
    @FXML private Button recordMoodButton;
    @FXML private Button viewMoodHistoryButton;
    @FXML private Button gratitudeJournalButton;
    @FXML private Button breathingExerciseButton;
    @FXML private Button groundingTechniqueButton;
    @FXML private Button positiveAffirmationButton;
    @FXML private Button thoughtReframingButton;
    @FXML private Button selfCareChecklistButton;
    @FXML private Button wellnessActivitiesButton;
    @FXML private Button crisisResourcesButton;
    @FXML private Button feelingAnxiousButton;
    @FXML private Button feelingSadButton;
    @FXML private Button needBreathingButton;
    @FXML private Button needGratitudeButton;
    @FXML private Label dominantEmotionLabel;
    @FXML private Label supportReminderLabel;
    
    // These UI elements are in the sidebar
    @FXML private ProgressBar positiveBar;
    @FXML private ProgressBar neutralBar;
    @FXML private ProgressBar negativeBar;
    @FXML private Label positiveLabel;
    @FXML private Label neutralLabel;
    @FXML private Label negativeLabel;
    
    @FXML private Button feedbackButton;
    
    private int currentUserId = 1; // Default user ID
    private final SentimentAnalysisService sentimentService;
    private final ChatMessageRepository chatMessageRepository;
    private final ObservableList<ChatMessage> chatMessages;
    private final Random random = new Random();
    
    private ChatMessage lastUserMessage;
    private ChatMessage lastBotMessage;
    
    // Suggestion prompts based on sentiment
    private final List<String> positiveActivities = Arrays.asList(
        "journaling about what you're grateful for", 
        "sharing your positive experiences with a friend", 
        "physical exercise to boost your mood even further",
        "creative activities like drawing or music",
        "mindfulness meditation to savor this positive state"
    );
    
    private final List<String> neutralActivities = Arrays.asList(
        "a short walk outside", 
        "listening to your favorite music", 
        "reading a book that interests you",
        "trying a new hobby",
        "deep breathing exercises"
    );
    
    private final List<String> negativeActivities = Arrays.asList(
        "gentle physical activity like yoga", 
        "talking to a trusted friend or family member", 
        "mindfulness meditation",
        "journaling your feelings",
        "practicing self-compassion exercises"
    );
    
    // Enhanced response templates for various emotional states
    private final List<String> anxietyResponses = Arrays.asList(
        "I hear that you're feeling anxious. That's a really challenging emotion to experience. Would it help to try a brief breathing exercise together?",
        "Anxiety can feel overwhelming. Remember that these feelings are temporary and will pass. What specific worries are on your mind right now?",
        "I'm sorry you're feeling anxious. Your feelings are valid, and you're not alone in this experience. Would talking about what's triggering this help?",
        "When anxiety appears, it often brings physical sensations with it. Would you like to try a quick grounding technique to help feel more centered?",
        "It takes courage to acknowledge feeling anxious. Would focusing on something specific that's causing worry help, or would a distraction be better right now?"
    );
    
    private final List<String> sadnessResponses = Arrays.asList(
        "I'm really sorry you're feeling sad. It's okay to feel this way, and your emotions are valid. Would you like to talk more about what's happening?",
        "Sadness is a natural emotion, though it can be painful. Be gentle with yourself today. What might bring you a small moment of comfort?",
        "Thank you for sharing that you're feeling sad. That takes courage. Is there something specific that triggered these feelings?",
        "I hear you're feeling sad. Sometimes just acknowledging those feelings can be an important step. Would expressing what's on your mind help?",
        "Sadness can feel heavy to carry. You don't have to face these feelings alone. What support would be most helpful for you right now?"
    );
    
    private final List<String> stressResponses = Arrays.asList(
        "I can hear that you're under a lot of stress right now. That's really difficult. What's contributing most to your stress levels?",
        "Stress can be so overwhelming. Your body and mind are working hard to cope. What's one small thing we could do to ease the pressure a bit?",
        "Being stressed is exhausting, both mentally and physically. Would it help to break down what's causing this into smaller, more manageable pieces?",
        "I'm sorry to hear you're feeling stressed. It might help to take a moment for yourself. Could we try a short breathing exercise together?",
        "Stress often comes when we're carrying too much. You don't have to handle everything at once. What's one priority we could focus on right now?"
    );
    
    private final List<String> fearResponses = Arrays.asList(
        "Fear can be really overwhelming. I want you to know that your feelings are valid, and you're not alone in this moment.",
        "I hear that you're feeling afraid. That sounds really difficult. Would it help to talk about what's causing this fear?",
        "When fear is present, it can be hard to think clearly. Would it help to try a grounding exercise to help you feel more centered?",
        "It takes courage to acknowledge when you're feeling afraid. Can we explore what might help you feel even a little bit safer right now?",
        "Fear is your body's way of trying to protect you, but sometimes it can be too intense. What's one small step that might help reduce this feeling?"
    );
    
    private final List<String> angerResponses = Arrays.asList(
        "I can hear that you're feeling angry. That's a valid emotion, and it's okay to feel this way. Would it help to express more about what's causing this?",
        "Anger often comes from feeling hurt or that something is unfair. What's underneath this anger for you right now?",
        "When we're angry, our bodies can feel tense and our thoughts might race. Would it help to try a brief calming exercise?",
        "Thank you for sharing that you're feeling angry. That takes self-awareness. Is there a specific situation that triggered these feelings?",
        "Anger can be an intense emotion to experience. What would be a healthy way for you to express or release some of this feeling?"
    );
    
    private final List<String> gratitudePrompts = Arrays.asList(
        "What's something small that brought you joy today?",
        "Who is someone you're grateful to have in your life, and why?",
        "What's something your body allows you to do that you appreciate?",
        "What's a challenge you've overcome that you now feel grateful for?",
        "What's something in nature that fills you with wonder or gratitude?",
        "What's a quality in yourself that you're thankful for?",
        "What's something you're looking forward to?",
        "What's a small comfort or luxury in your life that you appreciate?"
    );
    
    // Breathing exercise steps
    private final List<String> breathingExercise = Arrays.asList(
        "Let's start a brief breathing exercise. Find a comfortable position and gently close your eyes if that feels right.",
        "Breathe in slowly through your nose for 4 counts... 1... 2... 3... 4...",
        "Hold your breath for 4 counts... 1... 2... 3... 4...",
        "Now exhale slowly through your mouth for 6 counts... 1... 2... 3... 4... 5... 6...",
        "Let's repeat this cycle a few more times. Breathe in for 4... hold for 4... exhale for 6...",
        "Notice how your body feels as you continue breathing this way...",
        "With each breath, imagine tension flowing out of your body...",
        "Last cycle... breathe in... hold... and release...",
        "Whenever you're ready, gently open your eyes and return your awareness to the room.",
        "How do you feel now? Even a small moment of mindful breathing can help shift your state."
    );
    
    // 5-4-3-2-1 Grounding technique
    private final List<String> groundingTechnique = Arrays.asList(
        "Let's try a grounding technique called 5-4-3-2-1. This helps bring your attention to the present moment.",
        "Start by taking a deep breath...",
        "Now, name 5 things you can SEE around you right now.",
        "Next, notice 4 things you can FEEL or TOUCH (like the chair against your back or the texture of your clothing).",
        "Now, identify 3 things you can HEAR in this moment.",
        "Next, notice 2 things you can SMELL (or smells you like if you can't smell anything right now).",
        "Finally, name 1 thing you can TASTE right now (or a taste you enjoy).",
        "Take another deep breath...",
        "How are you feeling now? This exercise helps anchor you to the present moment through your senses."
    );
    
    // Positive affirmations
    private final List<String> positiveAffirmations = Arrays.asList(
        "I am doing the best I can with what I have right now.",
        "My feelings are valid, and I'm allowed to feel them fully.",
        "I am worthy of love and compassion, especially from myself.",
        "This moment is temporary, and I have the strength to move through it.",
        "I've survived difficult times before, and I'll make it through this too.",
        "I don't have to be perfect to be worthy.",
        "It's okay to take things one small step at a time.",
        "I am resilient and can adapt to change.",
        "I trust myself to make decisions that are right for me.",
        "I am allowed to set boundaries and prioritize my wellbeing."
    );
    
    // Self-care checklist items
    private final List<String> selfCareChecklist = Arrays.asList(
        "Have you had enough water today?",
        "When was your last nutritious meal?",
        "How did you sleep last night? Could you rest better tonight?",
        "Have you moved your body today, even just for a few minutes?",
        "Have you spent time outdoors or in natural light?",
        "Have you connected with someone who makes you feel good?",
        "Have you done something just for enjoyment today?",
        "Have you taken any breaks from screens?",
        "Have you practiced any mindfulness or relaxation today?",
        "Have you shown yourself kindness today?"
    );
    
    // Crisis resources information
    private final String crisisResources = 
        "If you're experiencing a mental health crisis or having thoughts of harming yourself:\n\n" +
        "• National Suicide Prevention Lifeline: 988 or 1-800-273-8255\n" +
        "• Crisis Text Line: Text HOME to 741741\n" +
        "• Emergency Services: 911\n\n" +
        "Remember, reaching out for help shows tremendous courage and strength. " +
        "You deserve support, and there are people ready to help 24/7.";
    
    // Default greeting message
    private static final String GREETING_MESSAGE = 
        "Hi there! I'm your supportive mental health assistant. How are you feeling today? I'm here to listen and help.";
    
    public ChatbotController() {
        sentimentService = SentimentServiceFactory.getService();
        chatMessageRepository = RepositoryFactory.getChatMessageRepository();
        chatMessages = FXCollections.observableArrayList();
    }
    
    @FXML
    public void initialize() {
        // Configure the UI
        setupChatListView();
        setupInputHandling();
        setupNavigationButtons();
        setupWellnessToolkitButtons();
        
        // Add feedback button functionality if present in the FXML
        if (feedbackButton != null) {
            feedbackButton.setOnAction(event -> {
                if (lastUserMessage != null && lastBotMessage != null) {
                    openFeedbackDialog(lastUserMessage, lastBotMessage);
                } else {
                    showInfoMessage("Please have a conversation first before providing feedback.");
                }
            });
        }
        
        // Store scene user data for access from cells
        Platform.runLater(() -> {
            if (messageInput.getScene() != null) {
                messageInput.getScene().setUserData(this);
            }
        });
        
        // Load recent conversation if available
        loadRecentMessages();
        
        // Add welcome message if no messages in history
        if (chatMessages.isEmpty()) {
            addBotGreeting();
        }
        
        // Only show feedback buttons for real AI services
        boolean isRealAIService = !(sentimentService instanceof com.dailymoodtracker.service.DummySentimentService);
        feedbackButton.setVisible(isRealAIService);
    }
    
    private void setupChatListView() {
        chatListView.setItems(chatMessages);
        chatListView.setCellFactory(param -> new ChatMessageCell());
        
        // Autoscroll to bottom when new messages are added
        chatMessages.addListener((javafx.collections.ListChangeListener.Change<? extends ChatMessage> c) -> {
            chatListView.scrollTo(chatMessages.size() - 1);
        });
    }
    
    private void setupInputHandling() {
        // Send message on button click
        sendButton.setOnAction(event -> sendMessage());
        
        // Send message on Enter key press
        messageInput.setOnAction(event -> sendMessage());
        
        // Quick response buttons
        if (feelingAnxiousButton != null) {
            feelingAnxiousButton.setOnAction(event -> {
                messageInput.setText("I'm feeling anxious");
                sendMessage();
            });
        }
        
        if (feelingSadButton != null) {
            feelingSadButton.setOnAction(event -> {
                messageInput.setText("I'm feeling sad");
                sendMessage();
            });
        }
        
        if (needBreathingButton != null) {
            needBreathingButton.setOnAction(event -> {
                messageInput.setText("I need help with breathing");
                sendMessage();
            });
        }
        
        if (needGratitudeButton != null) {
            needGratitudeButton.setOnAction(event -> {
                messageInput.setText("Help me practice gratitude");
                sendMessage();
            });
        }
    }
    
    private void setupNavigationButtons() {
        if (dashboardButton != null) {
            dashboardButton.setOnAction(event -> navigateToDashboard());
        }
        
        if (historyButton != null) {
            historyButton.setOnAction(event -> navigateToHistory());
        }
        
        if (settingsButton != null) {
            settingsButton.setOnAction(event -> navigateToSettings());
        }
        
        // Set up wellness toolkit buttons
        setupWellnessToolkitButtons();
    }
    
    private void setupWellnessToolkitButtons() {
        if (recordMoodButton != null) {
            recordMoodButton.setOnAction(event -> recordMood());
        }
        
        if (viewMoodHistoryButton != null) {
            viewMoodHistoryButton.setOnAction(event -> {
                navigateToHistory();
            });
        }
        
        if (gratitudeJournalButton != null) {
            gratitudeJournalButton.setOnAction(event -> {
                String randomPrompt = getRandomItem(gratitudePrompts);
                ChatMessage botMessage = new ChatMessage(
                    "Let's practice gratitude. " + randomPrompt, 
                    ChatMessage.MessageType.BOT
                );
                chatMessages.add(botMessage);
                saveBotMessage(botMessage);
            });
        }
        
        if (breathingExerciseButton != null) {
            breathingExerciseButton.setOnAction(event -> startBreathingExercise());
        }
        
        if (groundingTechniqueButton != null) {
            groundingTechniqueButton.setOnAction(event -> startGroundingTechnique());
        }
        
        if (positiveAffirmationButton != null) {
            positiveAffirmationButton.setOnAction(event -> showPositiveAffirmations());
        }
        
        if (thoughtReframingButton != null) {
            thoughtReframingButton.setOnAction(event -> {
                ChatMessage botMessage = new ChatMessage(
                    "Let's practice reframing negative thoughts. Can you share a challenging thought you've had recently?", 
                    ChatMessage.MessageType.BOT
                );
                chatMessages.add(botMessage);
                saveBotMessage(botMessage);
            });
        }
        
        if (selfCareChecklistButton != null) {
            selfCareChecklistButton.setOnAction(event -> showSelfCareChecklist());
        }
        
        if (wellnessActivitiesButton != null) {
            wellnessActivitiesButton.setOnAction(event -> {
                String activities = "Here are some wellness activities you might try:\n\n" +
                    "• Take a 10-minute walk outside\n" +
                    "• Practice gentle stretching\n" +
                    "• Listen to a favorite uplifting song\n" +
                    "• Draw or doodle for 5 minutes\n" +
                    "• Write down three good things about your day\n" +
                    "• Call or message a supportive friend\n" +
                    "• Take a warm shower or bath\n" +
                    "• Practice deep breathing for 2 minutes\n" +
                    "• Enjoy a cup of tea mindfully\n" +
                    "• Declutter a small space\n\n" +
                    "Which one sounds appealing to try today?";
                
                ChatMessage botMessage = new ChatMessage(activities, ChatMessage.MessageType.BOT);
                chatMessages.add(botMessage);
                saveBotMessage(botMessage);
            });
        }
        
        if (crisisResourcesButton != null) {
            crisisResourcesButton.setOnAction(event -> {
                ChatMessage botMessage = new ChatMessage(crisisResources, ChatMessage.MessageType.BOT);
                chatMessages.add(botMessage);
                saveBotMessage(botMessage);
            });
        }
    }
    
    private void navigateToDashboard() {
        try {
            // Load the FXML file directly and let the FXML handle controller injection
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
            
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            
            logger.info("Navigated to dashboard");
        } catch (IOException e) {
            logger.error("Failed to navigate to dashboard: {}", e.getMessage(), e);
            showErrorMessage("Navigation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error navigating to dashboard: {}", e.getMessage(), e);
            showErrorMessage("Unexpected navigation error: " + e.getMessage());
        }
    }
    
    private void navigateToHistory() {
        try {
            // Load the FXML file directly and let the FXML handle controller injection
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
            
            Stage stage = (Stage) historyButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            
            logger.info("Navigated to history view");
        } catch (IOException e) {
            logger.error("Failed to navigate to history view: {}", e.getMessage(), e);
            showErrorMessage("Navigation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error navigating to history view: {}", e.getMessage(), e);
            showErrorMessage("Unexpected navigation error: " + e.getMessage());
        }
    }
    
    private void navigateToSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("AI Settings");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(settingsButton.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
            logger.info("Opened AI settings dialog");
        } catch (IOException e) {
            logger.error("Failed to open AI settings dialog", e);
            showErrorMessage("Navigation error. Please try again.");
        }
    }
    
    private void recordMood() {
        // First check if we've analyzed any messages
        if (chatMessages.stream().noneMatch(msg -> msg.hasSentiment())) {
            showInfoMessage("Share how you're feeling first so I can help track your mood.");
            return;
        }
        
        // Find the most recent message with sentiment
        chatMessages.stream()
            .filter(ChatMessage::hasSentiment)
            .reduce((first, second) -> second) // Get the last element
            .ifPresent(lastSentimentMessage -> {
                String dominantSentiment = lastSentimentMessage.getSentiment().getDominantSentiment();
                String activity = suggestActivity(dominantSentiment);
                
                // Add a bot message with the recording confirmation and suggestion
                String botResponse = String.format(
                    "I've recorded your mood as %s. %s\n\nWould you like to talk about something else?",
                    formatSentiment(dominantSentiment),
                    activity
                );
                
                ChatMessage botMessage = new ChatMessage(botResponse, ChatMessage.MessageType.BOT);
                chatMessages.add(botMessage);
                
                try {
                    chatMessageRepository.save(botMessage);
                } catch (Exception e) {
                    logger.error("Failed to save bot response to database", e);
                }
            });
    }
    
    private String formatSentiment(String sentiment) {
        switch (sentiment.toLowerCase()) {
            case "positive": return "positive 😊";
            case "negative": return "negative 😔";
            case "neutral": return "neutral 😐";
            default: return sentiment;
        }
    }
    
    private String suggestActivity(String sentiment) {
        List<String> activities;
        String intro;
        
        switch (sentiment.toLowerCase()) {
            case "positive":
                activities = positiveActivities;
                intro = "To maintain this positive mood, I suggest";
                break;
            case "negative":
                activities = negativeActivities;
                intro = "To help improve your mood, consider";
                break;
            default:
                activities = neutralActivities;
                intro = "You might benefit from";
                break;
        }
        
        // Select a random activity from the appropriate list
        String activity = activities.get(random.nextInt(activities.size()));
        return intro + " " + activity + ".";
    }
    
    private void showInfoMessage(String message) {
        ChatMessage infoMessage = new ChatMessage(message, ChatMessage.MessageType.BOT);
        chatMessages.add(infoMessage);
        
        try {
            chatMessageRepository.save(infoMessage);
        } catch (Exception e) {
            logger.error("Failed to save info message to database", e);
        }
    }
    
    private void showErrorMessage(String message) {
        ChatMessage errorMessage = new ChatMessage("Error: " + message, ChatMessage.MessageType.BOT);
        chatMessages.add(errorMessage);
        
        try {
            chatMessageRepository.save(errorMessage);
        } catch (Exception e) {
            logger.error("Failed to save error message to database", e);
        }
    }
    
    private void addBotGreeting() {
        // Add initial bot greeting
        ChatMessage greeting = new ChatMessage(GREETING_MESSAGE, ChatMessage.MessageType.BOT);
        chatMessages.add(greeting);
        
        // Save greeting to database
        try {
            chatMessageRepository.save(greeting);
        } catch (Exception e) {
            logger.error("Failed to save greeting message to database", e);
        }
    }
    
    private void loadRecentMessages() {
        try {
            // Load last 20 messages for this user
            chatMessages.addAll(chatMessageRepository.findRecentByUserId(currentUserId, 20));
        } catch (Exception e) {
            logger.error("Failed to load recent messages from database", e);
        }
    }
    
    @FXML
    private void sendMessage() {
        String messageText = messageInput.getText().trim();
        if (messageText.isEmpty()) {
            return;
        }
        
        // Create user message
        ChatMessage userMessage = new ChatMessage(messageText, ChatMessage.MessageType.USER);
        chatMessages.add(userMessage);
        
        // Save to database
        try {
            chatMessageRepository.save(userMessage);
        } catch (Exception e) {
            logger.error("Failed to save user message to database", e);
        }
        
        // Store as last user message for feedback
        lastUserMessage = userMessage;
        
        // Clear input field
        messageInput.clear();
        
        // Process message with sentiment analysis
        processMessageWithSentimentAnalysis(userMessage);
    }
    
    private void processMessageWithSentimentAnalysis(ChatMessage userMessage) {
        // Add "typing" indicator
        int typingIndex = chatMessages.size();
        ChatMessage typingMessage = new ChatMessage("Analyzing...", ChatMessage.MessageType.BOT);
        chatMessages.add(typingMessage);
        
        // Process the message asynchronously
        CompletableFuture<SentimentResult> future = sentimentService.analyzeSentimentAsync(
            userMessage.getContent(), currentUserId);
            
        future.thenAccept(sentimentResult -> {
            // Apply sentiment to user message
            userMessage.setSentiment(sentimentResult);
            
            // Update user message in database with sentiment
            try {
                chatMessageRepository.save(userMessage);
            } catch (Exception e) {
                logger.error("Failed to update user message with sentiment", e);
            }
            
            // Log detailed sentiment data for therapeutic insights
            if (sentimentResult.getSpecificEmotion() != null) {
                Double intensity = sentimentResult.getEmotionScores().get("intensity");
                String intensityStr = intensity != null ? String.format("%.2f", intensity) : "N/A";
                String therapeuticApproach = sentimentResult.getMetadataValue("therapeutic_approach");
                String conversationNeeds = sentimentResult.getMetadataValue("conversation_needs");
                
                logger.info("Detailed sentiment: emotion={}, intensity={}, approach={}, needs={}", 
                    sentimentResult.getSpecificEmotion(), intensityStr,
                    therapeuticApproach != null ? therapeuticApproach : "N/A",
                    conversationNeeds != null ? conversationNeeds : "N/A");
            }
            
            // Generate bot response based on sentiment
            String botResponse = null;
            try {
                botResponse = sentimentService.getBotResponse(sentimentResult);
            } catch (Exception e) {
                logger.error("Error getting bot response: {}", e.getMessage(), e);
                botResponse = "I'm having trouble connecting to my AI services right now. This may be due to API limits or connection issues. Please try again in a moment.";
            }
            
            // Final variable for lambda
            final String finalBotResponse = botResponse;
            
            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                // Remove typing indicator
                chatMessages.remove(typingIndex);
                
                // Add bot response
                ChatMessage responseMessage = new ChatMessage(finalBotResponse, ChatMessage.MessageType.BOT);
                responseMessage.setRelatedSentiment(sentimentResult);
                chatMessages.add(responseMessage);
                
                // Store as last bot message for feedback
                lastBotMessage = responseMessage;
                
                // Save bot response to database
                try {
                    chatMessageRepository.save(responseMessage);
                } catch (Exception e) {
                    logger.error("Failed to save bot response to database", e);
                }
                
                // Update sentiment bars and emotion display
                updateSentimentBars(sentimentResult);
                updateEmotionDisplay(sentimentResult);
            });
            
            // Log sentiment analysis
            logger.info("Sentiment analysis: {}", sentimentResult);
        }).exceptionally(e -> {
            Throwable cause = e.getCause();
            logger.error("Error analyzing sentiment", cause);
            Platform.runLater(() -> {
                // Remove typing indicator
                chatMessages.remove(typingIndex);
                
                // Add error message
                String errorMsg = "I'm having trouble understanding that right now.";
                if (cause != null && cause.getMessage() != null && cause.getMessage().contains("model")) {
                    errorMsg = "I'm having trouble with my AI model configuration. Please check the API settings.";
                } else if (cause != null && cause.getMessage() != null && cause.getMessage().contains("API")) {
                    errorMsg = "I'm having trouble connecting to my AI services. This could be due to rate limits or API issues.";
                }
                
                ChatMessage errorMessage = new ChatMessage(errorMsg, ChatMessage.MessageType.BOT);
                chatMessages.add(errorMessage);
                
                // Store as last bot message for feedback
                lastBotMessage = errorMessage;
                
                // Save error message to database
                try {
                    chatMessageRepository.save(errorMessage);
                } catch (Exception ex) {
                    logger.error("Failed to save error message to database", ex);
                }
            });
            return null;
        });
    }
    
    private String generateEnhancedResponse(String userMessage, SentimentResult sentiment) {
        String lowercaseMessage = userMessage.toLowerCase();
        String dominantSentiment = sentiment.getDominantSentiment();
        
        // Update dominant emotion label
        Platform.runLater(() -> {
            if (dominantEmotionLabel != null) {
                String emotionText = "Your dominant emotion: " + formatSentiment(dominantSentiment);
                dominantEmotionLabel.setText(emotionText);
            }
        });
        
        // Check for specific emotional states or needs
        if (lowercaseMessage.contains("anxious") || lowercaseMessage.contains("anxiety") || 
            lowercaseMessage.contains("nervous") || lowercaseMessage.contains("worry")) {
            return getRandomItem(anxietyResponses);
        }
        
        if (lowercaseMessage.contains("sad") || lowercaseMessage.contains("unhappy") || 
            lowercaseMessage.contains("depressed") || lowercaseMessage.contains("down")) {
            return getRandomItem(sadnessResponses);
        }
        
        if (lowercaseMessage.contains("stress") || lowercaseMessage.contains("overwhelm") || 
            lowercaseMessage.contains("too much") || lowercaseMessage.contains("pressure")) {
            return getRandomItem(stressResponses);
        }
        
        if (lowercaseMessage.contains("fear") || lowercaseMessage.contains("afraid") || 
            lowercaseMessage.contains("scared") || lowercaseMessage.contains("terrified")) {
            return getRandomItem(fearResponses);
        }
        
        if (lowercaseMessage.contains("angry") || lowercaseMessage.contains("anger") || 
            lowercaseMessage.contains("mad") || lowercaseMessage.contains("frustrated")) {
            return getRandomItem(angerResponses);
        }
        
        if (lowercaseMessage.contains("breathing") || lowercaseMessage.contains("breath") || 
            lowercaseMessage.contains("calm down") || lowercaseMessage.contains("relax")) {
            startBreathingExercise();
            return "Let's take a moment for some breathing together.";
        }
        
        if (lowercaseMessage.contains("grounding") || lowercaseMessage.contains("present") || 
            lowercaseMessage.contains("reality") || lowercaseMessage.contains("panicking")) {
            startGroundingTechnique();
            return "Let's try a grounding exercise to help you connect with the present moment.";
        }
        
        if (lowercaseMessage.contains("gratitude") || lowercaseMessage.contains("thankful") || 
            lowercaseMessage.contains("appreciate") || lowercaseMessage.contains("grateful")) {
            return "Let's practice gratitude. " + getRandomItem(gratitudePrompts);
        }
        
        // If no specific need is detected, respond based on sentiment
        if (dominantSentiment.equalsIgnoreCase("positive")) {
            return "I'm glad to hear you're feeling positive! What's contributing to these good feelings?";
        } else if (dominantSentiment.equalsIgnoreCase("negative")) {
            return "I'm sorry to hear you're not feeling your best. Would you like to talk more about what's going on?";
        } else {
            return "Thank you for sharing. How else have you been feeling lately?";
        }
    }
    
    private String getRandomItem(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "I'm here to listen and support you.";
        }
        return items.get(random.nextInt(items.size()));
    }
    
    private void updateSentimentBars(SentimentResult result) {
        if (positiveBar != null && neutralBar != null && negativeBar != null) {
            positiveBar.setProgress(result.getPositiveScore());
            neutralBar.setProgress(result.getNeutralScore());
            negativeBar.setProgress(result.getNegativeScore());
            
            // Update percentage labels
            if (positiveLabel != null) {
                positiveLabel.setText(String.format("%.0f%%", result.getPositiveScore() * 100));
            }
            if (neutralLabel != null) {
                neutralLabel.setText(String.format("%.0f%%", result.getNeutralScore() * 100));
            }
            if (negativeLabel != null) {
                negativeLabel.setText(String.format("%.0f%%", result.getNegativeScore() * 100));
            }
        }
    }
    
    private void startBreathingExercise() {
        final int[] index = {0};
        
        // Show first step immediately
        ChatMessage firstStep = new ChatMessage(breathingExercise.get(index[0]), ChatMessage.MessageType.BOT);
        chatMessages.add(firstStep);
        saveBotMessage(firstStep);
        index[0]++;
        
        // Set timer to show remaining steps with delays
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        
        for (int i = 1; i < breathingExercise.size(); i++) {
            final int stepIndex = i;
            javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(4 * i), // Delay steps by 4 seconds each
                event -> {
                    ChatMessage step = new ChatMessage(breathingExercise.get(stepIndex), ChatMessage.MessageType.BOT);
                    chatMessages.add(step);
                    saveBotMessage(step);
                }
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        timeline.play();
    }
    
    private void startGroundingTechnique() {
        final int[] index = {0};
        
        // Show first step immediately
        ChatMessage firstStep = new ChatMessage(groundingTechnique.get(index[0]), ChatMessage.MessageType.BOT);
        chatMessages.add(firstStep);
        saveBotMessage(firstStep);
        index[0]++;
        
        // Set timer to show remaining steps with delays
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        
        for (int i = 1; i < groundingTechnique.size(); i++) {
            final int stepIndex = i;
            javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(5 * i), // Delay steps by 5 seconds each
                event -> {
                    ChatMessage step = new ChatMessage(groundingTechnique.get(stepIndex), ChatMessage.MessageType.BOT);
                    chatMessages.add(step);
                    saveBotMessage(step);
                }
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        timeline.play();
    }
    
    private void showPositiveAffirmations() {
        StringBuilder affirmationsText = new StringBuilder("Here are some positive affirmations. Try reading one aloud that resonates with you:\n\n");
        
        for (String affirmation : positiveAffirmations) {
            affirmationsText.append("• ").append(affirmation).append("\n");
        }
        
        affirmationsText.append("\nRemember, affirmations work best when repeated regularly. Which one speaks to you most right now?");
        
        ChatMessage botMessage = new ChatMessage(affirmationsText.toString(), ChatMessage.MessageType.BOT);
        chatMessages.add(botMessage);
        saveBotMessage(botMessage);
    }
    
    private void showSelfCareChecklist() {
        StringBuilder checklistText = new StringBuilder("Let's go through a quick self-care check-in:\n\n");
        
        for (String item : selfCareChecklist) {
            checklistText.append("• ").append(item).append("\n");
        }
        
        checklistText.append("\nWhich of these areas might need some attention today?");
        
        ChatMessage botMessage = new ChatMessage(checklistText.toString(), ChatMessage.MessageType.BOT);
        chatMessages.add(botMessage);
        saveBotMessage(botMessage);
    }
    
    private void saveBotMessage(ChatMessage message) {
        try {
            chatMessageRepository.save(message);
        } catch (Exception e) {
            logger.error("Failed to save bot message to database", e);
        }
    }
    
    /**
     * Sets the current user ID for this chatbot session.
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }
    
    /**
     * Custom ListCell for displaying chat messages.
     */
    private static class ChatMessageCell extends ListCell<ChatMessage> {
        @Override
        protected void updateItem(ChatMessage message, boolean empty) {
            super.updateItem(message, empty);
            
            if (empty || message == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            
            HBox messageContainer = new HBox(10);
            
            if (message.getType() == ChatMessage.MessageType.USER) {
                messageContainer.setAlignment(Pos.CENTER_RIGHT);
                createUserMessageBubble(message, messageContainer);
            } else {
                messageContainer.setAlignment(Pos.CENTER_LEFT);
                createBotMessageBubble(message, messageContainer);
            }
            
            setGraphic(messageContainer);
        }
        
        private void createUserMessageBubble(ChatMessage message, HBox container) {
            VBox messageBox = new VBox(5);
            messageBox.setMaxWidth(300);
            
            // Message bubble
            TextFlow textFlow = new TextFlow();
            textFlow.getStyleClass().addAll("message-bubble", "user-message-bubble");
            
            Text messageText = new Text(message.getContent());
            textFlow.getChildren().add(messageText);
            
            // Add sentiment emoji if available
            HBox infoBox = new HBox(10);
            infoBox.setAlignment(Pos.CENTER_RIGHT);
            
            Text timeText = new Text(message.getFormattedTime());
            timeText.getStyleClass().add("message-time");
            infoBox.getChildren().add(timeText);
            
            if (message.hasSentiment()) {
                Text sentimentEmoji = new Text(message.getSentimentEmoji());
                sentimentEmoji.setStyle("-fx-font-size: 14px;");
                infoBox.getChildren().add(sentimentEmoji);
            }
            
            messageBox.getChildren().addAll(textFlow, infoBox);
            container.getChildren().add(messageBox);
        }
        
        private void createBotMessageBubble(ChatMessage message, HBox container) {
            VBox messageBox = new VBox(5);
            messageBox.setMaxWidth(300);
            
            // Avatar for bot messages
            Text botAvatar = new Text("🤖");
            
            StackPane avatarContainer = new StackPane(botAvatar);
            avatarContainer.getStyleClass().add("bot-avatar");
            
            // Message bubble
            TextFlow textFlow = new TextFlow();
            textFlow.getStyleClass().addAll("message-bubble", "bot-message-bubble");
            
            Text messageText = new Text(message.getContent());
            textFlow.getChildren().add(messageText);
            
            // Time stamp and controls
            HBox infoBox = new HBox(10);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            
            Text timeText = new Text(message.getFormattedTime());
            timeText.getStyleClass().add("message-time");
            infoBox.getChildren().add(timeText);
            
            // Add feedback icon
            Text feedbackIcon = new Text("👍");
            feedbackIcon.getStyleClass().add("feedback-icon");
            
            // Capture the current message
            final ChatMessage botMessage = message;
            
            // Add click event for feedback
            feedbackIcon.setOnMouseClicked(e -> {
                // Find the user message that preceded this bot message
                ListView<ChatMessage> listView = (ListView<ChatMessage>) getListView();
                if (listView != null) {
                    ObservableList<ChatMessage> messages = listView.getItems();
                    int currentIndex = messages.indexOf(botMessage);
                    ChatMessage userMessage = null;
                    
                    // Search backward for the most recent user message
                    for (int i = currentIndex - 1; i >= 0; i--) {
                        if (messages.get(i).getType() == ChatMessage.MessageType.USER) {
                            userMessage = messages.get(i);
                            break;
                        }
                    }
                    
                    // If found user message and controller, open feedback dialog
                    if (userMessage != null) {
                        Scene scene = getScene();
                        if (scene != null && scene.getUserData() instanceof ChatbotController) {
                            ChatbotController controller = (ChatbotController) scene.getUserData();
                            controller.openFeedbackDialog(userMessage, botMessage);
                        }
                    }
                }
            });
            
            infoBox.getChildren().add(feedbackIcon);
            
            messageBox.getChildren().addAll(textFlow, infoBox);
            
            container.getChildren().addAll(avatarContainer, messageBox);
        }
    }
    
    /**
     * Updates the emotion display with detailed sentiment information
     */
    private void updateEmotionDisplay(SentimentResult result) {
        if (dominantEmotionLabel != null) {
            StringBuilder emotionText = new StringBuilder();
            
            // Basic emotion display
            String dominantSentiment = result.getDominantSentiment();
            emotionText.append("Emotion: ").append(formatSentiment(dominantSentiment));
            
            // Add specific emotion if available
            if (result.getSpecificEmotion() != null && !result.getSpecificEmotion().isEmpty()) {
                emotionText.append(" - ").append(result.getSpecificEmotion());
                
                // Add intensity if available
                Double intensity = result.getEmotionScores().get("intensity");
                if (intensity != null) {
                    String intensityLevel = intensity < 0.3 ? "mild" : (intensity < 0.7 ? "moderate" : "high");
                    emotionText.append(" (").append(intensityLevel).append(" intensity)");
                }
            }
            
            dominantEmotionLabel.setText(emotionText.toString());
            
            // Update support reminder with therapeutic approach if available
            if (supportReminderLabel != null) {
                String conversationNeeds = result.getMetadataValue("conversation_needs");
                if (conversationNeeds != null && !conversationNeeds.isEmpty()) {
                    supportReminderLabel.setText("Focus: " + conversationNeeds);
                    supportReminderLabel.setVisible(true);
                } else {
                    supportReminderLabel.setVisible(false);
                }
            }
        }
    }
    
    /**
     * Open the feedback dialog for a specific conversation.
     */
    private void openFeedbackDialog(ChatMessage userMessage, ChatMessage botMessage) {
        try {
            ChatFeedbackDialog dialog = new ChatFeedbackDialog(
                messageInput.getScene().getWindow(), userMessage, botMessage);
            dialog.showAndWait();
        } catch (Exception e) {
            logger.error("Error opening feedback dialog", e);
            showErrorMessage("Could not open feedback dialog: " + e.getMessage());
        }
    }
} 