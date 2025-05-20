package com.dailymoodtracker.repository;

import com.dailymoodtracker.service.DatabaseService;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of the Repository interface.
 * Provides common functionality for all repositories.
 * 
 * @param <T> the entity type
 * @param <ID> the ID type
 */
public abstract class AbstractRepository<T, ID> implements Repository<T, ID> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final DatabaseService databaseService;
    
    /**
     * Create a new AbstractRepository with the given database service.
     * 
     * @param databaseService the database service to use
     */
    protected AbstractRepository(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    
    /**
     * Get the table name for this repository.
     * 
     * @return the table name
     */
    protected abstract String getTableName();
    
    /**
     * Get the ID column name for this repository.
     * 
     * @return the ID column name
     */
    protected abstract String getIdColumnName();
    
    /**
     * Map a database row to an entity.
     * 
     * @param row the database row
     * @return the entity
     */
    protected abstract T mapRow(java.sql.ResultSet row) throws SQLException;
    
    /**
     * Get the parameters for inserting an entity.
     * 
     * @param entity the entity to insert
     * @return the parameters
     */
    protected abstract Object[] getInsertParameters(T entity);
    
    /**
     * Get the parameters for updating an entity.
     * 
     * @param entity the entity to update
     * @return the parameters
     */
    protected abstract Object[] getUpdateParameters(T entity);
    
    /**
     * Get the SQL for inserting an entity.
     * 
     * @return the SQL
     */
    protected abstract String getInsertSql();
    
    /**
     * Get the SQL for updating an entity.
     * 
     * @return the SQL
     */
    protected abstract String getUpdateSql();
    
    /**
     * Execute a query that returns a list of entities.
     * 
     * @param sql the SQL query
     * @param params the query parameters
     * @return a list of entities
     */
    protected List<T> executeQueryForList(String sql, Object... params) {
        List<T> results = new java.util.ArrayList<>();
        
        try (java.sql.Connection conn = databaseService.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            // Execute query
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new com.dailymoodtracker.exception.DatabaseException("Error executing database query", e);
        }
        
        return results;
    }
    
    /**
     * Execute a query that returns a single entity.
     * 
     * @param sql the SQL query
     * @param params the query parameters
     * @return an Optional containing the entity if found, empty otherwise
     */
    protected Optional<T> executeQueryForObject(String sql, Object... params) {
        try (java.sql.Connection conn = databaseService.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            // Execute query
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new com.dailymoodtracker.exception.DatabaseException("Error executing database query", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Execute a query that returns a single value.
     * 
     * @param sql the SQL query
     * @param params the query parameters
     * @param <R> the result type
     * @return an Optional containing the value if found, empty otherwise
     */
    protected <R> Optional<R> executeQueryForValue(String sql, Class<R> resultType, Object... params) {
        try (java.sql.Connection conn = databaseService.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            // Execute query
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getObject(1, resultType));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new com.dailymoodtracker.exception.DatabaseException("Error executing database query", e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        return executeQueryForValue(sql, Long.class).orElse(0L);
    }
    
    @Override
    public boolean existsById(ID id) {
        String sql = "SELECT 1 FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        return executeQueryForValue(sql, Integer.class, id).isPresent();
    }
    
    @Override
    public List<T> findAll() {
        String sql = "SELECT * FROM " + getTableName();
        return executeQueryForList(sql);
    }
    
    @Override
    public Optional<T> findById(ID id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        return executeQueryForObject(sql, id);
    }
    
    @Override
    public boolean deleteById(ID id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try {
            int rowsAffected = databaseService.executeUpdate(sql, id);
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error deleting entity with ID {}", id, e);
            throw new com.dailymoodtracker.exception.DatabaseException("Error deleting entity", e);
        }
    }
} 