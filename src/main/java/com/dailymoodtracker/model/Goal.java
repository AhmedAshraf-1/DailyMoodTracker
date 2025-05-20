package com.dailymoodtracker.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a personal goal for the user in the Daily Mood Tracker.
 */
public class Goal implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String goalId;
    private String description;
    private LocalDate creationDate;
    private LocalDate completionDate;
    private boolean completed;
    
    /**
     * Creates a new goal with the specified parameters.
     * 
     * @param description the description of the goal
     * @param creationDate the date the goal was created
     * @param completionDate the date the goal was completed (null if not completed)
     * @param completed whether the goal is completed
     */
    public Goal(String description, LocalDate creationDate, LocalDate completionDate, boolean completed) {
        this.goalId = UUID.randomUUID().toString();
        this.description = description;
        this.creationDate = creationDate;
        this.completionDate = completionDate;
        this.completed = completed;
    }
    
    /**
     * Gets the unique identifier for this goal.
     * 
     * @return the goal ID
     */
    public String getGoalId() {
        return goalId;
    }
    
    /**
     * Sets the unique identifier for this goal.
     * 
     * @param goalId the goal ID to set
     */
    public void setGoalId(String goalId) {
        this.goalId = goalId;
    }
    
    /**
     * Gets the description of the goal.
     * 
     * @return the goal description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the description of the goal.
     * 
     * @param description the new goal description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the creation date of the goal.
     * 
     * @return the creation date
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }
    
    /**
     * Gets the completion date of the goal.
     * 
     * @return the completion date or null if not completed
     */
    public LocalDate getCompletionDate() {
        return completionDate;
    }
    
    /**
     * Sets the completion date of the goal.
     * 
     * @param completionDate the completion date to set
     */
    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }
    
    /**
     * Checks if the goal is completed.
     * 
     * @return true if the goal is completed, false otherwise
     */
    public boolean isCompleted() {
        return completed;
    }
    
    /**
     * Sets the completion status of the goal.
     * 
     * @param completed true to mark as completed, false otherwise
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
        
        // If marked as completed and no completion date, set it to now
        if (completed && completionDate == null) {
            this.completionDate = LocalDate.now();
        }
        
        // If marked as not completed, clear the completion date
        if (!completed) {
            this.completionDate = null;
        }
    }
    
    /**
     * Marks the goal as complete and sets the completion date to the current date.
     */
    public void complete() {
        this.completed = true;
        this.completionDate = LocalDate.now();
    }
    
    /**
     * Checks if two goals are equal based on their ID.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Goal other = (Goal) obj;
        return goalId.equals(other.goalId);
    }
    
    /**
     * Returns a hash code for the goal based on its ID.
     */
    @Override
    public int hashCode() {
        return goalId.hashCode();
    }
    
    /**
     * Returns a string representation of the goal.
     */
    @Override
    public String toString() {
        return "Goal{" +
                "goalId='" + goalId + '\'' +
                ", description='" + description + '\'' +
                ", creationDate=" + creationDate +
                ", completionDate=" + completionDate +
                ", completed=" + completed +
                '}';
    }
} 