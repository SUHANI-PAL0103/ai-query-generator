package com.suhani.aiquerygenerator.controller;

import com.suhani.aiquerygenerator.entity.QueryHistory;
import com.suhani.aiquerygenerator.repository.QueryHistoryRepository;
import com.suhani.aiquerygenerator.util.DynamicJdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for dashboard statistics and analytics.
 * Provides real-time data from the database.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final QueryHistoryRepository queryHistoryRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DynamicJdbcTemplate dynamicJdbcTemplate;

    public DashboardController(QueryHistoryRepository queryHistoryRepository,
                               JdbcTemplate jdbcTemplate,
                               DynamicJdbcTemplate dynamicJdbcTemplate) {
        this.queryHistoryRepository = queryHistoryRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dynamicJdbcTemplate = dynamicJdbcTemplate;
    }

    /**
     * Get dashboard statistics from real database.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new LinkedHashMap<>();

            // Total queries from history
            long totalQueries = queryHistoryRepository.count();
            stats.put("totalQueries", totalQueries);

            // Successful executions (all history entries are successful generations)
            long successfulExecutions = queryHistoryRepository.count();
            stats.put("successfulExecutions", successfulExecutions);

            // Failed queries (we track failed executions in a separate way)
            // For now, we'll calculate based on execution results
            long failedExecutions = 0; // Can be enhanced later
            stats.put("failedExecutions", failedExecutions);

            // Total with status breakdown
            stats.put("successRate", totalQueries > 0 
                ? Math.round((successfulExecutions * 100.0 / totalQueries)) 
                : 0);

            // Saved queries (entries that are bookmarked or have high usage)
            long savedQueries = 0; // Can be enhanced with a bookmark feature
            stats.put("savedQueries", savedQueries);

            // Average execution time (from database if tracked)
            List<QueryHistory> execTimeRecords = queryHistoryRepository.findByExecutionTimeMsIsNotNull();
            Double avgExecTime = null;
            if (!execTimeRecords.isEmpty()) {
                double sum = execTimeRecords.stream()
                    .mapToLong(QueryHistory::getExecutionTimeMs)
                    .average()
                    .orElse(0.0);
                avgExecTime = sum / 1000.0; // Convert ms to seconds
            }
            stats.put("avgExecutionTime", avgExecTime != null ? String.format("%.1fs", avgExecTime) : "N/A");

            // Recent activity count (last 24 hours)
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            long recentCount = queryHistoryRepository.countByCreatedAtAfter(yesterday);
            stats.put("recentActivityCount", recentCount);

            // Query type distribution from history
            List<QueryHistory> allTypes = queryHistoryRepository.findAllQueryTypes();
            Map<String, Long> typeDistribution = allTypes.stream()
                .collect(Collectors.groupingBy(
                    QueryHistory::getQueryType,
                    Collectors.counting()
                ));
            stats.put("queryTypeDistribution", typeDistribution);

            // Risk level distribution
            List<QueryHistory> allRiskLevels = queryHistoryRepository.findAllRiskLevels();
            Map<String, Long> riskDistribution = allRiskLevels.stream()
                .collect(Collectors.groupingBy(
                    QueryHistory::getRiskLevel,
                    Collectors.counting()
                ));
            stats.put("riskDistribution", riskDistribution);

            // Trend data (last 7 days)
            Map<String, Long> trendData = new LinkedHashMap<>();
            for (int i = 6; i >= 0; i--) {
                LocalDateTime dayStart = LocalDateTime.now().minusDays(i).toLocalDate().atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1);
                long count = queryHistoryRepository.countByCreatedAtBetween(dayStart, dayEnd);
                String label = dayStart.toLocalDate().toString().substring(5); // MM-DD
                trendData.put(label, count);
            }
            stats.put("trendData", trendData);

            logger.info("Dashboard stats fetched: {} total queries", totalQueries);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.warn("Dashboard history stats unavailable: {}", e.getMessage());
            return ResponseEntity.ok(emptyStats());
        }
    }

    /**
     * Get recent activity from real database.
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity() {
        try {
            List<QueryHistory> recentHistory = queryHistoryRepository.findTop10ByOrderByCreatedAtDesc();
            
            List<Map<String, Object>> activity = recentHistory.stream()
                .map(h -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("id", h.getId());
                    entry.put("prompt", h.getPrompt());
                    entry.put("query", h.getGeneratedSql());
                    entry.put("date", formatTimeAgo(h.getCreatedAt()));
                    entry.put("status", "success"); // All history entries are successful generations
                    entry.put("queryType", h.getQueryType());
                    entry.put("riskLevel", h.getRiskLevel());
                    return entry;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(activity);

        } catch (Exception e) {
            logger.warn("Recent activity unavailable: {}", e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /**
     * Test database connection health.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        try {
            JdbcTemplate connectedJdbcTemplate = dynamicJdbcTemplate.create();
            if (connectedJdbcTemplate == null) {
                throw new IllegalStateException("No database connection. Please connect your database first.");
            }
            // Simple query to test connection
            Integer result = connectedJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            Map<String, Object> health = new LinkedHashMap<>();
            health.put("status", "healthy");
            health.put("database", "connected");
            health.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(health);

        } catch (Exception e) {
            logger.error("Database health check failed: {}", e.getMessage());
            Map<String, Object> health = new LinkedHashMap<>();
            health.put("status", "unhealthy");
            health.put("database", "disconnected");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    /**
     * Get summary statistics for the connected database.
     */
    @GetMapping("/database-info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        try {
            JdbcTemplate connectedJdbcTemplate = dynamicJdbcTemplate.create();
            if (connectedJdbcTemplate == null) {
                throw new IllegalStateException("No database connection. Please connect your database first.");
            }
            Map<String, Object> info = new LinkedHashMap<>();

            // Get table count
            Long tableCount = connectedJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'",
                Long.class
            );
            info.put("tableCount", tableCount);

            // Get database size
            try {
                Long dbSize = connectedJdbcTemplate.queryForObject(
                    "SELECT pg_database_size(current_database())",
                    Long.class
                );
                info.put("databaseSizeBytes", dbSize);
                info.put("databaseSize", formatBytes(dbSize));
            } catch (Exception e) {
                info.put("databaseSize", "N/A");
            }

            // Get connection info
            try {
                String currentUser = connectedJdbcTemplate.queryForObject("SELECT current_user", String.class);
                String currentDatabase = connectedJdbcTemplate.queryForObject("SELECT current_database()", String.class);
                String currentSchema = connectedJdbcTemplate.queryForObject("SELECT current_schema()", String.class);
                info.put("currentUser", currentUser);
                info.put("currentDatabase", currentDatabase);
                info.put("currentSchema", currentSchema);
            } catch (Exception e) {
                info.put("connectionInfo", "unavailable");
            }

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            logger.error("Error fetching database info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch database info"));
        }
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "unknown";
        
        java.time.Duration duration = java.time.Duration.between(dateTime, LocalDateTime.now());
        long minutes = duration.toMinutes();
        
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " mins ago";
        
        long hours = minutes / 60;
        if (hours < 24) return hours + " hours ago";
        
        long days = hours / 24;
        return days + " days ago";
    }

    private String formatBytes(Long bytes) {
        if (bytes == null) return "N/A";
        
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private Map<String, Object> emptyStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalQueries", 0);
        stats.put("successfulExecutions", 0);
        stats.put("failedExecutions", 0);
        stats.put("successRate", 0);
        stats.put("savedQueries", 0);
        stats.put("avgExecutionTime", "N/A");
        stats.put("recentActivityCount", 0);
        stats.put("queryTypeDistribution", Collections.emptyMap());
        stats.put("riskDistribution", Collections.emptyMap());

        Map<String, Long> trendData = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = LocalDateTime.now().minusDays(i).toLocalDate().atStartOfDay();
            trendData.put(dayStart.toLocalDate().toString().substring(5), 0L);
        }
        stats.put("trendData", trendData);
        return stats;
    }
}
