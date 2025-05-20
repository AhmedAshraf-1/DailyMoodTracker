package com.dailymoodtracker.model;

public enum MoodType {
    VERY_SAD(1, "😢", "#FF6B6B", "Very Sad"),
    SAD(2, "😕", "#FFB6C1", "Sad"),
    NEUTRAL(3, "😐", "#FFD700", "Neutral"),
    HAPPY(4, "🙂", "#90EE90", "Happy"),
    VERY_HAPPY(5, "😄", "#98FB98", "Very Happy");

    private final int level;
    private final String emoji;
    private final String color;
    private final String description;

    MoodType(int level, String emoji, String color, String description) {
        this.level = level;
        this.emoji = emoji;
        this.color = color;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    public static MoodType fromLevel(int level) {
        for (MoodType type : values()) {
            if (type.level == level) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid mood level: " + level);
    }
} 