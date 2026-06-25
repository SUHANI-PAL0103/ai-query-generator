package com.suhani.aiquerygenerator.exception;

/**
 * Custom exception thrown when the Hugging Face AI service encounters an error
 * during SQL query generation or response parsing.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
public class AIServiceException extends RuntimeException {

    /**
     * Constructs a new AIServiceException with the specified detail message.
     *
     * @param message the detail message explaining the error
     */
    public AIServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new AIServiceException with the specified detail message and cause.
     *
     * @param message the detail message explaining the error
     * @param cause   the underlying cause of the exception
     */
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}