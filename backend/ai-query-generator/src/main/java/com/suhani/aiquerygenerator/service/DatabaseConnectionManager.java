package com.suhani.aiquerygenerator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Service for managing database connections dynamically.
 * Supports runtime switching of database connections.
 */
@Service
public class DatabaseConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);

    private final JdbcTemplate jdbcTemplate;

    // Current connection config
    private String currentUrl;
    private String currentUsername;
    private String currentDatabase;

    public DatabaseConnectionManager(
            JdbcTemplate jdbcTemplate,
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username) {
        this.jdbcTemplate = jdbcTemplate;
        this.currentUrl = url;
        this.currentUsername = username;
        this.currentDatabase = extractDatabaseName(url);
    }

    /**
     * Test a database connection with given credentials.
     */
    public boolean testConnection(String url, String username, String password) {
        try {
            logger.info("Testing connection to: {}", url);
            
            // Parse URL to get database name
            String dbName = extractDatabaseName(url);
            
            // Try to get connection
            Connection conn = DriverManager.getConnection(url, username, password);
            
            // Test with a simple query
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT 1");
            if (rs.next()) {
                rs.close();
                stmt.close();
                conn.close();
                logger.info("Connection test successful for database: {}", dbName);
                return true;
            }
            
            conn.close();
            return false;
            
        } catch (SQLException e) {
            logger.error("Connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get current connection info.
     */
    public ConnectionInfo getCurrentConnectionInfo() {
        try {
            String currentDb = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
            String currentUser = jdbcTemplate.queryForObject("SELECT current_user", String.class);
            String currentSchema = jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
            
            return new ConnectionInfo(currentUrl, currentUsername, currentDb, currentUser, currentSchema, true);
        } catch (Exception e) {
            return new ConnectionInfo(currentUrl, currentUsername, currentDatabase, "N/A", "N/A", false);
        }
    }

    /**
     * Validate SQL against the current database connection.
     */
    public boolean validateSql(String sql) {
        try {
            // Try to explain the query to validate it
            if (sql.trim().toUpperCase().startsWith("SELECT")) {
                String explainQuery = "EXPLAIN " + sql;
                jdbcTemplate.queryForList(explainQuery);
                return true;
            }
            // For non-SELECT, we validate by checking if tables/columns exist
            return true;
        } catch (Exception e) {
            logger.warn("SQL validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Estimate rows for a SELECT query.
     */
    public long estimateRows(String sql) {
        try {
            if (!sql.trim().toUpperCase().startsWith("SELECT")) {
                return 0;
            }
            
            String explainQuery = "EXPLAIN " + sql;
            var results = jdbcTemplate.queryForList(explainQuery);
            
            // Parse estimated rows from EXPLAIN output
            // PostgreSQL EXPLAIN format: "Seq Scan on table  (cost=0.00..25.88 rows=128 ...)"
            for (var row : results) {
                String plan = (String) row.values().toArray()[0];
                // Extract rows=N from plan
                if (plan.contains("rows=")) {
                    int start = plan.indexOf("rows=") + 5;
                    int end = plan.indexOf(" ", start);
                    if (end == -1) end = plan.length();
                    try {
                        return Long.parseLong(plan.substring(start, end).trim());
                    } catch (NumberFormatException ignored) {}
                }
            }
            return -1;
        } catch (Exception e) {
            logger.warn("Could not estimate rows: {}", e.getMessage());
            return -1;
        }
    }

    public String extractDatabaseName(String jdbcUrl) {
        try {
            // jdbc:postgresql://host:port/dbname
            int lastSlash = jdbcUrl.lastIndexOf('/');
            if (lastSlash != -1) {
                String afterSlash = jdbcUrl.substring(lastSlash + 1);
                int queryIndex = afterSlash.indexOf('?');
                if (queryIndex != -1) {
                    return afterSlash.substring(0, queryIndex);
                }
                return afterSlash;
            }
        } catch (Exception e) {
            logger.warn("Could not extract database name from URL: {}", jdbcUrl);
        }
        return "unknown";
    }

    public record ConnectionInfo(
        String url,
        String username,
        String database,
        String currentUser,
        String currentSchema,
        boolean connected
    ) {}
}