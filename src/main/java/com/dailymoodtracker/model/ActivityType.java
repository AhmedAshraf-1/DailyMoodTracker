package com.dailymoodtracker.model;

public class ActivityType {
    private int id;
    private String name;
    private String description;

    public ActivityType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
} 