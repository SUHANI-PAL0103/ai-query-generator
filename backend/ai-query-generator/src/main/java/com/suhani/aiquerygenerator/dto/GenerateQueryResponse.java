package com.suhani.aiquerygenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for the generated SQL query.
 * Contains the generated SQL, explanation, metadata about tables/columns involved,
 * estimated impact analysis, and risk assessment.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQueryResponse {

    private boolean success;
    private String generatedSql;
    private String explanation;
    private List<String> tables;
    private List<String> columns;
    private long estimatedRows;
    private String queryType;
    private String riskLevel;
}