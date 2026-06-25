package com.suhani.aiquerygenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the AI SQL Query Generator application.
 * This Spring Boot application provides intelligent SQL query generation,
 * explanation, validation, and optimization using Hugging Face AI.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
@SpringBootApplication
public class AiquerygeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiquerygeneratorApplication.class, args);
    }
}