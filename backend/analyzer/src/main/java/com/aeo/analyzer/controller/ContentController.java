package com.aeo.analyzer.controller;

import lombok.extern.slf4j.Slf4j;
import com.aeo.analyzer.dto.AnalyzeRequest;
import com.aeo.analyzer.model.AuditReport;
import com.aeo.analyzer.repository.AuditReportRepository;
import com.aeo.analyzer.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/content")
@SuppressWarnings("unchecked")
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173"},//connect with frontend
        methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.DELETE},
        allowedHeaders = "*",
        maxAge = 3600
)
public class ContentController {

    private final GeminiService geminiService;
    private final AuditReportRepository repository;
    private final ObjectMapper objectMapper;


    //service connect controller (dependency injection)
    public ContentController(GeminiService geminiService, AuditReportRepository repository, ObjectMapper objectMapper) {
        this.geminiService = geminiService;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Object> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            HttpServletRequest httpServletRequest) {

        String clientIp = httpServletRequest.getRemoteAddr();
        log.info("Analysis request from IP: {}", clientIp);

        try {
            //call gemini service
            String geminiJson = geminiService.analyzeContent(request.getText());

            //json string
            Map<String, Object> result = objectMapper.readValue(
                    geminiJson,
                    new TypeReference<Map<String, Object>>() {}
            );

            //create report object
            AuditReport report = new AuditReport();
            String text = request.getText();

            String title = text.length() > 50 ? text.substring(0, 50) + "..." : text;
            report.setTitle(title);
            report.setUrlOrText(text);
            report.setType("text");
            report.setStatus("completed");
            report.setIpAddress(clientIp);

            //data add report
            if(result.containsKey("score")) {
                report.setScore((Map<String, Object>) result.get("score"));
            }
            if(result.containsKey("audits")) {
                report.setAudits((List<Map<String, Object>>) result.get("audits"));
            }
            if(result.containsKey("suggestions")) {
                report.setSuggestions((List<Map<String, Object>>) result.get("suggestions"));
            }

            //save db
            AuditReport savedReport = repository.save(report);
            log.info("Report saved with ID: {}", savedReport.getId());

            return ResponseEntity.ok(savedReport);
        }catch (Exception e) {
            log.error("Analysis failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Analysis failed", "message", e.getMessage()));
        }

    }


    //Get history
    @GetMapping("/history")
    public ResponseEntity<List<AuditReport>> getHistory() {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc());
    }

    //Get single report by id
    @GetMapping("/report/{id}")
    public ResponseEntity<AuditReport> getReport(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //delete report by id
    @DeleteMapping("/report/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(@PathVariable String id) {
        if(!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
    }

    //health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "timestamp", java.time.LocalDateTime.now().toString()));
    }
}
