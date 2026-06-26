package com.suhani.aiquerygenerator.controller;

import com.suhani.aiquerygenerator.dto.ExplainQueryRequest;
import com.suhani.aiquerygenerator.dto.ExplainQueryResponse;
import com.suhani.aiquerygenerator.dto.GenerateQueryRequest;
import com.suhani.aiquerygenerator.dto.GenerateQueryResponse;
import com.suhani.aiquerygenerator.dto.OptimizeQueryRequest;
import com.suhani.aiquerygenerator.dto.OptimizeQueryResponse;
import com.suhani.aiquerygenerator.dto.QueryHistoryResponse;
import com.suhani.aiquerygenerator.dto.ValidateQueryRequest;
import com.suhani.aiquerygenerator.dto.ValidateQueryResponse;
import com.suhani.aiquerygenerator.service.QueryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerateQueryResponse> generateQuery(
            @Valid @RequestBody GenerateQueryRequest request,
            @RequestHeader(value = "X-Clerk-User-Id", required = false) String clerkId) {
        logger.info("Received generate query request from user: {}", clerkId);
        GenerateQueryResponse response = queryService.generateQuery(request.getPrompt());
        if (response.isSuccess() && clerkId != null && !clerkId.isEmpty()) {
            try {
                queryService.saveHistoryForUser(
                    request.getPrompt(),
                    response.getGeneratedSql(),
                    response.getExplanation(),
                    response.getQueryType(),
                    response.getRiskLevel(),
                    clerkId
                );
            } catch (Exception e) {
                logger.warn("Could not save user query history: {}", e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/explain")
    public ResponseEntity<ExplainQueryResponse> explainQuery(
            @Valid @RequestBody ExplainQueryRequest request) {
        logger.info("Received explain query request: {}", request.getSql());
        ExplainQueryResponse response = queryService.explainQuery(request.getSql());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateQueryResponse> validateQuery(
            @Valid @RequestBody ValidateQueryRequest request) {
        logger.info("Received validate query request: {}", request.getSql());
        ValidateQueryResponse response = queryService.validateQuery(request.getSql());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/optimize")
    public ResponseEntity<OptimizeQueryResponse> optimizeQuery(
            @Valid @RequestBody OptimizeQueryRequest request) {
        logger.info("Received optimize query request: {}", request.getSql());
        OptimizeQueryResponse response = queryService.optimizeQuery(request.getSql());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<QueryHistoryResponse>> getQueryHistory(
            @RequestHeader(value = "X-Clerk-User-Id", required = false) String clerkId) {
        logger.info("Received query history request for user: {}", clerkId);
        try {
            List<QueryHistoryResponse> history;
            if (clerkId != null && !clerkId.isEmpty()) {
                history = queryService.getQueryHistoryForUser(clerkId);
            } else {
                history = queryService.getQueryHistory();
            }
            return ResponseEntity.status(HttpStatus.OK).body(history);
        } catch (Exception e) {
            logger.warn("Could not load query history: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList());
        }
    }
}
