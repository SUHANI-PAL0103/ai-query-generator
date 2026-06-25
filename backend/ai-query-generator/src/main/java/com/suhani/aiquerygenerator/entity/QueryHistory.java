package com.suhani.aiquerygenerator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing the query history table.
 * Stores every generated SQL query along with its metadata,
 * including the original prompt, generated SQL, explanation,
 * query type, and risk level for audit and history tracking.
 * Each record is linked to a user via clerk_id for data isolation.
 */
@Entity
@Table(name = "query_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clerk_id", length = 255)
    private String clerkId;

    @Column(name = "prompt", columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(name = "generated_sql", columnDefinition = "TEXT", nullable = false)
    private String generatedSql;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "query_type", length = 50)
    private String queryType;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
}