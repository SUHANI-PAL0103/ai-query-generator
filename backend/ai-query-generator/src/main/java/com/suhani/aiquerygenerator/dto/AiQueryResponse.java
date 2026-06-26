package com.suhani.aiquerygenerator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for parsing the structured JSON response from the Hugging Face AI model.
 * The AI is prompted to return SQL query details in this exact format,
 * containing the generated SQL, explanation, and metadata about tables/columns.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiQueryResponse {

    @JsonProperty("sql")
    private String sql;

    @JsonProperty("explanation")
    private String explanation;

    @JsonProperty("tables")
    private List<String> tables;

    @JsonProperty("columns")
    private List<String> columns;

    @JsonProperty("confidence")
    private Double confidence;
}
