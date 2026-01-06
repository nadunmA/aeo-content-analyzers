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

    // 1. Default No-args constructor (Lombok @NoArgsConstructor වෙනුවට)
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // 2. Custom constructor for basic errors
    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // 3. All-args constructor (Lombok @AllArgsConstructor වෙනුවට)
    public ErrorResponse(String errorCode, String message, LocalDateTime timestamp, Map<String, String> details) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
        this.details = details;
    }

    // 4. Constructor with details
    public ErrorResponse(String errorCode, String message, Map<String, String> details) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // 5. Getters and Setters (Lombok @Data වෙනුවට)

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
