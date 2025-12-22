package com.aeo.analyzer.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data //auto create getters & setters
@Document(collection = "audit_reports")
public class AuditReport {

    @Id
    private String id;
    private String title;
    private String urlOrText; //analyze content
    private String type; //url or text
    private Map<String, Object> score;
    private List<Map<String, Object>> audits;
    private List<Map<String, Object>> suggestions;
    private LocalDateTime createdAt;

    //constructor
    public AuditReport() {

        this.createdAt = LocalDateTime.now();

    }

}
