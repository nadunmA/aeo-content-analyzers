package com.aeo.analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnalyzeRequest {

    @NotBlank(message = "Content cannot be empty")
    @Size(max = 50000, message = "Content is too long (Max 50,000 chars)")
    private String text;

    @NotBlank(message = "Type is required")
    private String type; // 'url' or 'text'


    public AnalyzeRequest() {
    }

    public AnalyzeRequest(String text, String type) {
        this.text = text;
        this.type = type;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
