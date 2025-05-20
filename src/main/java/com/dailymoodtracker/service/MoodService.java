package com.dailymoodtracker.service;

import com.dailymoodtracker.model.MoodEntry;
import com.dailymoodtracker.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoodService {
    private static final Logger logger = LoggerFactory.getLogger(MoodService.class);
    private final List<MoodEntry> entries;
    private final ExportService exportService;

    public MoodService() {
        this.entries = new ArrayList<>();
        this.exportService = new ExportService();
    }

    public void saveEntry(MoodEntry entry) {
        try {
            entries.add(entry);
            logger.info("Mood entry saved successfully");
        } catch (Exception e) {
            logger.error("Failed to save mood entry", e);
            throw new RuntimeException("Could not save mood entry", e);
        }
    }

    public List<MoodEntry> getAllEntries() {
        try {
            return new ArrayList<>(entries);
        } catch (Exception e) {
            logger.error("Failed to retrieve mood entries", e);
            throw new RuntimeException("Could not retrieve mood entries", e);
        }
    }

    public List<MoodEntry> getEntriesByMoodLevel(int moodLevel) {
        try {
            return entries.stream()
                    .filter(entry -> entry.getMoodLevel() == moodLevel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to retrieve mood entries by level", e);
            throw new RuntimeException("Could not retrieve mood entries", e);
        }
    }

    public void updateEntry(MoodEntry entry) {
        try {
            int index = -1;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getTimestamp().equals(entry.getTimestamp())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                entries.set(index, entry);
                logger.info("Mood entry updated successfully");
            }
        } catch (Exception e) {
            logger.error("Failed to update mood entry", e);
            throw new RuntimeException("Could not update mood entry", e);
        }
    }

    public void deleteEntry(int id) {
        try {
            if (id >= 0 && id < entries.size()) {
                entries.remove(id);
                logger.info("Mood entry deleted successfully");
            }
        } catch (Exception e) {
            logger.error("Failed to delete mood entry", e);
            throw new RuntimeException("Could not delete mood entry", e);
        }
    }

    public ExportService getExportService() {
        return exportService;
    }

    public List<MoodEntry> getRecentEntries(User user, int count) {
        return entries.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
} 