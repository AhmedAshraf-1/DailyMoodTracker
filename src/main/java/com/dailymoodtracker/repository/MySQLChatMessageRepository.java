package com.dailymoodtracker.repository;

import com.dailymoodtracker.model.ChatMessage;
import com.dailymoodtracker.model.SentimentResult;
import com.dailymoodtracker.service.DatabaseService;
import com.dailymoodtracker.exception.DatabaseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for chat messages using MySQL database.
 */
public class MySQLChatMessageRepository implements ChatMessageRepository {
    private static final Logger logger = LoggerFactory.getLogger(MySQLChatMessageRepository.class);
    
    private final DatabaseService dbService;
    
    public MySQLChatMessageRepository(DatabaseService dbService) {
        this.dbService = dbService;
        initializeTable();
    }
    
    private void initializeTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS chat_messages (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                user_id INTEGER NOT NULL,
                sender VARCHAR(50) NOT NULL,
                content TEXT NOT NULL,
                timestamp TIMESTAMP NOT NULL,
                sentiment VARCHAR(50),
                positive_score DOUBLE,
                negative_score DOUBLE,
                neutral_score DOUBLE
            )
        """;
            
        try (Connection conn = dbService.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("Chat messages table initialized");
        } catch (SQLException e) {
            logger.error("Error initializing chat messages table", e);
        }
    }
    
    @Override
    public void save(ChatMessage message) {
        String sql = """
            INSERT INTO chat_messages (user_id, sender, content, timestamp, sentiment, 
                                      positive_score, negative_score, neutral_score)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // For demonstration purposes, we'll use a default user ID of 1
            int userId = 1;
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, message.getType().toString());
            pstmt.setString(3, message.getContent());
            pstmt.setTimestamp(4, Timestamp.valueOf(message.getTimestamp()));
            
            // Handle sentiment if available
            if (message.hasSentiment()) {
                SentimentResult sentiment = message.getSentiment();
                pstmt.setString(5, sentiment.getOverallSentiment());
                pstmt.setDouble(6, sentiment.getPositiveScore());
                pstmt.setDouble(7, sentiment.getNegativeScore());
                pstmt.setDouble(8, sentiment.getNeutralScore());
            } else {
                pstmt.setNull(5, java.sql.Types.VARCHAR);
                pstmt.setNull(6, java.sql.Types.DOUBLE);
                pstmt.setNull(7, java.sql.Types.DOUBLE);
                pstmt.setNull(8, java.sql.Types.DOUBLE);
            }
                    
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating chat message failed, no rows affected.");
            }
            
            logger.debug("Saved chat message to database");
            
        } catch (SQLException e) {
            logger.error("Error saving chat message", e);
            throw new DatabaseException("Error saving chat message", e);
        }
    }
    
    @Override
    public List<ChatMessage> findRecentByUserId(int userId, int limit) {
        String sql = """
            SELECT id, user_id, sender, content, timestamp, sentiment, 
                   positive_score, negative_score, neutral_score
            FROM chat_messages
            WHERE user_id = ?
            ORDER BY timestamp DESC
            LIMIT ?
        """;
        
        List<ChatMessage> messages = new ArrayList<>();
        
        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(createChatMessageFromResultSet(rs));
                }
            }
            
            logger.debug("Found {} recent chat messages for user {}", messages.size(), userId);
            
        } catch (SQLException e) {
            logger.error("Error finding recent chat messages", e);
            throw new DatabaseException("Error finding recent chat messages", e);
        }
    
        return messages;
    }
    
    private ChatMessage createChatMessageFromResultSet(ResultSet rs) throws SQLException {
        String senderStr = rs.getString("sender");
        String content = rs.getString("content");
        LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
        
        // Convert sender string to enum
        ChatMessage.MessageType sender = ChatMessage.MessageType.valueOf(senderStr);
        
        // Create message
        ChatMessage message = new ChatMessage(content, sender);
        
        // Set timestamp from database
        message.setTimestamp(timestamp);
        
        // Add sentiment if available
        String sentimentStr = rs.getString("sentiment");
        if (sentimentStr != null) {
            double positiveScore = rs.getDouble("positive_score");
            double negativeScore = rs.getDouble("negative_score");
            double neutralScore = rs.getDouble("neutral_score");
            
            // Create sentiment result (using default user ID of 1)
            SentimentResult sentiment = new SentimentResult(
                content, positiveScore, negativeScore, neutralScore, sentimentStr, 1);
                
            message.setSentiment(sentiment);
        }
        
        return message;
    }
} 