package com.cts.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorHandler {
//	   @ExceptionHandler(MenuItemNotFoundException.class)
//	    public ResponseEntity<ErrorResponse> handleMenuItemNotFound(MenuItemNotFoundException ex) {
//	        ErrorResponse response = new ErrorResponse();
//	        response.setMessage(ex.getMessage());
//	        return new ResponseEntity<ErrorResponse>(response, HttpStatus.NOT_FOUND);
//	    }

//	    @ExceptionHandler(CategoryNotFoundException.class)
//	    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex) {
//	        ErrorResponse response = new ErrorResponse();
//	        response.setMessage(ex.getMessage());
//	        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//	    }

	 @ExceptionHandler(DuplicateException.class)
	    public ResponseEntity<ErrorResponse> handleDuplicateException(DuplicateException ex) {
	        ErrorResponse error = new ErrorResponse(ex.getMessage());
	        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	    }
	 @ExceptionHandler(ExternalServiceUnavailableException.class)
	 public ResponseEntity<ErrorResponse> handleExternalServiceDown(ExternalServiceUnavailableException ex) {
	        ErrorResponse error = new ErrorResponse(ex.getMessage());
	        return new ResponseEntity<>(error,HttpStatus.SERVICE_UNAVAILABLE);
	    }
	 

	    @ExceptionHandler(MethodArgumentNotValidException.class)
	    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
	        StringBuilder sb = new StringBuilder();
	        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
	            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(" ");
	        }
	        ErrorResponse response = new ErrorResponse(sb.toString());
	        return ResponseEntity.badRequest().body(response);
	    }

	    @ExceptionHandler(Exception.class)
	    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
	        ErrorResponse response = new ErrorResponse("Something went wrong: " + ex.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
}
