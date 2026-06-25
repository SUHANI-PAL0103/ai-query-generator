package com.suhani.aiquerygenerator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Home controller providing the root health-check endpoint.
 * Returns application status and lists all available API endpoints.
 * This prevents "No static resource" errors when accessing the root URL.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@RestController
public class HomeController {

    /**
     * Root health-check endpoint.
     * Accessible at http://localhost:8080/
     *
     * @return ResponseEntity containing application status and API documentation
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "running");
        response.put("application", "AI SQL Query Generator");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
            "POST /api/query/generate", "Generate SQL from natural language prompt",
            "POST /api/query/explain", "Explain an SQL query in plain English",
            "POST /api/query/validate", "Validate SQL query syntax & safety",
            "POST /api/query/optimize", "Suggest SQL query optimizations",
            "GET /api/query/history", "Retrieve query generation history"
        ));
        return ResponseEntity.ok(response);
    }
}