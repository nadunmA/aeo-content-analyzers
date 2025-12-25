package com.aeo.analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeRequest {

    @NotBlank(message = "Text cannot be empty")
    @Size(max = 2048, message = "URL is too long")
    private String text;

    @NotBlank(message = "Text cannot be empty")
    @Size(max = 2048, message = "URL is too long")
    private String type;

    @NotBlank(message = "Text cannot be empty")
    @Size(max = 2048, message = "URL is too long")
    private String url;
}