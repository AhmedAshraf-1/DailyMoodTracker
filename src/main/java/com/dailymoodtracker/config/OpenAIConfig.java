package com.dailymoodtracker.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for OpenAI API.
 */
public class OpenAIConfig {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIConfig.class);
    
    private static final String CONFIG_FILE = "openai-config.properties";
    private static final String USER_HOME = System.getProperty("user.home");
    
    private String apiKey;
    private String apiUrl;
    private String model;
    private double temperature;
    private int maxTokens;
    private String systemSentimentPrompt;
    private String systemResponsePrompt;
    private int dailyLimit;
    
    // Custom GPT properties
    private String customGptId;
    private String customGptName;
    private String customGptApiUrl;
    private String customGptBetaHeader;
    private boolean useCustomGpt;
    
    private static OpenAIConfig instance;
    
    /**
     * Get the singleton instance of OpenAIConfig.
     * @return OpenAIConfig instance
     */
    public static synchronized OpenAIConfig getInstance() {
        if (instance == null) {
            instance = new OpenAIConfig();
        }
        return instance;
    }
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private OpenAIConfig() {
        loadConfig();
    }
    
    /**
     * Reload configuration from file.
     */
    public void reload() {
        loadConfig();
    }
    
    /**
     * Load configuration from the properties file.
     */
    private void loadConfig() {
        Properties properties = new Properties();
        
        // Try to load from current directory
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                properties.load(input);
                loadConfigFromProperties(properties);
                logger.info("Successfully loaded OpenAI API configuration from {}", configFile.getAbsolutePath());
                return;
            } catch (Exception e) {
                logger.error("Error loading OpenAI API config: {}", e.getMessage(), e);
            }
        }
        
        // Try to load from user's home directory
        configFile = new File(USER_HOME, CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                properties.load(input);
                loadConfigFromProperties(properties);
                logger.info("Successfully loaded OpenAI API configuration from {}", configFile.getAbsolutePath());
                return;
            } catch (Exception e) {
                logger.error("Error loading OpenAI API config: {}", e.getMessage(), e);
            }
        }
        
        // Try to load from classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                loadConfigFromProperties(properties);
                logger.info("Successfully loaded OpenAI API configuration from classpath");
                return;
            }
        } catch (Exception e) {
            logger.error("Error loading OpenAI API config from classpath: {}", e.getMessage(), e);
        }
        
        logger.error("OpenAI API configuration file not found in current directory, user home, or classpath");
    }
    
    /**
     * Load configuration values from properties.
     */
    private void loadConfigFromProperties(Properties properties) {
        apiKey = properties.getProperty("openai.api.key", "");
        apiUrl = properties.getProperty("openai.api.url", "https://api.openai.com/v1/chat/completions");
        model = properties.getProperty("openai.model", "gpt-3.5-turbo");
        
        try {
            temperature = Double.parseDouble(properties.getProperty("openai.temperature", "0.7"));
        } catch (NumberFormatException e) {
            temperature = 0.7;
            logger.warn("Invalid temperature value in config, using default: 0.7");
        }
        
        try {
            maxTokens = Integer.parseInt(properties.getProperty("openai.max_tokens", "300"));
        } catch (NumberFormatException e) {
            maxTokens = 300;
            logger.warn("Invalid max_tokens value in config, using default: 300");
        }
        
        try {
            dailyLimit = Integer.parseInt(properties.getProperty("openai.daily.limit", "100"));
        } catch (NumberFormatException e) {
            dailyLimit = 100;
            logger.warn("Invalid daily limit value in config, using default: 100");
        }
        
        // Load custom GPT configuration
        customGptId = properties.getProperty("openai.custom_gpt.id", "");
        customGptName = properties.getProperty("openai.custom_gpt.name", "Custom Mental Health GPT");
        customGptApiUrl = properties.getProperty("openai.custom_gpt.api_url", "https://api.openai.com/v1/assistants");
        customGptBetaHeader = properties.getProperty("openai.custom_gpt.beta_header", "assistants=v2");
        
        try {
            // Always set useCustomGpt to false regardless of config to disable it
            useCustomGpt = false;
            logger.warn("Custom GPT is disabled due to API changes, ignoring configuration setting");
        } catch (Exception e) {
            useCustomGpt = false;
            logger.warn("Invalid custom GPT enabled value in config, using default: false");
        }
        
        systemSentimentPrompt = properties.getProperty("openai.system.sentiment", 
            "You are a mental health assistant specialized in sentiment analysis. " +
            "Analyze the emotional tone of the user's message and return a JSON with " +
            "positive_score, negative_score, neutral_score (all values between 0-1 adding up to 1), " +
            "dominant_sentiment as either \"positive\", \"negative\", or \"neutral\", and " +
            "specific_emotion as the most likely emotional state (e.g., \"happy\", \"anxious\", \"sad\", \"angry\", etc.)");
        
        systemResponsePrompt = properties.getProperty("openai.system.response", 
            "You are an empathetic mental health assistant for a mood tracking application. " +
            "Respond to the user with warmth, understanding, and helpful guidance. " +
            "Be conversational yet professional. Keep responses concise and focused on supporting the user's emotional wellbeing.");
    }
    
    /**
     * Check if the configuration is valid.
     * @return true if valid, false otherwise
     */
    public boolean isConfigValid() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.contains("your_") && !apiKey.equals("placeholder");
    }
    
    /**
     * Check if custom GPT is configured and enabled.
     * @return true if custom GPT should be used, false otherwise
     */
    public boolean useCustomGpt() {
        return useCustomGpt && isCustomGptConfigValid();
    }
    
    /**
     * Check if custom GPT configuration is valid.
     * @return true if valid, false otherwise
     */
    public boolean isCustomGptConfigValid() {
        return isConfigValid() && customGptId != null && !customGptId.isEmpty();
    }
    
    /**
     * Get diagnostic information about the configuration.
     * @return String with diagnostic info
     */
    public String getConfigDiagnostics() {
        if (apiKey == null || apiKey.isEmpty()) {
            return "API key is missing";
        }
        if (apiKey.contains("your_") || apiKey.equals("placeholder")) {
            return "API key contains placeholder text";
        }
        if (useCustomGpt && (customGptId == null || customGptId.isEmpty())) {
            return "Custom GPT is enabled but ID is missing";
        }
        return "Configuration is valid" + (useCustomGpt ? " (Custom GPT enabled)" : "");
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public String getModel() {
        return model;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public String getSystemSentimentPrompt() {
        return systemSentimentPrompt;
    }
    
    public String getSystemResponsePrompt() {
        return systemResponsePrompt;
    }
    
    public int getDailyLimit() {
        return dailyLimit;
    }
    
    public String getCustomGptId() {
        return customGptId;
    }
    
    public void setCustomGptId(String customGptId) {
        this.customGptId = customGptId;
    }
    
    public String getCustomGptName() {
        return customGptName;
    }
    
    public void setCustomGptName(String customGptName) {
        this.customGptName = customGptName;
    }
    
    public String getCustomGptApiUrl() {
        return customGptApiUrl;
    }
    
    public void setCustomGptApiUrl(String customGptApiUrl) {
        this.customGptApiUrl = customGptApiUrl;
    }
    
    public boolean isUseCustomGpt() {
        return useCustomGpt;
    }
    
    public void setUseCustomGpt(boolean useCustomGpt) {
        this.useCustomGpt = useCustomGpt;
    }
    
    /**
     * Get the Custom GPT beta header value
     */
    public String getCustomGptBetaHeader() {
        return customGptBetaHeader;
    }
} 