package com.example.clinicapp.dto;

public class ApiResponse {
    private String message;
    private Object data; // This can hold any data you wish to return

    public ApiResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

    // Getters and setters
}
