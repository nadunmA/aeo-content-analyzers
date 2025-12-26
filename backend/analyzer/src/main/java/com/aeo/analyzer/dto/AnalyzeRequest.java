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

    @NotBlank(message = "Content cannot be empty")
    @Size(max = 50000, message = "Content is too long (Max 50,000 chars)")
    private String text;

    @NotBlank(message = "Type is required")
    private String type; // 'url' or 'text'

}