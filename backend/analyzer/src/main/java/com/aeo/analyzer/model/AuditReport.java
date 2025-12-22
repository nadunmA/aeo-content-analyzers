package com.aeo.analyzer.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "audit_reports")
public class AuditReport {

    @Id
    private String id;

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    @NotBlank(message = "Content cannot be empty")
    @Size(max = 100000, message = "Content too large (max 100KB)")
    private String urlOrText;

    @NotBlank(message = "Type cannot be empty")
    @Pattern(regexp = "^(url|text)$", message = "Type must be 'url' or 'text'")
    private String type;

    private Map<String, Object> score;
    private List<Map<String, Object>> audits;
    private List<Map<String, Object>> suggestions;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String userId;

    private String ipAddress;

    private String status;

    //No-args constructor with initialization
    public AuditReport() {
        this.createdAt = LocalDateTime.now();
        this.status = "pending";
    }

    public AuditReport(String title, String urlOrText, String type, String userId) {
        this(); // Call no-args constructor
        this.title = title;
        this.urlOrText = urlOrText;
        this.type = type;
        this.userId = userId;
    }
}