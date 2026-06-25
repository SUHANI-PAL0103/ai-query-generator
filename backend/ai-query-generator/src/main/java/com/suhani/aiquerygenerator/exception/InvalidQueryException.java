package com.suhani.aiquerygenerator.exception;

/**
 * Custom exception thrown when an SQL query is found to be invalid
 * during validation checks, such as syntax errors or security concerns.
 *
 * @author Suhani Pal
 * @version 1.0.0
 */
public class InvalidQueryException extends RuntimeException {

    /**
     * Constructs a new InvalidQueryException with the specified detail message.
     *
     * @param message the detail message explaining the validation error
     */
    public InvalidQueryException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidQueryException with the specified detail message and cause.
     *
     * @param message the detail message explaining the validation error
     * @param cause   the underlying cause of the exception
     */
    public InvalidQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}