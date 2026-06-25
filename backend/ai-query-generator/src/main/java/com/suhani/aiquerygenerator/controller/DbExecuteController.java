package com.suhani.aiquerygenerator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for executing SQL queries directly against the real PostgreSQL database.
 * Supports SELECT queries (returns rows) and DDL/DML statements (CREATE, INSERT, UPDATE, DELETE, etc.).
 */
@RestController
@RequestMapping("/api/db")
public class DbExecuteController {

    private static final Logger logger = LoggerFactory.getLogger(DbExecuteController.class);

    private final JdbcTemplate jdbcTemplate;

    public DbExecuteController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes a SQL query/statement against the real database.
     * Supports SELECT (returns row data), INSERT/UPDATE/DELETE (returns affected count),
     * and DDL like CREATE/ALTER/DROP/TRUNCATE (returns success status).
     *
     * @param request Map containing the "sql" key
     * @return ResponseEntity with query results
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");

        if (sql == null || sql.trim().isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "SQL query cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        String trimmed = sql.trim().toUpperCase();

        try {
            logger.info("Executing SQL: {}", sql);

            // Handle SELECT queries - return rows
            if (trimmed.startsWith("SELECT")) {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("data", rows);
                response.put("rowCount", rows.size());
                response.put("queryType", "SELECT");

                logger.info("SELECT query returned {} rows", rows.size());
                return ResponseEntity.ok(response);
            }
            // Handle DDL statements (CREATE, ALTER, DROP, TRUNCATE) and DML (INSERT, UPDATE, DELETE)
            else {
                // Use jdbcTemplate.update() for DDL/DML - executes the statement safely once
                String nonQuotedSql = sql.trim();
                // Remove trailing semicolon if present to avoid issues with some drivers
                if (nonQuotedSql.endsWith(";")) {
                    nonQuotedSql = nonQuotedSql.substring(0, nonQuotedSql.length() - 1);
                }
                int affectedRows = jdbcTemplate.update(nonQuotedSql);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("queryType", "UPDATE");
                response.put("affectedRows", affectedRows);
                response.put("message", "Statement executed successfully");

                logger.info("Non-SELECT statement executed successfully. SQL: {}", sql);
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            logger.error("SQL execution error: {}", e.getMessage(), e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "SQL execution failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
