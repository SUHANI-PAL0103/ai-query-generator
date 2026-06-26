package com.suhani.aiquerygenerator.repository;

import com.suhani.aiquerygenerator.entity.QueryHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for QueryHistory entity.
 * Provides database operations for storing and retrieving query history records.
 * Extends MongoRepository to inherit standard CRUD operations for MongoDB.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Repository
public interface QueryHistoryRepository extends MongoRepository<QueryHistory, String> {

    /**
     * Retrieves all query history records ordered by creation date descending.
     */
    List<QueryHistory> findAllByOrderByCreatedAtDesc();

    /**
     * Retrieve query history for a specific user (Clerk ID).
     */
    List<QueryHistory> findByClerkIdOrderByCreatedAtDesc(String clerkId);

    /**
     * Count queries for a specific user.
     */
    long countByClerkId(String clerkId);

    /**
     * Count queries created after a specific date for a user.
     */
    long countByClerkIdAndCreatedAtAfter(String clerkId, LocalDateTime date);

    /**
     * Count queries created after a specific date.
     */
    long countByCreatedAtAfter(LocalDateTime date);

    /**
     * Count queries created between two dates.
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Count queries grouped by query type - returns list of [queryType, count] pairs.
     */
    @Query(value = "{}", fields = "{ 'query_type': 1 }")
    List<QueryHistory> findAllQueryTypes();

    /**
     * Count queries grouped by risk level - returns list of [riskLevel, count] pairs.
     */
    @Query(value = "{}", fields = "{ 'risk_level': 1 }")
    List<QueryHistory> findAllRiskLevels();

    /**
     * Find average execution time from history.
     */
    @Query(value = "{ 'execution_time_ms': { $ne: null } }", fields = "{ 'execution_time_ms': 1 }")
    List<QueryHistory> findByExecutionTimeMsIsNotNull();

    /**
     * Get top 10 recent records ordered by creation date descending.
     */
    List<QueryHistory> findTop10ByOrderByCreatedAtDesc();
}
