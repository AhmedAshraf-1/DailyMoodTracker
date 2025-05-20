package com.dailymoodtracker.model;

import java.time.LocalDateTime;
import java.util.List;

public class MoodEntry {
    private int id;
    private int userId;
    private LocalDateTime timestamp;
    private int moodLevel;
    private String notes;
    private List<String> activities;

    public MoodEntry(LocalDateTime timestamp, int moodLevel, String notes, List<String> activities) {
        this.id = 0; // Default value, will be set when saved to database
        this.userId = 0; // Default value, will be set before saving
        this.timestamp = timestamp;
        this.moodLevel = moodLevel;
        this.notes = notes;
        this.activities = activities;
    }
    
    public MoodEntry(int id, int userId, LocalDateTime timestamp, int moodLevel, String notes, List<String> activities) {
        this.id = id;
        this.userId = userId;
        this.timestamp = timestamp;
        this.moodLevel = moodLevel;
        this.notes = notes;
        this.activities = activities;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getMoodLevel() {
        return moodLevel;
    }

    public String getNotes() {
        return notes;
    }

    public List<String> getActivities() {
        return activities;
    }

    public String getMoodEmoji() {
        return switch (moodLevel) {
            case 1 -> "😢"; // Very Sad
            case 2 -> "😕"; // Sad
            case 3 -> "😐"; // Neutral
            case 4 -> "🙂"; // Happy
            case 5 -> "😄"; // Very Happy
            default -> "❓"; // Unknown
        };
    }

    public String getMoodDescription() {
        return switch (moodLevel) {
            case 1 -> "Very Sad";
            case 2 -> "Sad";
            case 3 -> "Neutral";
            case 4 -> "Happy";
            case 5 -> "Very Happy";
            default -> "Unknown";
        };
    }

    @Override
    public String toString() {
        return "MoodEntry{" +
                "id=" + id +
                ", userId=" + userId +
                ", timestamp=" + timestamp +
                ", moodLevel=" + moodLevel +
                ", notes='" + notes + '\'' +
                ", activities=" + activities +
                '}';
    }
} 