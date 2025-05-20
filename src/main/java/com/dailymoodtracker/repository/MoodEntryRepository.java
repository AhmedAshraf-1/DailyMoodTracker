package com.dailymoodtracker.repository;

import com.dailymoodtracker.model.MoodEntry;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for MoodEntry entities.
 */
public interface MoodEntryRepository extends Repository<MoodEntry, Integer> {
    
    /**
     * Find all mood entries for a user.
     * 
     * @param userId the user ID
     * @return a list of mood entries
     */
    List<MoodEntry> findByUserId(int userId);
    
    /**
     * Find mood entries for a user in a date range.
     * 
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of mood entries
     */
    List<MoodEntry> findByUserIdAndDateRange(int userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find recent mood entries for a user.
     * 
     * @param userId the user ID
     * @param limit the maximum number of entries to return
     * @return a list of mood entries
     */
    List<MoodEntry> findRecentByUserId(int userId, int limit);
    
    /**
     * Calculate average mood level for a user in a date range.
     * 
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @return the average mood level
     */
    double calculateAverageMoodLevel(int userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find most common activities for a user.
     * 
     * @param userId the user ID
     * @param limit the maximum number of activities to return
     * @return a map of activity names to frequency counts
     */
    java.util.Map<String, Integer> findMostCommonActivities(int userId, int limit);
    
    /**
     * Delete all mood entries for a user.
     * 
     * @param userId the user ID
     * @return the number of entries deleted
     */
    int deleteByUserId(int userId);
} 