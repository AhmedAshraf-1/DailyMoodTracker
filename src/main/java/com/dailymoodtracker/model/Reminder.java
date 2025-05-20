package com.dailymoodtracker.model;

import java.time.LocalTime;

public class Reminder {
    private final int reminderId;
    private final int userId;
    private LocalTime time;
    private String message;
    private boolean enabled;
    private String frequency; // daily, weekly, custom
    private boolean read;

    public Reminder(int reminderId, int userId, LocalTime time, String message, boolean enabled, String frequency, boolean read) {
        this.reminderId = reminderId;
        this.userId = userId;
        this.time = time;
        this.message = message;
        this.enabled = enabled;
        this.frequency = frequency;
        this.read = read;
    }

    // Getters
    public int getReminderId() {
        return reminderId;
    }

    public int getUserId() {
        return userId;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getFrequency() {
        return frequency;
    }

    public boolean isRead() {
        return read;
    }

    // Setters for mutable fields
    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
} 