package com.cts.exception;

public class ErrorResponse {
	
	 public ErrorResponse() {
		super();
	}

	 public ErrorResponse(String message) {
		super();
		this.message = message;
	}

	 public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	 private String message;

}
