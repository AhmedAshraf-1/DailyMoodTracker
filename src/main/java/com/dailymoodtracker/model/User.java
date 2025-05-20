package com.dailymoodtracker.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;
    private List<MoodEntry> moods;
    private List<Goal> goals;
    private List<Reminder> reminders;

    public User(String username, String password) {
        this.id = 1; // Default user ID
        this.username = username;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.moods = new ArrayList<>();
        this.goals = new ArrayList<>();
        this.reminders = new ArrayList<>();
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<MoodEntry> getMoods() {
        return new ArrayList<>(moods); // Return a copy to prevent external modification
    }

    public List<Goal> getGoals() {
        return new ArrayList<>(goals); // Return a copy to prevent external modification
    }

    public List<Reminder> getReminders() {
        return new ArrayList<>(reminders); // Return a copy to prevent external modification
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Methods to manage relationships
    public void addMood(MoodEntry mood) {
        this.moods.add(mood);
    }

    public void removeMood(MoodEntry mood) {
        this.moods.remove(mood);
    }

    public void addGoal(Goal goal) {
        this.goals.add(goal);
    }

    public void removeGoal(Goal goal) {
        this.goals.remove(goal);
    }

    public void addReminder(Reminder reminder) {
        this.reminders.add(reminder);
    }

    public void removeReminder(Reminder reminder) {
        this.reminders.remove(reminder);
    }
} 