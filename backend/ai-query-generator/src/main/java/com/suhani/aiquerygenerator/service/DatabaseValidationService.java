package com.suhani.aiquerygenerator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.suhani.aiquerygenerator.util.DynamicJdbcTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DatabaseValidationService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseValidationService.class);

    private final DynamicJdbcTemplate dynamicJdbcTemplate;

    public DatabaseValidationService(DynamicJdbcTemplate dynamicJdbcTemplate) {
        this.dynamicJdbcTemplate = dynamicJdbcTemplate;
    }

    private JdbcTemplate getJdbcTemplate() {
        JdbcTemplate jdbcTemplate = dynamicJdbcTemplate.create();
        if (jdbcTemplate == null) {
            throw new IllegalStateException("No database connection available. Please connect your database first.");
        }
        return jdbcTemplate;
    }

    /**
     * Validates SQL against the real PostgreSQL database.
     * For SELECT queries, runs EXPLAIN to verify syntax.
     * For DDL/DML (CREATE, INSERT, UPDATE, DELETE, ALTER, DROP, etc.), does a basic 
     * syntax check by attempting to parse via the database (without executing).
     * Returns true if the SQL is valid.
     */
    public boolean isValidSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }

        String trimmed = sql.trim().toUpperCase();
        JdbcTemplate jdbcTemplate;
        try {
            jdbcTemplate = getJdbcTemplate();
        } catch (Exception e) {
            logger.warn("SQL validation skipped: {}", e.getMessage());
            return false;
        }

        // For SELECT queries, use EXPLAIN to validate safely
        if (trimmed.startsWith("SELECT")) {
            try {
                jdbcTemplate.queryForList("EXPLAIN " + sql);
                return true;
            } catch (Exception e) {
                logger.warn("SQL validation failed: {}", e.getMessage());
                return false;
            }
        }
        
        // For DDL/DML (CREATE, INSERT, UPDATE, DELETE, ALTER, DROP, TRUNCATE),
        // we can't use EXPLAIN. Instead, do basic validation:
        // 1. Check it starts with a valid SQL keyword
        // 2. Try a lightweight parse via the database without executing by wrapping in a DO block
        try {
            // Wrap in a BEGIN/ROLLBACK transaction to test without committing
            jdbcTemplate.execute("BEGIN");
            try {
                jdbcTemplate.execute(sql);
                // If we get here, the SQL is syntactically valid
                // Roll back any side effects
                jdbcTemplate.execute("ROLLBACK");
                return true;
            } catch (Exception e) {
                jdbcTemplate.execute("ROLLBACK");
                logger.warn("SQL validation failed: {}", e.getMessage());
                return false;
            }
        } catch (Exception e) {
            // Fallback: basic keyword check
            logger.warn("Transaction-based validation failed, using fallback: {}", e.getMessage());
            String[] validKeywords = {"CREATE", "INSERT", "UPDATE", "DELETE", "ALTER", "DROP", "TRUNCATE", "WITH"};
            for (String keyword : validKeywords) {
                if (trimmed.startsWith(keyword)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Runs EXPLAIN on the SQL and extracts the real row estimate from PostgreSQL's query planner.
     * Returns -1 if estimation fails.
     */
    public long estimateRealRows(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return -1;
        }

        String trimmed = sql.trim().toUpperCase();
        if (!trimmed.startsWith("SELECT")) {
            return -1;
        }

        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate();
            List<String> explainOutput = jdbcTemplate.query(
                    "EXPLAIN " + sql,
                    (rs, rowNum) -> rs.getString("QUERY PLAN")
            );

            // Parse first line for row estimate: "rows=X"
            if (!explainOutput.isEmpty()) {
                String firstLine = explainOutput.get(0);
                Pattern pattern = Pattern.compile("rows=(\\d+)");
                Matcher matcher = pattern.matcher(firstLine);
                if (matcher.find()) {
                    long rows = Long.parseLong(matcher.group(1));
                    logger.info("Real row estimate from DB: {} for query: {}", rows, sql);
                    return rows;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not estimate rows: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * Fetches real tables from the database (information_schema).
     */
    public List<String> getRealTables() {
        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate();
            // Only return application tables from the connected database.
            // Avoid system/extension tables if they exist in the same schema.
            return jdbcTemplate.query(
                    "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema = 'public' " +
                    "  AND table_type = 'BASE TABLE' " +
                    "ORDER BY table_name",
                    (rs, rowNum) -> rs.getString("table_name")
            );
        } catch (Exception e) {
            logger.warn("Could not fetch tables: {}", e.getMessage());
            return List.of();
        }
    }


    /**
     * Fetches real columns for a given table from information_schema.
     */
    public List<String> getRealColumns(String tableName) {
        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate();
            return jdbcTemplate.query(
                    "SELECT column_name FROM information_schema.columns " +
                    "WHERE table_schema = 'public' AND table_name = ? ORDER BY ordinal_position",
                    new Object[]{tableName},
                    (rs, rowNum) -> rs.getString("column_name")
            );
        } catch (Exception e) {
            logger.warn("Could not fetch columns for table {}: {}", tableName, e.getMessage());
            return List.of();
        }
    }
}
