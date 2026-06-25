package com.suhani.aiquerygenerator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for generating an SQL query from a natural language prompt.
 * Contains the user's plain English requirement to be converted into SQL.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQueryRequest {

    @NotBlank(message = "Prompt cannot be blank")
    private String prompt;
}