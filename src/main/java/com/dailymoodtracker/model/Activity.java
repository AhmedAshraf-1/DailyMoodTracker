package com.dailymoodtracker.model;

public class Activity {
    private final String activityId;
    private final String moodId;
    private final String userId;
    private final ActivityType activityType;
    private String notes;
    private int duration; // in minutes

    public Activity(String activityId, String moodId, String userId, ActivityType activityType) {
        this.activityId = activityId;
        this.moodId = moodId;
        this.userId = userId;
        this.activityType = activityType;
    }

    // Getters
    public String getActivityId() {
        return activityId;
    }

    public String getMoodId() {
        return moodId;
    }

    public String getUserId() {
        return userId;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public String getNotes() {
        return notes;
    }

    public int getDuration() {
        return duration;
    }

    // Setters
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
} 