package com.suhani.aiquerygenerator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for analyzing SQL queries.
 * Provides methods to determine query type, extract tables and columns,
 * estimate impact, and assess risk levels based on the query structure.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@Component
public class QueryAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(QueryAnalyzer.class);

    // Risk level constants
    public static final String RISK_LOW = "LOW";
    public static final String RISK_MEDIUM = "MEDIUM";
    public static final String RISK_HIGH = "HIGH";
    public static final String RISK_CRITICAL = "CRITICAL";

    // Query type constants
    public static final String TYPE_SELECT = "SELECT";
    public static final String TYPE_INSERT = "INSERT";
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_DELETE = "DELETE";
    public static final String TYPE_UNKNOWN = "UNKNOWN";

    /**
     * Determines the type of SQL query (SELECT, INSERT, UPDATE, DELETE).
     *
     * @param sql the SQL query string
     * @return the query type as a string
     */
    public String determineQueryType(String sql) {
        if (sql == null || sql.isBlank()) {
            return TYPE_UNKNOWN;
        }

        String trimmedSql = sql.trim().toUpperCase();

        if (trimmedSql.startsWith("SELECT")) {
            return TYPE_SELECT;
        } else if (trimmedSql.startsWith("INSERT")) {
            return TYPE_INSERT;
        } else if (trimmedSql.startsWith("UPDATE")) {
            return TYPE_UPDATE;
        } else if (trimmedSql.startsWith("DELETE")) {
            return TYPE_DELETE;
        }

        return TYPE_UNKNOWN;
    }

    /**
     * Estimates the number of rows that would be affected/returned by the query.
     * Uses a heuristic approach based on query type and presence of WHERE clause.
     *
     * @param sql the SQL query string
     * @return estimated number of rows
     */
    public long estimateRows(String sql) {
        if (sql == null || sql.isBlank()) {
            return 0;
        }

        String upperSql = sql.trim().toUpperCase();
        String queryType = determineQueryType(sql);

        switch (queryType) {
            case TYPE_SELECT:
                return estimateSelectRows(upperSql);
            case TYPE_UPDATE:
                return estimateUpdateRows(upperSql);
            case TYPE_DELETE:
                return estimateDeleteRows(upperSql);
            case TYPE_INSERT:
                return estimateInsertRows(upperSql);
            default:
                return 10; // Default estimate
        }
    }

    /**
     * Analyzes the risk level of executing the given SQL query.
     * Performs safety checks and assigns appropriate risk levels.
     *
     * @param sql the SQL query string
     * @return risk level as a string (LOW, MEDIUM, HIGH, CRITICAL)
     */
    public String assessRiskLevel(String sql) {
        if (sql == null || sql.isBlank()) {
            return RISK_LOW;
        }

        String upperSql = sql.trim().toUpperCase();
        String queryType = determineQueryType(sql);

        switch (queryType) {
            case TYPE_DELETE:
                return assessDeleteRisk(upperSql);
            case TYPE_UPDATE:
                return assessUpdateRisk(upperSql);
            case TYPE_INSERT:
                return RISK_MEDIUM;
            case TYPE_SELECT:
                return assessSelectRisk(upperSql);
            default:
                return RISK_LOW;
        }
    }

    /**
     * Extracts table names mentioned in the SQL query.
     *
     * @param sql the SQL query string
     * @return a set of table names
     */
    public Set<String> extractTables(String sql) {
        Set<String> tables = new HashSet<>();
        if (sql == null || sql.isBlank()) {
            return tables;
        }

        String upperSql = sql.trim().toUpperCase();
        String queryType = determineQueryType(sql);

        // Extract table after FROM clause
        if (upperSql.contains("FROM")) {
            int fromIndex = upperSql.indexOf("FROM") + 4;
            String afterFrom = upperSql.substring(fromIndex).trim();
            // Handle JOINs and subqueries
            // Take the first word as table name (before WHERE, JOIN, GROUP BY, ORDER BY, LIMIT)
            int endIndex = findNextKeyword(afterFrom);
            String tableName = endIndex > 0 ? afterFrom.substring(0, endIndex).trim() : afterFrom.trim();
            // Remove any aliases
            String[] parts = tableName.split("\\s+");
            if (parts.length > 0) {
                tables.add(parts[0].replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        // Extract table from UPDATE
        if (TYPE_UPDATE.equals(queryType)) {
            String afterUpdate = upperSql.substring(6).trim();
            int spaceIndex = afterUpdate.indexOf(' ');
            if (spaceIndex > 0) {
                tables.add(afterUpdate.substring(0, spaceIndex).replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        // Extract table from INSERT INTO
        if (upperSql.contains("INTO")) {
            int intoIndex = upperSql.indexOf("INTO") + 4;
            String afterInto = upperSql.substring(intoIndex).trim();
            String[] parts = afterInto.split("\\s+");
            if (parts.length > 0) {
                tables.add(parts[0].replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        // Extract tables from DELETE FROM
        if (upperSql.contains("DELETE")) {
            int fromIdx = upperSql.indexOf("FROM");
            if (fromIdx != -1) {
                String afterFrom = upperSql.substring(fromIdx + 4).trim();
                int endIdx = findNextKeyword(afterFrom);
                String tableName = endIdx > 0 ? afterFrom.substring(0, endIdx).trim() : afterFrom.trim();
                String[] parts = tableName.split("\\s+");
                if (parts.length > 0) {
                    tables.add(parts[0].replaceAll("[^a-zA-Z0-9_]", ""));
                }
            }
        }

        logger.debug("Extracted tables from query: {}", tables);
        return tables;
    }

    /**
     * Extracts column names mentioned in the SQL query.
     *
     * @param sql the SQL query string
     * @return a set of column names
     */
    public Set<String> extractColumns(String sql) {
        Set<String> columns = new HashSet<>();
        if (sql == null || sql.isBlank()) {
            return columns;
        }

        String upperSql = sql.trim().toUpperCase();
        String queryType = determineQueryType(sql);

        switch (queryType) {
            case TYPE_SELECT:
                extractSelectColumns(sql, columns);
                break;
            case TYPE_UPDATE:
                // Extract columns from SET clause
                extractUpdateColumns(sql, columns);
                break;
            case TYPE_INSERT:
                // Extract columns from INSERT INTO ... (...)
                extractInsertColumns(sql, columns);
                break;
            case TYPE_DELETE:
                // Extract columns from WHERE clause
                extractWhereColumns(sql, columns);
                break;
        }

        logger.debug("Extracted columns from query: {}", columns);
        return columns;
    }

    /**
     * Checks if the SQL query has a WHERE clause.
     *
     * @param sql the SQL query string
     * @return true if WHERE clause is present
     */
    public boolean hasWhereClause(String sql) {
        if (sql == null || sql.isBlank()) {
            return false;
        }
        return sql.trim().toUpperCase().contains("WHERE");
    }

    /**
     * Detects if the query uses SELECT *.
     *
     * @param sql the SQL query string
     * @return true if SELECT * is used
     */
    public boolean hasSelectStar(String sql) {
        if (sql == null || sql.isBlank()) {
            return false;
        }
        String upperSql = sql.trim().toUpperCase();
        return upperSql.contains("SELECT *") || upperSql.contains("SELECT  *");
    }

    /**
     * Detects if the query has ORDER BY without any index hints.
     *
     * @param sql the SQL query string
     * @return true if ORDER BY is present
     */
    public boolean hasOrderBy(String sql) {
        if (sql == null || sql.isBlank()) {
            return false;
        }
        return sql.trim().toUpperCase().contains("ORDER BY");
    }

    /**
     * Detects unnecessary joins in a SELECT query.
     *
     * @param sql the SQL query string
     * @return true if multiple JOINs are detected
     */
    public boolean hasUnnecessaryJoins(String sql) {
        if (sql == null || sql.isBlank()) {
            return false;
        }
        String upperSql = sql.trim().toUpperCase();
        int joinCount = 0;
        int index = 0;
        while ((index = upperSql.indexOf("JOIN", index)) != -1) {
            joinCount++;
            index += 4;
        }
        return joinCount > 2; // More than 2 joins might be unnecessary
    }

    // ==================== Private Helper Methods ====================

    private long estimateSelectRows(String upperSql) {
        if (hasWhereClause(upperSql)) {
            // Queries with WHERE clause typically return fewer rows
            if (upperSql.contains("=") || upperSql.contains("LIKE") || upperSql.contains("IN")) {
                return 25;
            }
            return 50;
        }
        // SELECT without WHERE returns all rows
        return 1000;
    }

    private long estimateUpdateRows(String upperSql) {
        if (!hasWhereClause(upperSql)) {
            return 1000; // Updates all rows
        }
        return 25; // Targeted update
    }

    private long estimateDeleteRows(String upperSql) {
        if (!hasWhereClause(upperSql)) {
            return 1000; // Deletes all rows
        }
        return 10; // Targeted delete
    }

    private long estimateInsertRows(String upperSql) {
        // Count VALUES clauses for multi-row inserts
        int count = 1;
        int index = 0;
        while ((index = upperSql.indexOf("VALUES", index)) != -1) {
            count++;
            index += 6;
        }
        return count;
    }

    private String assessDeleteRisk(String upperSql) {
        if (!hasWhereClause(upperSql)) {
            return RISK_CRITICAL;
        }
        return RISK_HIGH;
    }

    private String assessUpdateRisk(String upperSql) {
        if (!hasWhereClause(upperSql)) {
            return RISK_CRITICAL;
        }
        return RISK_MEDIUM;
    }

    private String assessSelectRisk(String upperSql) {
        if (upperSql.contains("DROP") || upperSql.contains("TRUNCATE")
                || upperSql.contains("ALTER") || upperSql.contains("EXEC")) {
            return RISK_HIGH;
        }
        return RISK_LOW;
    }

    private int findNextKeyword(String str) {
        String[] keywords = {"WHERE", "JOIN", "INNER", "LEFT", "RIGHT", "OUTER",
                "CROSS", "ON", "GROUP", "ORDER", "HAVING", "LIMIT",
                "OFFSET", "UNION", "INTERSECT", "EXCEPT"};
        int minIndex = Integer.MAX_VALUE;
        for (String keyword : keywords) {
            int idx = str.indexOf(" " + keyword + " ");
            if (idx == -1) {
                idx = str.indexOf(" " + keyword);
            }
            if (idx == -1) {
                idx = str.indexOf(keyword + " ");
            }
            if (idx != -1 && idx < minIndex) {
                minIndex = idx;
            }
        }
        // Also check for parentheses
        int parenIndex = str.indexOf('(');
        if (parenIndex != -1 && parenIndex < minIndex) {
            minIndex = parenIndex;
        }
        return minIndex == Integer.MAX_VALUE ? -1 : minIndex;
    }

    private void extractSelectColumns(String sql, Set<String> columns) {
        String upperSql = sql.trim().toUpperCase();
        int selectIndex = 6; // "SELECT".length()
        int fromIndex = upperSql.indexOf("FROM");

        if (fromIndex > selectIndex) {
            String selectClause = sql.substring(selectIndex, fromIndex).trim();
            if (!selectClause.equals("*")) {
                String[] parts = selectClause.split(",");
                for (String part : parts) {
                    String cleaned = part.trim().replaceAll("\\s+.*", "").replaceAll("[^a-zA-Z0-9_.]", "").trim();
                    if (!cleaned.isEmpty()) {
                        columns.add(cleaned);
                    }
                }
            }
        }
    }

    private void extractUpdateColumns(String sql, Set<String> columns) {
        String upperSql = sql.trim().toUpperCase();
        int setIndex = upperSql.indexOf("SET");
        int whereIndex = upperSql.indexOf("WHERE");

        if (setIndex != -1) {
            int endIndex = whereIndex != -1 ? whereIndex : upperSql.length();
            String setClause = sql.substring(setIndex + 3, endIndex).trim();
            String[] assignments = setClause.split(",");
            for (String assignment : assignments) {
                String[] parts = assignment.split("=");
                if (parts.length > 0) {
                    columns.add(parts[0].trim().replaceAll("[^a-zA-Z0-9_.]", ""));
                }
            }
        }
    }

    private void extractInsertColumns(String sql, Set<String> columns) {
        String upperSql = sql.trim().toUpperCase();
        int intoIndex = upperSql.indexOf("INTO");
        int valuesIndex = upperSql.indexOf("VALUES");
        int openParen = -1;

        if (intoIndex != -1 && valuesIndex != -1) {
            String between = sql.substring(intoIndex + 4, valuesIndex);
            openParen = between.indexOf('(');
            if (openParen != -1) {
                int closeParen = between.indexOf(')');
                if (closeParen > openParen) {
                    String colList = between.substring(openParen + 1, closeParen);
                    for (String col : colList.split(",")) {
                        columns.add(col.trim().replaceAll("[^a-zA-Z0-9_.]", ""));
                    }
                }
            }
        }
    }

    private void extractWhereColumns(String sql, Set<String> columns) {
        String upperSql = sql.trim().toUpperCase();
        int whereIndex = upperSql.indexOf("WHERE");
        if (whereIndex != -1) {
            String whereClause = sql.substring(whereIndex + 5);
            // Extract column names from conditions
            String[] parts = whereClause.split("AND|OR");
            for (String condition : parts) {
                String[] operands = condition.split("=|!=|<>|>|<|>=|<=|LIKE|IN|BETWEEN|IS");
                if (operands.length > 0) {
                    String col = operands[0].trim().replaceAll("[^a-zA-Z0-9_.]", "").trim();
                    if (!col.isEmpty() && !col.matches("'.*'")) {
                        columns.add(col);
                    }
                }
            }
        }
    }
}