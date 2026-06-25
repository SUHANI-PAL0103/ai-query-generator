package com.suhani.aiquerygenerator.controller;

import com.suhani.aiquerygenerator.util.DynamicJdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for executing SQL queries directly against the user's real PostgreSQL database.
 * Uses dynamic connection from DatabaseContext.
 */
@RestController
@RequestMapping("/api/db")
public class DbExecuteController {

    private static final Logger logger = LoggerFactory.getLogger(DbExecuteController.class);

    private final DynamicJdbcTemplate dynamicJdbcTemplate;

    public DbExecuteController(DynamicJdbcTemplate dynamicJdbcTemplate) {
        this.dynamicJdbcTemplate = dynamicJdbcTemplate;
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");

        if (sql == null || sql.trim().isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "SQL query cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        JdbcTemplate jt = dynamicJdbcTemplate.create();
        if (jt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "No database connection. Please connect your database first."));
        }

        String trimmed = sql.trim().toUpperCase();

        try {
            logger.info("Executing SQL: {}", sql);

            if (trimmed.startsWith("SELECT")) {
                List<Map<String, Object>> rows = jt.queryForList(sql);
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("data", rows);
                response.put("rowCount", rows.size());
                response.put("queryType", "SELECT");
                logger.info("SELECT query returned {} rows", rows.size());
                return ResponseEntity.ok(response);
            } else {
                String nonQuotedSql = sql.trim();
                if (nonQuotedSql.endsWith(";")) {
                    nonQuotedSql = nonQuotedSql.substring(0, nonQuotedSql.length() - 1);
                }
                int affectedRows = jt.update(nonQuotedSql);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "SQL execution failed: " + e.getMessage()));
        }
    }
}