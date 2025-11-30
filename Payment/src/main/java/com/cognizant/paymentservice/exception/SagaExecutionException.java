package com.cognizant.paymentservice.exception;

/**
 * Exception thrown when Saga pattern execution fails
 * Used to indicate compensating transaction has been executed
 */
public class SagaExecutionException extends RuntimeException {
    
    private String errorCode;
    private String errorMessage;
    private Throwable cause;

    // ==================== CONSTRUCTORS ====================

    /**
     * Constructor with message only
     * @param message Error message
     */
    public SagaExecutionException(String message) {
        super(message);
        this.errorMessage = message;
        this.errorCode = "SAGA_EXECUTION_ERROR";
    }

    /**
     * Constructor with message and cause
     * @param message Error message
     * @param cause Root cause exception
     */
    public SagaExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
        this.cause = cause;
        this.errorCode = "SAGA_EXECUTION_ERROR";
    }

    /**
     * Constructor with message, error code, and cause
     * @param message Error message
     * @param errorCode Specific error code for logging
     * @param cause Root cause exception
     */
    public SagaExecutionException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
        this.errorCode = errorCode;
        this.cause = cause;
    }

    // ==================== GETTERS ====================

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "SagaExecutionException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", cause=" + (cause != null ? cause.getMessage() : "null") +
                '}';
    }

    /**
     * Get detailed error information for logging
     */
    public String getDetailedError() {
        return String.format(
            "SagaExecutionException [Code: %s, Message: %s, Cause: %s]",
            errorCode,
            errorMessage,
            cause != null ? cause.getMessage() : "No cause"
        );
    }
}