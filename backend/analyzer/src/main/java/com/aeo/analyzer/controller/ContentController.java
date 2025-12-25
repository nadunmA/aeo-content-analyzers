package com.aeo.analyzer.controller;

import com.aeo.analyzer.service.ModelRouterService;
import com.aeo.analyzer.service.WebScraperService;
import lombok.extern.slf4j.Slf4j;
import com.aeo.analyzer.dto.AnalyzeRequest;
import com.aeo.analyzer.model.AuditReport;
import com.aeo.analyzer.repository.AuditReportRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/content")
@SuppressWarnings("unchecked")
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:2000"},//connect with frontend
        methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.DELETE},
        allowedHeaders = {"Content-Type", "Authorization", "X-Requested-With"},
        maxAge = 3600
)
public class ContentController {

    private final ModelRouterService modelRouterService;
    private final AuditReportRepository repository;
    private final ObjectMapper objectMapper;
    private final WebScraperService webScraperService;


    public ContentController(AuditReportRepository repository, ObjectMapper objectMapper, WebScraperService webScraperService, ModelRouterService modelRouterService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.webScraperService = webScraperService;
        this.modelRouterService = modelRouterService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Object> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            HttpServletRequest httpServletRequest) {
        String clientIp = httpServletRequest.getRemoteAddr();
        log.info("Analysis request from IP: {}", clientIp);
        try {
            String contentToAnalyze = request.getText();
            String requestType = request.getType() != null ? request.getType() : "text";
            String extractedTitle = null;

            if ("url".equalsIgnoreCase(requestType)) {
                log.info("Scraping URL: {}", request.getText());
                try {
                    contentToAnalyze = webScraperService.scrapeUrl(request.getText());
                    extractedTitle = webScraperService.extractTitleFromUrl(request.getText());
                    log.info("Extracted title: {}", extractedTitle);
                } catch (Exception e) {
                    log.error("Scraping failed: {}", e.getMessage());
                    throw e;
                }
                if (contentToAnalyze.length() > 20000) {
                    contentToAnalyze = contentToAnalyze.substring(0, 20000);
                }
            }

            String aiJson = modelRouterService.analyzeWithFailover(contentToAnalyze);

// NEW ROBUST CLEANER
            String cleanedJson = aiJson.trim();

            cleanedJson = cleanedJson.replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("\\s*```$", "");

            int firstBrace = cleanedJson.indexOf("{");
            int lastBrace = cleanedJson.lastIndexOf("}");

            if (firstBrace == -1 || lastBrace == -1 || firstBrace >= lastBrace) {
                log.error("Invalid JSON structure in AI response: {}", aiJson);
                throw new RuntimeException("AI returned malformed JSON - no valid object");
            }

            cleanedJson = cleanedJson.substring(firstBrace, lastBrace + 1);

            cleanedJson = cleanedJson.replaceAll(",\\s*}", "}")
                    .replaceAll(",\\s*]", "]")
                    .trim();

            log.info("Final cleaned JSON: {}", cleanedJson);

            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            objectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true); // Extra safety

            Map<String, Object> result = objectMapper.readValue(cleanedJson,
                    new TypeReference<Map<String, Object>>() {});

            AuditReport report = new AuditReport();
            String title;
            if ("url".equalsIgnoreCase(requestType)) {
                if (extractedTitle != null && !extractedTitle.equals("Untitled Article")) {
                    title = extractedTitle;
                } else {
                    title = request.getText();
                }
            } else {
                title = request.getText().length() > 50
                        ? request.getText().substring(0, 50) + "..."
                        : request.getText();
            }
            report.setTitle(title);
            report.setUrlOrText(request.getText());
            report.setType(requestType);
            report.setStatus("completed");
            report.setIpAddress(clientIp);
            if(result.containsKey("score")) {
                report.setScore((Map<String, Object>) result.get("score"));
            }
            if(result.containsKey("audits")) {
                report.setAudits((List<Map<String, Object>>) result.get("audits"));
            }
            if(result.containsKey("suggestions")) {
                report.setSuggestions((List<Map<String, Object>>) result.get("suggestions"));
            }
            if(result.containsKey("comparison")) {
                report.setComparison((Map<String, Object>) result.get("comparison"));
            } else {
                report.setComparison(Map.of("topRanking", 92, "industryAverage", 68));
            }
            AuditReport savedReport = repository.save(report);
            log.info("Report saved with ID: {}", savedReport.getId());
            return ResponseEntity.ok(savedReport);
        } catch (Exception e) {
            log.error("Analysis failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Analysis failed",
                            "message", e.getMessage(),
                            "details", e.getClass().getSimpleName()
                    ));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AuditReport>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc(pageable));
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<AuditReport> getReport(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/report/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(@PathVariable String id) {
        if(!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "timestamp", java.time.LocalDateTime.now().toString()));
    }
}