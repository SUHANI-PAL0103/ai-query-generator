package com.suhani.aiquerygenerator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for optimizing an SQL query.
 * Contains the SQL query string to be analyzed and optimized.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizeQueryRequest {

    @NotBlank(message = "SQL query cannot be blank")
    private String sql;
}