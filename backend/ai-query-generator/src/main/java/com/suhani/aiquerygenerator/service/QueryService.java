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
        List<AiQueryResponse> aiResponses;
        try {
            aiResponses = huggingFaceAIService.generateSql(prompt);
        } catch (Exception e) {
            logger.error("AI service failed: {}", e.getMessage());
            aiResponses = buildFallbackQueries(prompt);
            if (aiResponses == null || aiResponses.isEmpty()) {
                return GenerateQueryResponse.builder()
                        .success(false)
                        .generatedSql(null)
                        .explanation("AI service is unavailable and no matching table was found in the connected database schema. Set HUGGINGFACE_API_KEY or try a prompt that mentions an existing table.")
                        .tables(new ArrayList<>())
                        .columns(new ArrayList<>())
                        .estimatedRows(0)
                        .queryType("UNKNOWN")
                        .riskLevel("High")
                        .build();
            }
        }

        if (aiResponses == null || aiResponses.isEmpty() || aiResponses.get(0).getSql() == null) {
            return GenerateQueryResponse.builder()
                    .success(false)
                    .generatedSql(null)
                    .explanation("AI service returned an empty response. Please try again.")
                    .tables(new ArrayList<>())
                    .columns(new ArrayList<>())
                    .estimatedRows(0)
                    .queryType("UNKNOWN")
                    .riskLevel("High")
                    .build();
        }

        // Select the best query based on confidence score
        AiQueryResponse bestResponse = aiResponses.stream()
                .max(Comparator.comparingDouble(r -> r.getConfidence() != null ? r.getConfidence() : 0.0))
                .orElse(aiResponses.get(0));

        String generatedSql = bestResponse.getSql();

        // Step 2: Analysis
        String queryType = queryAnalyzer.determineQueryType(generatedSql);

        // Real row estimate from PostgreSQL EXPLAIN (no hardcoded fallback)
        long estimatedRows = dbValidation.estimateRealRows(generatedSql);

        String riskLevel = queryAnalyzer.assessRiskLevel(generatedSql);

        List<String> tables = (bestResponse.getTables() != null && !bestResponse.getTables().isEmpty())
                ? bestResponse.getTables()
                : new ArrayList<>(queryAnalyzer.extractTables(generatedSql));

        List<String> columns = (bestResponse.getColumns() != null && !bestResponse.getColumns().isEmpty())
                ? bestResponse.getColumns()
                : new ArrayList<>(queryAnalyzer.extractColumns(generatedSql));

        // Validate the generated SQL against the real database
        if (queryType.equals("SELECT") && !dbValidation.isValidSql(generatedSql)) {
            logger.warn("Generated SQL failed real DB validation: {}", generatedSql);
        }

        // Step 3: Save history safely (separate transaction)
        try {
            saveHistorySafe(prompt, generatedSql, bestResponse.getExplanation(), queryType, riskLevel);
        } catch (Exception e) {
            logger.warn("Could not save query history: {}", e.getMessage());
        }

        // Step 4: Response
        return GenerateQueryResponse.builder()
                .success(true)
                .generatedSql(generatedSql)
                .explanation(bestResponse.getExplanation())
                .tables(tables)
                .columns(columns)
                .estimatedRows(estimatedRows)
                .queryType(queryType)
                .riskLevel(riskLevel)
                .build();
    }

    private List<AiQueryResponse> buildFallbackQueries(String prompt) {
        List<String> tables = dbValidation.getRealTables();
        if (tables.isEmpty()) {
            return null;
        }

        String normalizedPrompt = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        String selectedTable = tables.stream()
                .filter(table -> promptMentionsTable(normalizedPrompt, table))
                .findFirst()
                .orElse(tables.size() == 1 ? tables.get(0) : null);

        if (selectedTable == null) {
            return null;
        }

        String quotedTable = quoteIdentifier(selectedTable);
        String sql = normalizedPrompt.contains("count")
                ? "SELECT COUNT(*) AS total FROM " + quotedTable
                : "SELECT * FROM " + quotedTable + " LIMIT 100";

        AiQueryResponse fallback = AiQueryResponse.builder()
                .sql(sql)
                .explanation("Generated from the connected database schema because the AI service was unavailable.")
                .tables(List.of(selectedTable))
                .columns(dbValidation.getRealColumns(selectedTable))
                .confidence(0.6)
                .build();

        return List.of(fallback);
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private boolean promptMentionsTable(String normalizedPrompt, String table) {
        String normalizedTable = table.toLowerCase(Locale.ROOT);
        String readableTable = normalizedTable.replace("_", " ");
        String singularTable = singularize(readableTable);

        return normalizedPrompt.contains(normalizedTable)
                || normalizedPrompt.contains(readableTable)
                || normalizedPrompt.contains(singularTable);
    }

    private String singularize(String value) {
        if (value.endsWith("ies") && value.length() > 3) {
            return value.substring(0, value.length() - 3) + "y";
        }
        if (value.endsWith("s") && value.length() > 1) {
            return value.substring(0, value.length() - 1);
        }
        return value;
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
            List<AiQueryResponse> aiResponses =
                    huggingFaceAIService.generateSql("Explain this SQL: " + sql);
            AiQueryResponse aiResponse = aiResponses != null && !aiResponses.isEmpty() ? aiResponses.get(0) : null;

            return ExplainQueryResponse.builder()
                    .explanation(aiResponse != null ? aiResponse.getExplanation() : "Basic explanation: " + sql)
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
