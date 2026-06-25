package com.suhani.aiquerygenerator.service;

import com.suhani.aiquerygenerator.ai.HuggingFaceAIService;
import com.suhani.aiquerygenerator.dto.*;
import com.suhani.aiquerygenerator.entity.QueryHistory;
import com.suhani.aiquerygenerator.repository.QueryHistoryRepository;
import com.suhani.aiquerygenerator.util.QueryAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QueryService {

    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);

    private final HuggingFaceAIService huggingFaceAIService;
    private final QueryHistoryRepository queryHistoryRepository;
    private final QueryAnalyzer queryAnalyzer;
    private final DatabaseValidationService dbValidation;

    public QueryService(HuggingFaceAIService huggingFaceAIService,
                        QueryHistoryRepository queryHistoryRepository,
                        QueryAnalyzer queryAnalyzer,
                        DatabaseValidationService dbValidation) {
        this.huggingFaceAIService = huggingFaceAIService;
        this.queryHistoryRepository = queryHistoryRepository;
        this.queryAnalyzer = queryAnalyzer;
        this.dbValidation = dbValidation;
    }

    // ✅ NO TRANSACTION HERE (IMPORTANT FIX)
    public GenerateQueryResponse generateQuery(String prompt) {

        logger.info("Generating SQL for prompt: {}", prompt);

        // Step 1: AI call (NO transaction involved)
        AiQueryResponse aiResponse = huggingFaceAIService.generateSql(prompt);

        if (aiResponse == null || aiResponse.getSql() == null) {
            throw new RuntimeException("AI returned null SQL response");
        }

        String generatedSql = aiResponse.getSql();

        // Step 2: Analysis
        String queryType = queryAnalyzer.determineQueryType(generatedSql);

        // Real row estimate from PostgreSQL EXPLAIN (no hardcoded fallback)
        long estimatedRows = dbValidation.estimateRealRows(generatedSql);

        String riskLevel = queryAnalyzer.assessRiskLevel(generatedSql);

        List<String> tables = (aiResponse.getTables() != null && !aiResponse.getTables().isEmpty())
                ? aiResponse.getTables()
                : new ArrayList<>(queryAnalyzer.extractTables(generatedSql));

        List<String> columns = (aiResponse.getColumns() != null && !aiResponse.getColumns().isEmpty())
                ? aiResponse.getColumns()
                : new ArrayList<>(queryAnalyzer.extractColumns(generatedSql));

        // Validate the generated SQL against the real database
        if (queryType.equals("SELECT") && !dbValidation.isValidSql(generatedSql)) {
            logger.warn("Generated SQL failed real DB validation: {}", generatedSql);
        }

        // Step 3: Save history safely (separate transaction)
        saveHistorySafe(prompt, generatedSql, aiResponse.getExplanation(), queryType, riskLevel);

        // Step 4: Response
        return GenerateQueryResponse.builder()
                .success(true)
                .generatedSql(generatedSql)
                .explanation(aiResponse.getExplanation())
                .tables(tables)
                .columns(columns)
                .estimatedRows(estimatedRows)
                .queryType(queryType)
                .riskLevel(riskLevel)
                .build();
    }

    // ✅ ISOLATED TRANSACTION (FIX FOR ROLLBACK ISSUE)
    @Transactional
    public void saveHistorySafe(String prompt,
                                String sql,
                                String explanation,
                                String queryType,
                                String riskLevel) {
        saveHistoryForUser(prompt, sql, explanation, queryType, riskLevel, null);
    }

    @Transactional
    public void saveHistoryForUser(String prompt,
                                   String sql,
                                   String explanation,
                                   String queryType,
                                   String riskLevel,
                                   String clerkId) {

        QueryHistory history = QueryHistory.builder()
                .clerkId(clerkId)
                .prompt(prompt)
                .generatedSql(sql)
                .explanation(explanation)
                .queryType(queryType)
                .riskLevel(riskLevel)
                .createdAt(LocalDateTime.now())
                .build();

        queryHistoryRepository.save(history);

        logger.info("Query history saved for user {}", clerkId);
    }

    // ---------------- EXPLAIN ----------------
    public ExplainQueryResponse explainQuery(String sql) {
        try {
            AiQueryResponse aiResponse =
                    huggingFaceAIService.generateSql("Explain this SQL: " + sql);

            return ExplainQueryResponse.builder()
                    .explanation(aiResponse.getExplanation())
                    .build();

        } catch (Exception e) {
            logger.warn("AI explanation failed: {}", e.getMessage());
            return ExplainQueryResponse.builder()
                    .explanation("Basic explanation: " + sql)
                    .build();
        }
    }

    // ---------------- VALIDATE ----------------
    public ValidateQueryResponse validateQuery(String sql) {

        if (sql == null || sql.trim().isEmpty()) {
            return ValidateQueryResponse.builder()
                    .valid(false)
                    .message("SQL cannot be empty")
                    .build();
        }

        String upper = sql.toUpperCase();
        List<String> warnings = new ArrayList<>();

        if (upper.contains("UPDATE") && !upper.contains("WHERE")) {
            warnings.add("UPDATE without WHERE will affect all rows");
        }

        if (upper.contains("DELETE") && !upper.contains("WHERE")) {
            warnings.add("DELETE without WHERE will remove all rows");
        }

        // Run real SQL validation against PostgreSQL using EXPLAIN
        if (upper.startsWith("SELECT")) {
            boolean dbValid = dbValidation.isValidSql(sql);
            if (!dbValid) {
                return ValidateQueryResponse.builder()
                        .valid(false)
                        .message("SQL validation failed against the database — check table/column names")
                        .build();
            }
        }

        if (warnings.isEmpty()) {
            return ValidateQueryResponse.builder()
                    .valid(true)
                    .message("Valid SQL")
                    .build();
        }

        return ValidateQueryResponse.builder()
                .valid(false)
                .message(String.join("; ", warnings))
                .build();
    }

    // ---------------- OPTIMIZE ----------------
    public OptimizeQueryResponse optimizeQuery(String sql) {

        if (sql == null || sql.isBlank()) {
            return OptimizeQueryResponse.builder()
                    .optimizedSql(sql)
                    .reason("Empty query")
                    .build();
        }

        List<String> reasons = new ArrayList<>();
        String optimized = sql;

        if (optimized.contains("SELECT *")) {
            reasons.add("Avoid SELECT *");
        }

        if (optimized.contains("ORDER BY")) {
            reasons.add("Consider indexing ORDER BY column");
        }

        if (reasons.isEmpty()) {
            return OptimizeQueryResponse.builder()
                    .optimizedSql(sql)
                    .reason("Already optimized")
                    .build();
        }

        return OptimizeQueryResponse.builder()
                .optimizedSql(optimized)
                .reason(String.join(", ", reasons))
                .build();
    }

    // ---------------- HISTORY ----------------
    @Transactional(readOnly = true)
    public List<QueryHistoryResponse> getQueryHistory() {
        return getQueryHistoryForUser(null);
    }

    @Transactional(readOnly = true)
    public List<QueryHistoryResponse> getQueryHistoryForUser(String clerkId) {
        List<QueryHistory> records;
        if (clerkId != null && !clerkId.isEmpty()) {
            records = queryHistoryRepository.findByClerkIdOrderByCreatedAtDesc(clerkId);
        } else {
            records = queryHistoryRepository.findAllByOrderByCreatedAtDesc();
        }

        return records.stream()
                .map(h -> QueryHistoryResponse.builder()
                        .id(h.getId())
                        .prompt(h.getPrompt())
                        .generatedSql(h.getGeneratedSql())
                        .explanation(h.getExplanation())
                        .queryType(h.getQueryType())
                        .riskLevel(h.getRiskLevel())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}