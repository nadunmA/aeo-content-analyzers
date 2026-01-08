package com.aeo.analyzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> details;


    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }


    public ErrorResponse(String errorCode, String message, LocalDateTime timestamp, Map<String, String> details) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
        this.details = details;
    }

    public ErrorResponse(String errorCode, String message, Map<String, String> details) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }


    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}
