package com.dailymoodtracker.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface for CRUD operations.
 * @param <T> the entity type
 * @param <ID> the ID type
 */
public interface Repository<T, ID> {

    /**
     * Save an entity to the repository.
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);
    
    /**
     * Find an entity by its ID.
     * @param id the entity ID
     * @return an Optional containing the entity if found, empty otherwise
     */
    Optional<T> findById(ID id);
    
    /**
     * Find all entities.
     * @return a list of all entities
     */
    List<T> findAll();
    
    /**
     * Delete an entity by its ID.
     * @param id the entity ID
     * @return true if the entity was deleted, false otherwise
     */
    boolean deleteById(ID id);
    
    /**
     * Delete an entity.
     * @param entity the entity to delete
     * @return true if the entity was deleted, false otherwise
     */
    boolean delete(T entity);
    
    /**
     * Check if an entity exists by its ID.
     * @param id the entity ID
     * @return true if the entity exists, false otherwise
     */
    boolean existsById(ID id);
    
    /**
     * Count the number of entities.
     * @return the number of entities
     */
    long count();
} 