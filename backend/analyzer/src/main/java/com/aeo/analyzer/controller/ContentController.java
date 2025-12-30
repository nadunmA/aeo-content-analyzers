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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/content")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:2000"},
        methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.DELETE},
        allowedHeaders = "*", allowCredentials = "true")
public class ContentController {

    private final ModelRouterService modelRouterService;
    private final AuditReportRepository repository;
    private final ObjectMapper objectMapper;
    private final WebScraperService webScraperService;

    public ContentController(AuditReportRepository repository, ObjectMapper objectMapper,
                             WebScraperService webScraperService, ModelRouterService modelRouterService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.webScraperService = webScraperService;
        this.modelRouterService = modelRouterService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Object> analyze(@Valid @RequestBody AnalyzeRequest request,
                                          HttpServletRequest httpServletRequest) {
        String clientIp = httpServletRequest.getRemoteAddr();

        String requestType = request.getType() != null ? request.getType().toLowerCase() : "text";
        String contentToProcess = request.getText();

        if ("url".equals(requestType)) {
            // Log raw input for debugging
            log.info("🔍 Analysis requested for URL: {}", contentToProcess);
        }

        try {
            String contentToAnalyze = contentToProcess;
            String extractedTitle = null;

            if ("url".equals(requestType)) {
                try {
                    contentToAnalyze = webScraperService.scrapeUrl(contentToProcess);
                    extractedTitle = webScraperService.extractTitleFromUrl(contentToProcess);
                } catch (Exception e) {
                    log.error("Scraping failed: {}", e.getMessage());
                    throw e;
                }
                // AI Limit (20k chars)
                if (contentToAnalyze.length() > 20000) contentToAnalyze = contentToAnalyze.substring(0, 20000);
            }

            // Call AI
            String aiJson = modelRouterService.analyzeWithFailover(contentToAnalyze);

            // JSON CLEANING
            String cleanedJson = aiJson.trim()
                    .replaceAll("^```json\\s*", "").replaceAll("^```\\s*", "").replaceAll("\\s*```$", "");

            int firstBrace = cleanedJson.indexOf("{");
            int lastBrace = cleanedJson.lastIndexOf("}");
            if (firstBrace != -1 && lastBrace != -1) {
                cleanedJson = cleanedJson.substring(firstBrace, lastBrace + 1);
            }
            cleanedJson = cleanedJson.replaceAll(",\\s*}", "}").replaceAll(",\\s*]", "]");

            log.info("📝 AI JSON RESPONSE:\n{}", cleanedJson);

            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            Map<String, Object> result = objectMapper.readValue(cleanedJson, new TypeReference<Map<String, Object>>() {});

            // ==========================================
            // 🛡️ SELF-HEALING LOGIC
            // ==========================================
            List<Map<String, Object>> audits = (List<Map<String, Object>>) result.get("audits");
            if (audits != null) {
                for (Map<String, Object> audit : audits) {
                    if (!audit.containsKey("title")) {
                        Object label = audit.get("label");
                        Object name = audit.get("name");
                        audit.put("title", label != null ? label : (name != null ? name : "Optimization Check"));
                    }
                    if (audit.containsKey("status")) {
                        String status = audit.get("status").toString().toLowerCase();
                        audit.put("status", status);
                    } else {
                        audit.put("status", "failed");
                    }
                }
            }

            Map<String, Object> scoreMap = (Map<String, Object>) result.get("score");
            if (scoreMap == null) {
                scoreMap = new HashMap<>();
                result.put("score", scoreMap);
            }
            Object overallObj = scoreMap.get("overall");
            int overallScore = 0;
            if (overallObj instanceof Number) overallScore = ((Number) overallObj).intValue();

            if (overallScore == 0 && audits != null && !audits.isEmpty()) {
                double passedCount = 0.0;
                for (Map<String, Object> audit : audits) {
                    String status = (String) audit.getOrDefault("status", "failed");
                    if ("passed".equals(status)) passedCount += 1.0;
                    else if ("warning".equals(status)) passedCount += 0.5;
                }
                overallScore = (int) ((passedCount / audits.size()) * 100);
                scoreMap.put("overall", overallScore);
                scoreMap.put("seo", overallScore);
                scoreMap.put("readability", overallScore + 5 > 100 ? 100 : overallScore + 5);
                scoreMap.put("structure", overallScore);
                log.info("🔧 Auto-calculated Score: {}", overallScore);
            }

            // ==========================================
            // 💾 SAVE TO DB (WITH VALIDATION SAFETY)
            // ==========================================
            AuditReport report = new AuditReport();

            // Truncate Title to 190 chars
            String finalTitle;
            if ("url".equals(requestType)) {
                finalTitle = (extractedTitle != null && !extractedTitle.isEmpty()) ? extractedTitle : contentToProcess;
            } else {
                finalTitle = request.getText();
            }
            if (finalTitle.length() > 190) finalTitle = finalTitle.substring(0, 190) + "...";
            report.setTitle(finalTitle);

            // Truncate Content to 99,000 chars
            String finalContent = request.getText();
            if (finalContent != null && finalContent.length() > 99000) {
                finalContent = finalContent.substring(0, 99000) + "... (truncated)";
            }
            report.setUrlOrText(finalContent);

            report.setType(requestType); // Already lowercased above
            report.setStatus("completed");
            report.setIpAddress(clientIp);

            // Add AI Results
            if(result.containsKey("score")) report.setScore((Map<String, Object>) result.get("score"));
            if(result.containsKey("audits")) report.setAudits(audits);
            if(result.containsKey("suggestions")) report.setSuggestions((List<Map<String, Object>>) result.get("suggestions"));
            if(result.containsKey("comparison")) {
                report.setComparison((Map<String, Object>) result.get("comparison"));
            } else {
                report.setComparison(Map.of("topRanking", 92, "industryAverage", 68));
            }

            log.info("💾 Saving report to DB...");
            return ResponseEntity.ok(repository.save(report));

        } catch (Exception e) {
            // Log the REAL error if saving fails
            log.error("❌ CRITICAL ERROR: Analysis/Save failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Analysis failed", "message", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AuditReport>> getHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)));
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<AuditReport> getReport(@PathVariable String id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/report/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(@PathVariable String id) {
        if(!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
    }
}
