package com.suhani.aiquerygenerator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for validating an SQL query.
 * Contains the SQL query string to be validated for syntax and safety.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateQueryRequest {

    @NotBlank(message = "SQL query cannot be blank")
    private String sql;
}