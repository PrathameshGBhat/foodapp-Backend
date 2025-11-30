package com.cognizant.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle SagaExecutionException
     * Returns appropriate HTTP status based on error code
     */
    @ExceptionHandler(SagaExecutionException.class)
    public ResponseEntity<Map<String, Object>> handleSagaExecutionException(
            SagaExecutionException ex, 
            WebRequest request) {
        
        logger.error("🔴 SagaExecutionException caught: {}", ex.getMessage());
        logger.error("   Error Code: {}", ex.getErrorCode());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", getHttpStatus(ex.getErrorCode()).value());
        response.put("error", ex.getErrorCode());
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        HttpStatus status = getHttpStatus(ex.getErrorCode());
        logger.error("   Returning HTTP Status: {}", status.value());
        
        return new ResponseEntity<>(response, status);
    }

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            WebRequest request) {
        
        logger.error("🔴 ResourceNotFoundException: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "NOT_FOUND");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, 
            WebRequest request) {
        
        logger.error("🔴 IllegalArgumentException: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "BAD_REQUEST");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, 
            WebRequest request) {
        
        logger.error("🔴 Unexpected Exception: {}", ex.getClass().getSimpleName());
        logger.error("   Message: {}", ex.getMessage());
        ex.printStackTrace();
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "INTERNAL_SERVER_ERROR");
        response.put("message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
        response.put("path", request.getDescription(false).replace("uri=", ""));
        response.put("exceptionType", ex.getClass().getSimpleName());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Map error codes to HTTP status codes
     */
    private HttpStatus getHttpStatus(String errorCode) {
        switch (errorCode) {
            case "SERVICE_UNAVAILABLE":
                return HttpStatus.SERVICE_UNAVAILABLE;  // 503
            case "ORDER_NOT_FOUND":
                return HttpStatus.NOT_FOUND;  // 404
            case "ORDER_SERVICE_CALL":
                return HttpStatus.BAD_REQUEST;  // 400
            case "FEIGN_ERROR":
                return HttpStatus.SERVICE_UNAVAILABLE;  // 503
            case "COMPENSATING_TX_FAILED":
                return HttpStatus.INTERNAL_SERVER_ERROR;  // 500
            case "UNKNOWN_ERROR":
                return HttpStatus.INTERNAL_SERVER_ERROR;  // 500
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;  // 500
        }
    }
}