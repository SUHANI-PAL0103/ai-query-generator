package com.suhani.aiquerygenerator.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MongoDB document for storing query history.
 * Stores every generated SQL query along with its metadata,
 * including the original prompt, generated SQL, explanation,
 * query type, and risk level for audit and history tracking.
 * Each record is linked to a user via clerk_id for data isolation.
 */
@Document(collection = "query_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryHistory {

    @Id
    private String id;

    @Field("clerk_id")
    private String clerkId;

    @Field("prompt")
    private String prompt;

    @Field("generated_sql")
    private String generatedSql;

    @Field("explanation")
    private String explanation;

    @Field("query_type")
    private String queryType;

    @Field("risk_level")
    private String riskLevel;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("execution_time_ms")
    private Long executionTimeMs;
}
