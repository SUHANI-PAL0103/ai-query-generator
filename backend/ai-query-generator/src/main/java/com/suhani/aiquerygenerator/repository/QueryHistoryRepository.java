package com.suhani.aiquerygenerator.repository;

import com.suhani.aiquerygenerator.entity.QueryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for QueryHistory entity.
 * Provides database operations for storing and retrieving query history records.
 * Extends JpaRepository to inherit standard CRUD operations.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Repository
public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Long> {

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
     * Count queries grouped by query type.
     */
    @Query("SELECT q.queryType, COUNT(q) FROM QueryHistory q GROUP BY q.queryType")
    List<Object[]> countByQueryType();

    /**
     * Count queries grouped by risk level.
     */
    @Query("SELECT q.riskLevel, COUNT(q) FROM QueryHistory q GROUP BY q.riskLevel")
    List<Object[]> countByRiskLevel();

    /**
     * Find average execution time from history.
     */
    @Query("SELECT AVG(q.executionTimeMs) FROM QueryHistory q WHERE q.executionTimeMs IS NOT NULL")
    Double findAverageExecutionTime();

    /**
     * Get top 10 recent records ordered by creation date descending.
     */
    List<QueryHistory> findTop10ByOrderByCreatedAtDesc();
}
