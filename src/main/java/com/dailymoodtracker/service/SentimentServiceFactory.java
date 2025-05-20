package com.dailymoodtracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating SentimentAnalysisService instances.
 * Provides service implementations based on configuration and availability.
 */
public class SentimentServiceFactory {
    private static final Logger logger = LoggerFactory.getLogger(SentimentServiceFactory.class);
    
    // Service type enum
    public enum ServiceType {
        PYTHON,
        HTTP,
        DUMMY
    }
    
    // Default to HTTP if available, otherwise try Python, then fallback to dummy
    private static ServiceType serviceType = determineDefaultServiceType();
    
    // Cache the created service
    private static SentimentAnalysisService cachedService;
    
    /**
     * Determine the default service type based on available configurations.
     */
    private static ServiceType determineDefaultServiceType() {
        // First check if HTTP service is available
        try {
            HttpSentimentService httpService = HttpSentimentService.getInstance();
            if (httpService.isServiceAvailable()) {
                logger.info("HTTP sentiment analysis service detected, using as default");
                return ServiceType.HTTP;
            }
        } catch (Exception e) {
            logger.warn("Error checking HTTP service: {}", e.getMessage());
        }
        
        // Then check if Python service is available
        try {
            PythonSentimentService pythonService = PythonSentimentService.getInstance();
            if (pythonService.isServiceAvailable()) {
                logger.info("Python sentiment analysis service detected, using as default");
                return ServiceType.PYTHON;
            }
        } catch (Exception e) {
            logger.warn("Error checking Python service: {}", e.getMessage());
        }
        
        // Fallback to Dummy if nothing else available
        logger.warn("No valid service configurations found, falling back to Dummy service");
        return ServiceType.DUMMY;
    }
    
    /**
     * Gets the appropriate sentiment analysis service implementation
     * based on the current service type setting.
     */
    public static synchronized SentimentAnalysisService getService() {
        // Return cached instance if available
        if (cachedService != null) {
            return cachedService;
        }
        
        // Determine which service to use based on service type and availability
        try {
            switch (serviceType) {
                case HTTP:
                    try {
                        HttpSentimentService httpService = HttpSentimentService.getInstance();
                        if (httpService.isServiceAvailable()) {
                            return cachedService = httpService;
                        }
                        logger.warn("HTTP service is not available. Falling back to Python service");
                        serviceType = ServiceType.PYTHON; // Update service type
                        return getService(); // Recursive call with updated service type
                    } catch (Exception e) {
                        logger.error("Failed to initialize HTTP service: {}", e.getMessage(), e);
                        logger.warn("Falling back to Python service");
                        serviceType = ServiceType.PYTHON; // Update service type
                        return getService(); // Recursive call with updated service type
                    }
                    
                case PYTHON:
                    try {
                        PythonSentimentService pythonService = PythonSentimentService.getInstance();
                        if (pythonService.isServiceAvailable()) {
                            return cachedService = pythonService;
                        }
                        logger.warn("Python service is not available. Falling back to Dummy service");
                        serviceType = ServiceType.DUMMY;
                        return getService(); // Recursive call with updated service type
                    } catch (Exception e) {
                        logger.error("Failed to initialize Python service: {}", e.getMessage(), e);
                        logger.warn("Falling back to Dummy service");
                        serviceType = ServiceType.DUMMY;
                        return getService(); // Recursive call with updated service type
                    }
                    
                case DUMMY:
                default:
                    return cachedService = DummySentimentService.getInstance();
            }
        } catch (Exception e) {
            logger.error("Unexpected error creating sentiment service: {}", e.getMessage(), e);
            return cachedService = DummySentimentService.getInstance();
        }
    }
    
    /**
     * Set the sentiment analysis service type to use.
     * 
     * @param type The service type to use
     * @return the newly created service based on the updated setting
     */
    public static SentimentAnalysisService setServiceType(ServiceType type) {
        if (type != serviceType) {
            serviceType = type;
            // Clear cached service to force recreation
            cachedService = null;
            logger.info("Sentiment service setting changed to: {}", type);
        }
        return getService();
    }
    
    /**
     * Forces reload of the sentiment service.
     * Useful after updating API credentials.
     * 
     * @return the newly created service
     */
    public static SentimentAnalysisService reloadService() {
        cachedService = null;
        logger.info("Sentiment service reloading...");
        return getService();
    }
    
    /**
     * Gets diagnostic information about the current sentiment service.
     * 
     * @return A string containing diagnostic information
     */
    public static String getDiagnosticInfo() {
        StringBuilder info = new StringBuilder();
        
        info.append("Current service type: ").append(serviceType);
        
        info.append(", Current service: ");
        if (cachedService == null) {
            info.append("None (not initialized)");
        } else if (cachedService instanceof PythonSentimentService) {
            info.append("Python Sentiment Analysis (Local)");
        } else if (cachedService instanceof HttpSentimentService) {
            info.append("Python Sentiment Analysis (HTTP Service)");
        } else if (cachedService instanceof DummySentimentService) {
            info.append("Dummy Service (for testing)");
        } else {
            info.append(cachedService.getClass().getSimpleName());
        }
        
        // HTTP service info
        try {
            HttpSentimentService httpService = HttpSentimentService.getInstance();
            info.append(", HTTP service available: ").append(httpService.isServiceAvailable());
        } catch (Exception e) {
            info.append(", HTTP service available: false");
        }
        
        // Python service info
        try {
            PythonSentimentService pythonService = PythonSentimentService.getInstance();
            info.append(", Python service available: ").append(pythonService.isServiceAvailable());
        } catch (Exception e) {
            info.append(", Python service available: false");
        }
        
        return info.toString();
    }
    
    /**
     * Get current service type
     */
    public static ServiceType getServiceType() {
        return serviceType;
    }
} 