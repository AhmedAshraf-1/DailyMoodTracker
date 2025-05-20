package com.dailymoodtracker.service;

import com.dailymoodtracker.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;

public class ExportService {
    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    private final ObjectMapper objectMapper;

    public ExportService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void exportUserData(User user, String exportPath) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("mood_tracker_export_%s_%s.zip", user.getUsername(), timestamp);
            Path fullPath = Path.of(exportPath, fileName);

            Map<String, Object> exportData = new HashMap<>();
            exportData.put("user", user);
            exportData.put("moods", user.getMoods());
            exportData.put("goals", user.getGoals());

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(fullPath.toFile()))) {
                // Export user data
                ZipEntry userEntry = new ZipEntry("user.json");
                zos.putNextEntry(userEntry);
                byte[] userJson = objectMapper.writeValueAsBytes(exportData);
                zos.write(userJson);
                zos.closeEntry();

                // Export mood data with activities
                int moodCounter = 0;
                for (MoodEntry mood : user.getMoods()) {
                    ZipEntry moodEntry = new ZipEntry(String.format("moods/%d.json", moodCounter++));
                    zos.putNextEntry(moodEntry);
                    byte[] moodJson = objectMapper.writeValueAsBytes(mood);
                    zos.write(moodJson);
                    zos.closeEntry();
                }

                // Export goal data with achievements
                for (Goal goal : user.getGoals()) {
                    ZipEntry goalEntry = new ZipEntry(String.format("goals/%s.json", goal.getGoalId()));
                    zos.putNextEntry(goalEntry);
                    byte[] goalJson = objectMapper.writeValueAsBytes(goal);
                    zos.write(goalJson);
                    zos.closeEntry();
                }
            }

            logger.info("Successfully exported user data to: {}", fullPath);
        } catch (Exception e) {
            logger.error("Error exporting user data", e);
            throw new RuntimeException("Failed to export user data", e);
        }
    }

    public User importUserData(String importPath) {
        try {
            File importFile = new File(importPath);
            if (!importFile.exists()) {
                throw new FileNotFoundException("Import file not found: " + importPath);
            }

            User importedUser = null;
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(importFile))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals("user.json")) {
                        // Read user data
                        byte[] buffer = readEntryContent(zis);
                        Map<String, Object> userData = objectMapper.readValue(buffer, Map.class);
                        importedUser = objectMapper.convertValue(userData.get("user"), User.class);
                        
                        // Process moods
                        List<Map<String, Object>> moodsData = (List<Map<String, Object>>) userData.get("moods");
                        for (Map<String, Object> moodData : moodsData) {
                            MoodEntry mood = objectMapper.convertValue(moodData, MoodEntry.class);
                            importedUser.addMood(mood);
                        }
                        
                        // Process goals
                        List<Map<String, Object>> goalsData = (List<Map<String, Object>>) userData.get("goals");
                        for (Map<String, Object> goalData : goalsData) {
                            Goal goal = objectMapper.convertValue(goalData, Goal.class);
                            importedUser.addGoal(goal);
                        }
                    }
                    zis.closeEntry();
                }
            }

            if (importedUser == null) {
                throw new RuntimeException("No user data found in import file");
            }

            logger.info("Successfully imported user data for: {}", importedUser.getUsername());
            return importedUser;
        } catch (Exception e) {
            logger.error("Error importing user data", e);
            throw new RuntimeException("Failed to import user data", e);
        }
    }

    private byte[] readEntryContent(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = zis.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }

    public void exportToCSV(User user, String exportPath) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("mood_tracker_%s_%s.csv", user.getUsername(), timestamp);
            Path fullPath = Path.of(exportPath, fileName);

            try (BufferedWriter writer = Files.newBufferedWriter(fullPath)) {
                // Write header
                writer.write("Date,Mood Level,Notes,Activities\n");

                // Write mood entries
                for (MoodEntry mood : user.getMoods()) {
                    StringBuilder line = new StringBuilder();
                    line.append(mood.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append(",");
                    line.append(mood.getMoodLevel()).append(",");
                    line.append(escapeCSV(mood.getNotes())).append(",");
                    
                    // Add activities
                    StringBuilder activities = new StringBuilder();
                    for (String activity : mood.getActivities()) {
                        if (activities.length() > 0) activities.append(";");
                        activities.append(activity);
                    }
                    line.append(escapeCSV(activities.toString()));
                    
                    writer.write(line.toString());
                    writer.write("\n");
                }
            }

            logger.info("Successfully exported CSV data to: {}", fullPath);
        } catch (Exception e) {
            logger.error("Error exporting CSV data", e);
            throw new RuntimeException("Failed to export CSV data", e);
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
} 