package com.suhani.aiquerygenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for query history entries.
 * Represents a single entry in the query generation history.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryHistoryResponse {

    private Long id;
    private String prompt;
    private String generatedSql;
    private String explanation;
    private String queryType;
    private String riskLevel;
    private LocalDateTime createdAt;
}