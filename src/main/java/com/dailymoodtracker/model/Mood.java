package com.dailymoodtracker.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Mood {
    private final String moodId;
    private final String userId;
    private final LocalDateTime date;
    private final MoodType moodType;
    private String notes;
    private List<Activity> activities;
    private List<Quote> quotes;

    public Mood(String moodId, String userId, LocalDateTime date, MoodType moodType) {
        this.moodId = moodId;
        this.userId = userId;
        this.date = date;
        this.moodType = moodType;
        this.activities = new ArrayList<>();
        this.quotes = new ArrayList<>();
    }

    // Getters
    public String getMoodId() {
        return moodId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public MoodType getMoodType() {
        return moodType;
    }

    public String getNotes() {
        return notes;
    }

    public List<Activity> getActivities() {
        return new ArrayList<>(activities);
    }

    public List<Quote> getQuotes() {
        return new ArrayList<>(quotes);
    }

    // Setters
    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Methods to manage relationships
    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public void addQuote(Quote quote) {
        quotes.add(quote);
    }

    public void removeQuote(Quote quote) {
        quotes.remove(quote);
    }

    // Convenience methods
    public String getMoodEmoji() {
        return moodType.getEmoji();
    }

    public String getMoodColor() {
        return moodType.getColor();
    }

    public String getMoodDescription() {
        return moodType.getDescription();
    }
} 