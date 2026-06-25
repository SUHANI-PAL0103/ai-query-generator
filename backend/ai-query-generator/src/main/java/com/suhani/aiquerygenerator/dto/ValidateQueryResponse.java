package com.suhani.aiquerygenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for SQL query validation.
 * Indicates whether the query is valid and provides any validation messages.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateQueryResponse {

    private boolean valid;
    private String message;
}