package com.suhani.aiquerygenerator.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suhani.aiquerygenerator.dto.AiQueryResponse;
import com.suhani.aiquerygenerator.exception.AIServiceException;
import com.suhani.aiquerygenerator.service.DatabaseValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class HuggingFaceAIService {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceAIService.class);

    private static final String API_URL =
            "https://router.huggingface.co/v1/chat/completions";

    private static final String MODEL = "Qwen/Qwen2.5-7B-Instruct";

    private final DatabaseValidationService dbValidation;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public HuggingFaceAIService(RestTemplate restTemplate,
                                ObjectMapper objectMapper,
                                @Value("${huggingface.api.key}") String apiKey,
                                DatabaseValidationService dbValidation) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.dbValidation = dbValidation;
    }

    /**
     * Builds the system prompt with real database schema injected dynamically.
     */
    private String buildSystemPrompt() {
        List<String> schemaLines = new ArrayList<>();
        schemaLines.add("You are an expert SQL engineer.");
        schemaLines.add("Convert the user request into the appropriate SQL.");
        schemaLines.add("");
        schemaLines.add("Here is the CURRENT database schema (existing tables):");

        List<String> tables = dbValidation.getRealTables();
        if (tables.isEmpty()) {
            schemaLines.add("  The database currently has no tables — you may need to CREATE them.");
        } else {
            for (String table : tables) {
                List<String> cols = dbValidation.getRealColumns(table);
                String colStr = cols.isEmpty() ? "(no columns)" : cols.stream().collect(Collectors.joining(", "));
                schemaLines.add("  Table: " + table);
                schemaLines.add("    Columns: " + colStr);
            }
        }

        schemaLines.add("");
        schemaLines.add("RULES:");
        schemaLines.add("1. If the user wants to CREATE a new table, generate a CREATE TABLE statement with appropriate columns and data types.");
        schemaLines.add("2. If the user wants to INSERT/UPDATE/DELETE data, generate the appropriate DML statement.");
        schemaLines.add("3. If the user wants to query data, use SELECT on the existing tables listed above.");
        schemaLines.add("4. For SELECT queries, ONLY use tables and columns that exist in the schema above.");
        schemaLines.add("5. For CREATE TABLE statements, you may define new tables with any columns the user requests.");
        schemaLines.add("");
        schemaLines.add("Return ONLY valid JSON:");
        schemaLines.add("{");
        schemaLines.add("  \"sql\": \"...\",");
        schemaLines.add("  \"explanation\": \"...\",");
        schemaLines.add("  \"tables\": [],");
        schemaLines.add("  \"columns\": []");
        schemaLines.add("}");

        return String.join("\n", schemaLines);
    }

    public AiQueryResponse generateSql(String prompt) {

        logger.info("Prompt: {}", prompt);
        logger.info("API Key starts with: {}", apiKey.substring(0, Math.min(8, apiKey.length())));

        try {
            // ---------------------------
            // Build messages
            // ---------------------------
            List<Map<String, String>> messages = new ArrayList<>();

            messages.add(Map.of(
                    "role", "system",
                    "content", buildSystemPrompt()
            ));

            messages.add(Map.of(
                    "role", "user",
                    "content", prompt
            ));

            // ---------------------------
            // Request body
            // ---------------------------
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("messages", messages);

            // ✅ FIXED VARIABLE NAME
            String requestJson = objectMapper.writeValueAsString(requestBody);

            logger.info("Final Request JSON: {}", requestJson);

            // ---------------------------
            // Headers
            // ---------------------------
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("X-HF-Model", MODEL);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            // ---------------------------
            // API CALL
            // ---------------------------
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String body = response.getBody();
            logger.info("Raw Response: {}", body);

            return parseResponse(body);

        } catch (Exception e) {
            logger.error("AI Error: ", e);
            throw new AIServiceException("Failed to generate SQL: " + e.getMessage());
        }
    }

    // ---------------------------
    // RESPONSE PARSER
    // ---------------------------
    private AiQueryResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String text = "";

            // Handle chat completions API format: choices[0].message.content
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                JsonNode message = root.get("choices").get(0).path("message");
                text = message.path("content").asText("");
            }
            // Handle text-generation API format (array with generated_text)
            else if (root.isArray() && root.size() > 0) {
                text = root.get(0).path("generated_text").asText();
            }
            // Handle direct object with generated_text
            else if (root.has("generated_text")) {
                text = root.get("generated_text").asText();
            }
            // Fallback: use full response as text
            else {
                text = root.toString();
            }

            logger.info("AI RAW OUTPUT TEXT: {}", text);

            return extractJson(text);

        } catch (Exception e) {
            throw new AIServiceException("Parse error: " + e.getMessage());
        }
    }

    // ---------------------------
    // EXTRACT JSON FROM TEXT
    // ---------------------------
    private AiQueryResponse extractJson(String text) throws JsonProcessingException {

        if (!text.contains("{") || !text.contains("}")) {
            throw new RuntimeException("AI did not return valid JSON: " + text);
        }

        Pattern pattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(text);

        String json;

        if (matcher.find()) {
            json = matcher.group(1);
        } else {
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');
            json = (start != -1 && end != -1) ? text.substring(start, end + 1) : text;
        }

        return objectMapper.readValue(json, AiQueryResponse.class);
    }
}