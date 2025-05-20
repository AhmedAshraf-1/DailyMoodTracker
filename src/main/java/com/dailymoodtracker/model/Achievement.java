package com.dailymoodtracker.model;

import java.time.LocalDateTime;

public class Achievement {
    private String name;
    private String description;
    private LocalDateTime dateAchieved;

    public Achievement(String name, String description, LocalDateTime dateAchieved) {
        this.name = name;
        this.description = description;
        this.dateAchieved = dateAchieved;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateAchieved() {
        return dateAchieved;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateAchieved(LocalDateTime dateAchieved) {
        this.dateAchieved = dateAchieved;
    }
} 