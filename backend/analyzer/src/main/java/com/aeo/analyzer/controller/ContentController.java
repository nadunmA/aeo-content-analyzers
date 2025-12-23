package com.aeo.analyzer.controller;

import com.aeo.analyzer.service.WebScraperService;
import lombok.extern.slf4j.Slf4j;
import com.aeo.analyzer.dto.AnalyzeRequest;
import com.aeo.analyzer.model.AuditReport;
import com.aeo.analyzer.repository.AuditReportRepository;
import com.aeo.analyzer.service.GeminiService;
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
        allowedHeaders = "*",
        maxAge = 3600
)
public class ContentController {

    private final GeminiService geminiService;
    private final AuditReportRepository repository;
    private final ObjectMapper objectMapper;
    private final WebScraperService webScraperService;


    //service connect controller (dependency injection)
    public ContentController(GeminiService geminiService, AuditReportRepository repository, ObjectMapper objectMapper, WebScraperService webScraperService) {
        this.geminiService = geminiService;
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.webScraperService = webScraperService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Object> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            HttpServletRequest httpServletRequest) {

        String clientIp = httpServletRequest.getRemoteAddr();
        log.info("Analysis request from IP: {}", clientIp);

        try {
            //Determine Content Source (URL or Text)
            String contentToAnalyze = request.getText();
            String requestType = request.getType() != null ? request.getType() : "text";


            if ("url".equalsIgnoreCase(requestType)) {
                log.info("Scraping URL: {}", request.getText());
                contentToAnalyze = webScraperService.scrapeUrl(request.getText());


                if (contentToAnalyze.length() > 20000) {
                    contentToAnalyze = contentToAnalyze.substring(0, 20000);
                }
            }

            //Call Gemini Service with the actual content
            String geminiJson = geminiService.analyzeContent(contentToAnalyze);

            //Clean Gemini Response
            String cleanedJson = geminiJson.trim();
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.substring(7);
            } else if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.substring(3);
            }
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.length() - 3);
            }
            cleanedJson = cleanedJson.trim();

            //Configure Mapper for safety
            objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

            Map<String, Object> result = objectMapper.readValue(cleanedJson,
                    new TypeReference<Map<String, Object>>() {});


            AuditReport report = new AuditReport();


            String title;
            if ("url".equalsIgnoreCase(requestType)) {
                title = request.getText();
            } else {
                title = request.getText().length() > 50 ? request.getText().substring(0, 50) + "..." : request.getText();
            }

            report.setTitle(title);
            report.setUrlOrText(request.getText());
            report.setType(requestType); // "url" or "text"
            report.setStatus("completed");
            report.setIpAddress(clientIp);

            //Add Analysis Data
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

            //Save to DB
            AuditReport savedReport = repository.save(report);
            log.info("Report saved with ID: {}", savedReport.getId());

            return ResponseEntity.ok(savedReport);

        } catch (Exception e) {
            log.error("Analysis failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Analysis failed", "message", e.getMessage()));
        }
    }

    //Get history
    @GetMapping("/history")
    public ResponseEntity<Page<AuditReport>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc(pageable));
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
