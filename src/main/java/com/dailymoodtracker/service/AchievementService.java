package com.dailymoodtracker.service;

import com.dailymoodtracker.model.Achievement;
import com.dailymoodtracker.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementService {
    private final Map<Integer, List<Achievement>> userAchievements;

    public AchievementService() {
        this.userAchievements = new HashMap<>();
    }

    public boolean addAchievement(User user, Achievement achievement) {
        List<Achievement> achievements = userAchievements.computeIfAbsent(user.getId(), k -> new ArrayList<>());
        
        // Check if achievement already exists
        boolean alreadyExists = achievements.stream()
            .anyMatch(a -> a.getName().equals(achievement.getName()));
            
        if (!alreadyExists) {
            achievements.add(achievement);
            return true; // Achievement was newly added
        }
        
        return false; // Achievement already existed
    }

    public List<Achievement> getUnlockedAchievements(User user) {
        return userAchievements.getOrDefault(user.getId(), new ArrayList<>());
    }

    public void checkMoodStreak(User user) {
        // Implementation for checking mood streaks
    }

    public void checkGoalCompletion(User user) {
        // Implementation for checking goal completion
    }

    public void checkActivityVariety(User user) {
        // Implementation for checking activity variety
    }
} 