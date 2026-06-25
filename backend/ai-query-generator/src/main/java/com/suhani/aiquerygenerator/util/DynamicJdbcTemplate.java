package com.suhani.aiquerygenerator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Provides a JdbcTemplate dynamically created from the user's DatabaseContext credentials.
 * This ensures each user's API calls hit their own database, not the fallback H2.
 */
@Component
public class DynamicJdbcTemplate {

    private static final Logger logger = LoggerFactory.getLogger(DynamicJdbcTemplate.class);

    /**
     * Creates a JdbcTemplate connected to the user's database from DatabaseContext.
     * Returns null if no connection context is available.
     */
    public JdbcTemplate create() {
        DatabaseContext.ConnectionRequest ctx = DatabaseContext.getCurrent();
        if (ctx == null || !ctx.isValid()) {
            logger.warn("No database context available for this request");
            return null;
        }
        try {
            Connection conn = DriverManager.getConnection(ctx.getUrl(), ctx.getUsername(), ctx.getPassword());
            SingleConnectionDataSource ds = new SingleConnectionDataSource(conn, true);
            return new JdbcTemplate(ds);
        } catch (Exception e) {
            logger.error("Failed to create dynamic connection: {}", e.getMessage());
            return null;
        }
    }
}