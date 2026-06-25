package com.suhani.aiquerygenerator.service;

import com.suhani.aiquerygenerator.dto.DbSchemaColumn;
import com.suhani.aiquerygenerator.dto.DbSchemaForeignKey;
import com.suhani.aiquerygenerator.dto.DbSchemaTable;
import com.suhani.aiquerygenerator.util.DynamicJdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatabaseSchemaService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaService.class);

    private final DynamicJdbcTemplate dynamicJdbcTemplate;

    public DatabaseSchemaService(DynamicJdbcTemplate dynamicJdbcTemplate) {
        this.dynamicJdbcTemplate = dynamicJdbcTemplate;
    }

    public List<DbSchemaTable> getSchema() {
        JdbcTemplate jt = dynamicJdbcTemplate.create();
        if (jt == null) {
            throw new RuntimeException("No database connection available. Please connect your database first.");
        }

        try {
            List<String> tables = jt.query(
                    "SELECT table_name " +
                            "FROM information_schema.tables " +
                            "WHERE table_schema = 'public' " +
                            "  AND table_type = 'BASE TABLE' " +
                            "ORDER BY table_name",
                    (rs, rowNum) -> rs.getString("table_name")
            );

            logger.info("Found tables: {}", tables);

            Map<String, List<DbSchemaColumn>> columnsByTable = new HashMap<>();
            for (String table : tables) {
                List<DbSchemaColumn> cols = jt.query(
                        "SELECT c.column_name, c.data_type, " +
                                "       (" +
                                "         SELECT COUNT(*) " +
                                "         FROM information_schema.table_constraints tc " +
                                "         JOIN information_schema.constraint_column_usage ccu " +
                                "           ON tc.constraint_name = ccu.constraint_name " +
                                "          AND tc.table_schema = ccu.table_schema " +
                                "         WHERE tc.constraint_type = 'PRIMARY KEY' " +
                                "           AND tc.table_name = ? " +
                                "           AND ccu.column_name = c.column_name " +
                                "       ) > 0 AS is_pk " +
                                "FROM information_schema.columns c " +
                                "WHERE c.table_schema = 'public' AND c.table_name = ? " +
                                "ORDER BY c.ordinal_position",
                        new Object[]{table, table},
                        (rs, rowNum) -> DbSchemaColumn.builder()
                                .name(rs.getString("column_name"))
                                .type(rs.getString("data_type"))
                                .primaryKey(rs.getBoolean("is_pk"))
                                .build()
                );
                columnsByTable.put(table, cols);
            }

            Map<String, List<DbSchemaForeignKey>> fksByTable = fetchForeignKeys(jt);

            List<DbSchemaTable> result = tables.stream()
                    .map(t -> DbSchemaTable.builder()
                            .name(t)
                            .columns(columnsByTable.getOrDefault(t, List.of()))
                            .foreignKeys(fksByTable.getOrDefault(t, List.of()))
                            .build())
                    .collect(Collectors.toList());

            logger.info("Schema result: {} tables", result.size());
            return result;

        } catch (Exception e) {
            logger.error("Failed to fetch database schema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch database schema: " + e.getMessage(), e);
        }
    }

    private Map<String, List<DbSchemaForeignKey>> fetchForeignKeys(JdbcTemplate jt) {
        try {
            List<Map<String, Object>> fkRows = jt.queryForList(
                    "SELECT " +
                            "  kcu.table_name AS table_name, " +
                            "  kcu.column_name AS column_name, " +
                            "  ccu.table_name AS foreign_table_name, " +
                            "  ccu.column_name AS foreign_column_name " +
                            "FROM information_schema.table_constraints tc " +
                            "JOIN information_schema.key_column_usage kcu " +
                            "  ON tc.constraint_name = kcu.constraint_name " +
                            " AND tc.table_schema = kcu.table_schema " +
                            "JOIN information_schema.constraint_column_usage ccu " +
                            "  ON ccu.constraint_name = tc.constraint_name " +
                            " AND ccu.table_schema = tc.table_schema " +
                            "WHERE tc.constraint_type = 'FOREIGN KEY' " +
                            "  AND tc.table_schema = 'public' " +
                            "ORDER BY kcu.table_name, kcu.column_name"
            );

            return fkRows.stream().collect(
                    Collectors.groupingBy(
                            r -> (String) r.get("table_name"),
                            Collectors.mapping(r -> DbSchemaForeignKey.builder()
                                            .column((String) r.get("column_name"))
                                            .references(
                                                    r.get("foreign_table_name") + "(" + r.get("foreign_column_name") + ")"
                                            )
                                            .build(),
                                    Collectors.toList())
                    )
            );
        } catch (Exception e) {
            logger.warn("Could not fetch foreign keys: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}