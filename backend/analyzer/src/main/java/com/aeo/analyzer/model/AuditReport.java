package com.aeo.analyzer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private Map<String, Object> comparison;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private String userId;

    private String ipAddress;
    private String status;


    private String modelUsed;

    public AuditReport() {
        this.createdAt = LocalDateTime.now();
        this.status = "pending";
    }

    public AuditReport(String title, String urlOrText, String type, String userId) {
        this();
        this.title = title;
        this.urlOrText = urlOrText;
        this.type = type;
        this.userId = userId;
    }

    // Getters & Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrlOrText() { return urlOrText; }
    public void setUrlOrText(String urlOrText) { this.urlOrText = urlOrText; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getScore() { return score; }
    public void setScore(Map<String, Object> score) { this.score = score; }

    public List<Map<String, Object>> getAudits() { return audits; }
    public void setAudits(List<Map<String, Object>> audits) { this.audits = audits; }

    public List<Map<String, Object>> getSuggestions() { return suggestions; }
    public void setSuggestions(List<Map<String, Object>> suggestions) { this.suggestions = suggestions; }

    public Map<String, Object> getComparison() { return comparison; }
    public void setComparison(Map<String, Object> comparison) { this.comparison = comparison; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
}
