package com.suhani.aiquerygenerator.controller;

import com.suhani.aiquerygenerator.dto.DbSchemaTable;
import com.suhani.aiquerygenerator.service.DatabaseSchemaService;
import com.suhani.aiquerygenerator.service.DatabaseConnectionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/db")
public class DbSchemaController {

    private final DatabaseSchemaService databaseSchemaService;
    private final DatabaseConnectionManager connectionManager;

    public DbSchemaController(DatabaseSchemaService databaseSchemaService,
                              DatabaseConnectionManager connectionManager) {
        this.databaseSchemaService = databaseSchemaService;
        this.connectionManager = connectionManager;
    }

    @GetMapping("/schema")
    public ResponseEntity<?> getSchema() {
        try {
            List<DbSchemaTable> schema = databaseSchemaService.getSchema();
            return ResponseEntity.ok(schema);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch schema: " + e.getMessage()));
        }
    }

    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody Map<String, String> request) {
        try {
            String url = request.get("url");
            String username = request.get("username");
            String password = request.get("password");

            if (url == null || username == null || password == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Missing connection parameters"));
            }

            boolean success = connectionManager.testConnection(url, username, password);
            
            return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Connection successful!" : "Connection failed. Check your credentials.",
                "database", success ? connectionManager.extractDatabaseName(url) : "unknown"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Connection test error: " + e.getMessage()));
        }
    }
}

