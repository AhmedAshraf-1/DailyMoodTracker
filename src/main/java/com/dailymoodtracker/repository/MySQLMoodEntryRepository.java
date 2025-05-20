package com.dailymoodtracker.repository;

import com.dailymoodtracker.exception.DatabaseException;
import com.dailymoodtracker.model.MoodEntry;
import com.dailymoodtracker.service.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQL implementation of the MoodEntryRepository interface.
 */
public class MySQLMoodEntryRepository extends AbstractRepository<MoodEntry, Integer> implements MoodEntryRepository {
    private static final Logger logger = LoggerFactory.getLogger(MySQLMoodEntryRepository.class);
    
    public MySQLMoodEntryRepository(DatabaseService databaseService) {
        super(databaseService);
    }
    
    @Override
    protected String getTableName() {
        return "mood_entries";
    }
    
    @Override
    protected String getIdColumnName() {
        return "id";
    }
    
    @Override
    protected MoodEntry mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
        int moodLevel = rs.getInt("mood_level");
        String notes = rs.getString("notes");
        
        // Get activities for this mood entry
        List<String> activities = getActivitiesForMoodEntry(id);
        
        MoodEntry entry = new MoodEntry(id, userId, timestamp, moodLevel, notes, activities);
        return entry;
    }
    
    private List<String> getActivitiesForMoodEntry(int moodEntryId) {
        String sql = "SELECT a.activity_name FROM activities a " +
                     "JOIN mood_activities ma ON a.id = ma.activity_id " +
                     "WHERE ma.mood_entry_id = ?";
        
        List<String> activities = new ArrayList<>();
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, moodEntryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(rs.getString("activity_name"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting activities for mood entry ID {}", moodEntryId, e);
            throw new DatabaseException("Error getting activities for mood entry", e);
        }
        
        return activities;
    }
    
    @Override
    protected Object[] getInsertParameters(MoodEntry entry) {
        return new Object[] {
            entry.getUserId(),
            entry.getMoodLevel(),
            entry.getNotes(),
            Timestamp.valueOf(entry.getTimestamp())
        };
    }
    
    @Override
    protected Object[] getUpdateParameters(MoodEntry entry) {
        return new Object[] {
            entry.getMoodLevel(),
            entry.getNotes(),
            Timestamp.valueOf(entry.getTimestamp()),
            entry.getId()
        };
    }
    
    @Override
    protected String getInsertSql() {
        return "INSERT INTO mood_entries (user_id, mood_level, notes, timestamp) VALUES (?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSql() {
        return "UPDATE mood_entries SET mood_level = ?, notes = ?, timestamp = ? WHERE id = ?";
    }
    
    private int saveActivities(Connection conn, MoodEntry entry) throws SQLException {
        int moodEntryId = entry.getId();
        List<String> activities = entry.getActivities();
        int activitiesSaved = 0;
        
        if (activities == null || activities.isEmpty()) {
            return 0;
        }
        
        // First, delete existing activities for this mood entry
        try (PreparedStatement deleteStmt = conn.prepareStatement(
                "DELETE FROM mood_activities WHERE mood_entry_id = ?")) {
            deleteStmt.setInt(1, moodEntryId);
            deleteStmt.executeUpdate();
        }
        
        // Then, insert new activities
        for (String activityName : activities) {
            // Check if activity exists, create if not
            int activityId = getOrCreateActivity(conn, activityName, "General");
            
            // Link activity to mood entry
            try (PreparedStatement linkStmt = conn.prepareStatement(
                    "INSERT INTO mood_activities (mood_entry_id, activity_id) VALUES (?, ?)")) {
                linkStmt.setInt(1, moodEntryId);
                linkStmt.setInt(2, activityId);
                activitiesSaved += linkStmt.executeUpdate();
            }
        }
        
        return activitiesSaved;
    }
    
    private int getOrCreateActivity(Connection conn, String activityName, String category) throws SQLException {
        // Check if activity exists
        try (PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT id FROM activities WHERE activity_name = ?")) {
            checkStmt.setString(1, activityName);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        
        // Create new activity
        try (PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO activities (activity_name, category) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, activityName);
            insertStmt.setString(2, category);
            insertStmt.executeUpdate();
            
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to create activity, no ID obtained.");
                }
            }
        }
    }
    
    @Override
    public MoodEntry save(MoodEntry entry) {
        Connection conn = null;
        boolean isNewEntry = (entry.getId() == 0);
        
        try {
            conn = databaseService.getConnection();
            conn.setAutoCommit(false);
            
            if (isNewEntry) {
                // Insert new mood entry
                try (PreparedStatement stmt = conn.prepareStatement(getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
                    Object[] params = getInsertParameters(entry);
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }
                    
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Creating mood entry failed, no rows affected.");
                    }
                    
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            entry.setId(generatedKeys.getInt(1));
                        } else {
                            throw new SQLException("Creating mood entry failed, no ID obtained.");
                        }
                    }
                }
            } else {
                // Update existing mood entry
                try (PreparedStatement stmt = conn.prepareStatement(getUpdateSql())) {
                    Object[] params = getUpdateParameters(entry);
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }
                    
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Updating mood entry failed, no rows affected.");
                    }
                }
            }
            
            // Save associated activities
            saveActivities(conn, entry);
            
            conn.commit();
            return entry;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
            logger.error("Error saving mood entry", e);
            throw new DatabaseException("Error saving mood entry", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Error resetting auto-commit", e);
                }
            }
        }
    }
    
    @Override
    public boolean delete(MoodEntry entity) {
        return deleteById(entity.getId());
    }
    
    @Override
    public List<MoodEntry> findByUserId(int userId) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE user_id = ? ORDER BY timestamp DESC";
        return executeQueryForList(sql, userId);
    }
    
    @Override
    public List<MoodEntry> findByUserIdAndDateRange(int userId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE user_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        return executeQueryForList(sql, userId, Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
    }
    
    @Override
    public List<MoodEntry> findRecentByUserId(int userId, int limit) {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE user_id = ? ORDER BY timestamp DESC LIMIT ?";
        return executeQueryForList(sql, userId, limit);
    }
    
    @Override
    public double calculateAverageMoodLevel(int userId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT AVG(mood_level) FROM " + getTableName() + 
                    " WHERE user_id = ? AND timestamp BETWEEN ? AND ?";
        
        Optional<Double> result = executeQueryForValue(sql, Double.class, userId, 
                                                     Timestamp.valueOf(startDate), 
                                                     Timestamp.valueOf(endDate));
        return result.orElse(0.0);
    }
    
    @Override
    public Map<String, Integer> findMostCommonActivities(int userId, int limit) {
        String sql = "SELECT a.activity_name, COUNT(*) as count FROM activities a " +
                    "JOIN mood_activities ma ON a.id = ma.activity_id " +
                    "JOIN mood_entries me ON ma.mood_entry_id = me.id " +
                    "WHERE me.user_id = ? " +
                    "GROUP BY a.activity_name " +
                    "ORDER BY count DESC " +
                    "LIMIT ?";
        
        Map<String, Integer> activityCounts = new HashMap<>();
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String activityName = rs.getString("activity_name");
                    int count = rs.getInt("count");
                    activityCounts.put(activityName, count);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding most common activities for user ID {}", userId, e);
            throw new DatabaseException("Error finding most common activities", e);
        }
        
        return activityCounts;
    }
    
    @Override
    public int deleteByUserId(int userId) {
        Connection conn = null;
        
        try {
            conn = databaseService.getConnection();
            conn.setAutoCommit(false);
            
            // First, delete from mood_activities
            String deleteActivitiesSql = "DELETE FROM mood_activities " +
                                        "WHERE mood_entry_id IN (SELECT id FROM mood_entries WHERE user_id = ?)";
            
            int activitiesDeleted = 0;
            try (PreparedStatement stmt = conn.prepareStatement(deleteActivitiesSql)) {
                stmt.setInt(1, userId);
                activitiesDeleted = stmt.executeUpdate();
            }
            
            // Then, delete from mood_entries
            String deleteMoodsSql = "DELETE FROM " + getTableName() + " WHERE user_id = ?";
            
            int moodsDeleted = 0;
            try (PreparedStatement stmt = conn.prepareStatement(deleteMoodsSql)) {
                stmt.setInt(1, userId);
                moodsDeleted = stmt.executeUpdate();
            }
            
            conn.commit();
            return moodsDeleted;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
            logger.error("Error deleting mood entries for user ID {}", userId, e);
            throw new DatabaseException("Error deleting mood entries", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Error resetting auto-commit", e);
                }
            }
        }
    }
} 