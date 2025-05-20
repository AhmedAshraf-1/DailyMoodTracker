package com.dailymoodtracker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores user preferences and customization options for the Daily Mood Tracker.
 */
public class UserPreferences implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Theme options
    public enum Theme implements Serializable {
        DARK("Dark", "#1E1E1E", "#2D2D2D", "#3D3D3D", "#FFFFFF", "#BBBBBB"),
        LIGHT("Light", "#F5F5F5", "#FFFFFF", "#EEEEEE", "#333333", "#666666"),
        BLUE("Blue", "#1A2B3C", "#263545", "#354555", "#FFFFFF", "#BBBBBB"),
        PURPLE("Purple", "#2D1E3E", "#3D2D4E", "#4D3D5E", "#FFFFFF", "#BBBBBB"),
        GREEN("Green", "#1E3E2D", "#2D4E3D", "#3D5E4D", "#FFFFFF", "#BBBBBB");
        
        private final String name;
        private final String backgroundColor;
        private final String cardColor;
        private final String inputColor;
        private final String textColor;
        private final String secondaryTextColor;
        
        Theme(String name, String backgroundColor, String cardColor, String inputColor, 
              String textColor, String secondaryTextColor) {
            this.name = name;
            this.backgroundColor = backgroundColor;
            this.cardColor = cardColor;
            this.inputColor = inputColor;
            this.textColor = textColor;
            this.secondaryTextColor = secondaryTextColor;
        }
        
        public String getName() { return name; }
        public String getBackgroundColor() { return backgroundColor; }
        public String getCardColor() { return cardColor; }
        public String getInputColor() { return inputColor; }
        public String getTextColor() { return textColor; }
        public String getSecondaryTextColor() { return secondaryTextColor; }
    }
    
    // Accent color options
    public enum AccentColor implements Serializable {
        GREEN("#4CAF50"),
        BLUE("#2196F3"),
        PURPLE("#9C27B0"),
        ORANGE("#FF9800"),
        RED("#F44336"),
        PINK("#E91E63"),
        TEAL("#009688");
        
        private final String hexColor;
        
        AccentColor(String hexColor) {
            this.hexColor = hexColor;
        }
        
        public String getHexColor() { return hexColor; }
    }
    
    // Mood scale options
    public enum MoodScale implements Serializable {
        THREE_POINT(3),
        FIVE_POINT(5),
        TEN_POINT(10);
        
        private final int points;
        
        MoodScale(int points) {
            this.points = points;
        }
        
        public int getPoints() { return points; }
    }
    
    // User ID
    private final int userId;
    
    // Theme preferences
    private Theme theme;
    private AccentColor accentColor;
    
    // Mood scale preference
    private MoodScale moodScale;
    
    // Custom activity categories and activities
    private Map<String, List<String>> activityCategories;
    
    // User goals
    private List<Goal> personalGoals;
    
    // Constructor
    public UserPreferences(int userId) {
        this.userId = userId;
        this.theme = Theme.DARK; // Default theme
        this.accentColor = AccentColor.GREEN; // Default accent color
        this.moodScale = MoodScale.FIVE_POINT; // Default mood scale
        this.activityCategories = initializeDefaultCategories();
        this.personalGoals = new ArrayList<>();
    }
    
    // Initialize default activity categories
    private Map<String, List<String>> initializeDefaultCategories() {
        Map<String, List<String>> categories = new HashMap<>();
        
        // Physical category
        List<String> physical = new ArrayList<>();
        physical.add("Exercise");
        physical.add("Walking");
        physical.add("Running");
        physical.add("Swimming");
        physical.add("Yoga");
        categories.put("Physical", physical);
        
        // Mental category
        List<String> mental = new ArrayList<>();
        mental.add("Reading");
        mental.add("Meditation");
        mental.add("Learning");
        mental.add("Puzzle solving");
        categories.put("Mental", mental);
        
        // Social category
        List<String> social = new ArrayList<>();
        social.add("Family time");
        social.add("Friend meetup");
        social.add("Party");
        social.add("Date");
        categories.put("Social", social);
        
        // Leisure category
        List<String> leisure = new ArrayList<>();
        leisure.add("Gaming");
        leisure.add("TV/Movies");
        leisure.add("Music");
        leisure.add("Hobby");
        categories.put("Leisure", leisure);
        
        return categories;
    }
    
    // Getters and setters
    public int getUserId() { return userId; }
    
    public Theme getTheme() { return theme; }
    public void setTheme(Theme theme) { this.theme = theme; }
    
    public AccentColor getAccentColor() { return accentColor; }
    public void setAccentColor(AccentColor accentColor) { this.accentColor = accentColor; }
    
    public MoodScale getMoodScale() { return moodScale; }
    public void setMoodScale(MoodScale moodScale) { this.moodScale = moodScale; }
    
    public Map<String, List<String>> getActivityCategories() { return activityCategories; }
    
    public List<String> getActivitiesForCategory(String category) {
        return activityCategories.getOrDefault(category, new ArrayList<>());
    }
    
    public List<String> getAllCategories() {
        return new ArrayList<>(activityCategories.keySet());
    }
    
    public void addCategory(String categoryName) {
        if (!activityCategories.containsKey(categoryName)) {
            activityCategories.put(categoryName, new ArrayList<>());
        }
    }
    
    public void removeCategory(String categoryName) {
        activityCategories.remove(categoryName);
    }
    
    public void addActivityToCategory(String categoryName, String activity) {
        if (!activityCategories.containsKey(categoryName)) {
            addCategory(categoryName);
        }
        
        List<String> activities = activityCategories.get(categoryName);
        if (!activities.contains(activity)) {
            activities.add(activity);
        }
    }
    
    public void removeActivityFromCategory(String categoryName, String activity) {
        if (activityCategories.containsKey(categoryName)) {
            activityCategories.get(categoryName).remove(activity);
        }
    }
    
    public List<Goal> getPersonalGoals() {
        return new ArrayList<>(personalGoals);
    }
    
    public void addPersonalGoal(Goal goal) {
        personalGoals.add(goal);
    }
    
    public void removePersonalGoal(Goal goal) {
        personalGoals.remove(goal);
    }
    
    public void updatePersonalGoal(Goal oldGoal, Goal newGoal) {
        int index = personalGoals.indexOf(oldGoal);
        if (index != -1) {
            personalGoals.set(index, newGoal);
        }
    }
    
    // Get CSS styles for current theme and accent color
    public String getThemeStyles() {
        return String.format(
            "-fx-background-color: %s; " +
            "-fx-card-color: %s; " +
            "-fx-input-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-secondary-text-fill: %s; " +
            "-fx-accent-color: %s;",
            theme.getBackgroundColor(),
            theme.getCardColor(),
            theme.getInputColor(),
            theme.getTextColor(),
            theme.getSecondaryTextColor(),
            accentColor.getHexColor()
        );
    }
} 