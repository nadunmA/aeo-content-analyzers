package com.aeo.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ErrorResponse {

    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> details;

    public ErrorResponse(String errorCode, String message) {

        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String errorCode, String message, Map<String, String> details) {

        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
