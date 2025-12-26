package com.aeo.analyzer.controller;

import com.aeo.analyzer.dto.AnalyzeRequest;
import com.aeo.analyzer.model.AuditReport;
import com.aeo.analyzer.repository.AuditReportRepository;
import com.aeo.analyzer.service.ModelRouterService;
import com.aeo.analyzer.service.WebScraperService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/content")
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:2000"},
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
        String requestType = request.getType() != null ? request.getType() : "text";

        // 1️⃣ Sanitization Logic Fix
        // URL එකක් නම් විතරක් Clean කරන්න, නැත්නම් Text එක එහෙමම තියන්න
        String contentToProcess = request.getText();
        if ("url".equalsIgnoreCase(requestType)) {
            // XSS ආරක්ෂාව සඳහා URL එක සුද්ද කරනවා
            contentToProcess = Jsoup.clean(contentToProcess, Safelist.none());
            log.info("🔍 Analysis requested for URL: {}", contentToProcess);
        }

        try {
            String contentToAnalyze = contentToProcess;
            String extractedTitle = null;

            // 2️⃣ URL Scraping Logic
            if ("url".equalsIgnoreCase(requestType)) {
                log.info("Scraping URL: {}", contentToProcess);
                try {
                    // Clean කරපු URL එක යවනවා
                    contentToAnalyze = webScraperService.scrapeUrl(contentToProcess);
                    extractedTitle = webScraperService.extractTitleFromUrl(contentToProcess);
                } catch (Exception e) {
                    log.error("Scraping failed: {}", e.getMessage());
                    throw e;
                }

                // Content එක දිග වැඩි නම් කපනවා (Tokens ඉතිරි කරන්න)
                if (contentToAnalyze.length() > 20000) {
                    contentToAnalyze = contentToAnalyze.substring(0, 20000);
                }
            }

            // AI Service Call
            String aiJson = modelRouterService.analyzeWithFailover(contentToAnalyze);

            // --- JSON CLEANING LOGIC (As is) ---
            String cleanedJson = aiJson.trim();
            cleanedJson = cleanedJson.replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("\\s*```$", "");

            int firstBrace = cleanedJson.indexOf("{");
            int lastBrace = cleanedJson.lastIndexOf("}");

            if (firstBrace == -1 || lastBrace == -1 || firstBrace >= lastBrace) {
                log.error("Invalid JSON structure in AI response: {}", aiJson);
                throw new RuntimeException("AI returned malformed JSON");
            }

            cleanedJson = cleanedJson.substring(firstBrace, lastBrace + 1);
            cleanedJson = cleanedJson.replaceAll(",\\s*}", "}")
                    .replaceAll(",\\s*]", "]")
                    .trim();

            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            objectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);

            Map<String, Object> result = objectMapper.readValue(cleanedJson,
                    new TypeReference<Map<String, Object>>() {});

            // --- REPORT SAVING ---
            AuditReport report = new AuditReport();
            String title;

            if ("url".equalsIgnoreCase(requestType)) {
                title = (extractedTitle != null && !extractedTitle.equals("Untitled Article"))
                        ? extractedTitle
                        : contentToProcess;
            } else {
                title = request.getText().length() > 50
                        ? request.getText().substring(0, 50) + "..."
                        : request.getText();
            }

            report.setTitle(title);
            report.setUrlOrText(request.getText()); // Original text එකම save කරනවා
            report.setType(requestType);
            report.setStatus("completed");
            report.setIpAddress(clientIp);

            if(result.containsKey("score")) report.setScore((Map<String, Object>) result.get("score"));
            if(result.containsKey("audits")) report.setAudits((List<Map<String, Object>>) result.get("audits"));
            if(result.containsKey("suggestions")) report.setSuggestions((List<Map<String, Object>>) result.get("suggestions"));

            if(result.containsKey("comparison")) {
                report.setComparison((Map<String, Object>) result.get("comparison"));
            } else {
                report.setComparison(Map.of("topRanking", 92, "industryAverage", 68));
            }

            AuditReport savedReport = repository.save(report);
            return ResponseEntity.ok(savedReport);

        } catch (Exception e) {
            log.error("Analysis failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Analysis failed", "message", e.getMessage()));
        }
    }

    // Other Endpoints (GET, DELETE) can stay the same
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
        if(!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
    }
}