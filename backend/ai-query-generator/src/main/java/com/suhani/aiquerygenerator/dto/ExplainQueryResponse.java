package com.suhani.aiquerygenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for explaining an SQL query.
 * Provides a human-readable explanation of what the SQL query does.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplainQueryResponse {

    private String explanation;
}