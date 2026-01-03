package com.aeo.analyzer.controller;

import com.aeo.analyzer.dto.AnalyzeRequest;
import com.aeo.analyzer.model.AuditReport;
import com.aeo.analyzer.repository.AuditReportRepository;
import com.aeo.analyzer.service.ModelRouterService;
import com.aeo.analyzer.service.WebScraperService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    private static final String STATUS_KEY = "status";
    private static final String AUDITS_KEY = "audits";
    private static final String SCORE_KEY = "score";
    private static final String FAILED_VAL = "failed";

    private final ModelRouterService modelRouterService;
    private final AuditReportRepository repository;
    private final WebScraperService webScraperService;

    public ContentController(AuditReportRepository repository,
                             WebScraperService webScraperService,
                             ModelRouterService modelRouterService) {
        this.repository = repository;
        this.webScraperService = webScraperService;
        this.modelRouterService = modelRouterService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Object> analyze(@Valid @RequestBody AnalyzeRequest request,
                                          HttpServletRequest httpServletRequest) {
        try {
            String clientIp = httpServletRequest.getRemoteAddr();
            String type = request.getType() != null ? request.getType().toLowerCase() : "text";

            // 1. Content Extraction
            String contentToAnalyze = extractContent(request, type);
            String title = "url".equals(type) ? webScraperService.extractTitleFromUrl(request.getText()) : request.getText();

            // 2. AI Processing
            String aiRaw = modelRouterService.analyzeWithFailover(contentToAnalyze);
            Map<String, Object> result = parseAiResponse(aiRaw);

            // 3. Logic & Self-healing
            processAuditsAndScores(result);


            log.info("💾 Final result before save - Score: {}", result.get(SCORE_KEY));


            AuditReport report = createReportObject(request, result, type, title, clientIp);
            AuditReport saved = repository.save(report);


            log.info("✅ Saved report ID: {} with score: {}", saved.getId(), saved.getScore());

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("❌ Analysis failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Analysis failed", "message", e.getMessage()));
        }
    }

    private String extractContent(AnalyzeRequest request, String type) throws IOException {
        String content = request.getText();
        if ("url".equals(type)) {
            content = webScraperService.scrapeUrl(content);
            if (content.length() > 20000) return content.substring(0, 20000);
        }
        return content;
    }

    private Map<String, Object> parseAiResponse(String aiJson) throws IOException {
        String cleaned = aiJson.trim()
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("\\s*```$", "");

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1) {
            cleaned = cleaned.substring(start, end + 1);
        }

        cleaned = cleaned.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.readValue(cleaned, new TypeReference<Map<String, Object>>() {});


        if (!result.containsKey(SCORE_KEY) || result.get(SCORE_KEY) == null) {
            result.put(SCORE_KEY, new HashMap<String, Object>());
            log.warn("⚠️ AI response missing 'score' field - initialized empty map");
        }

        if (!result.containsKey(AUDITS_KEY) || result.get(AUDITS_KEY) == null) {
            result.put(AUDITS_KEY, new java.util.ArrayList<>());
            log.warn("⚠️ AI response missing 'audits' field - initialized empty list");
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void processAuditsAndScores(Map<String, Object> result) {
        Object auditsObj = result.get(AUDITS_KEY);
        if (auditsObj instanceof List) {
            List<Map<String, Object>> audits = (List<Map<String, Object>>) auditsObj;
            for (Map<String, Object> audit : audits) {
                normalizeAudit(audit);
            }
            calculateFinalScore(result, audits);
        } else {

            List<Map<String, Object>> emptyAudits = new java.util.ArrayList<>();
            result.put(AUDITS_KEY, emptyAudits);
            calculateFinalScore(result, emptyAudits);
        }
    }

    private void normalizeAudit(Map<String, Object> audit) {
        if (!audit.containsKey("title")) {
            Object label = audit.get("label");
            audit.put("title", label != null ? label : "Optimization Check");
        }
        String s = audit.getOrDefault(STATUS_KEY, FAILED_VAL).toString().toLowerCase();
        audit.put(STATUS_KEY, s);
    }

    @SuppressWarnings("unchecked")
    private void calculateFinalScore(Map<String, Object> result, List<Map<String, Object>> audits) {

        Map<String, Object> scoreMap = (Map<String, Object>) result.computeIfAbsent(SCORE_KEY, k -> new HashMap<>());


        Object overallObj = scoreMap.get("overall");
        Integer overallScore = extractIntegerValue(overallObj);


        if (overallScore == null || overallScore == 0) {
            double totalPoints = 0;
            int totalAudits = audits.size();

            for (Map<String, Object> audit : audits) {
                String status = audit.getOrDefault(STATUS_KEY, FAILED_VAL).toString().toLowerCase();
                if (status.contains("pass")) {
                    totalPoints += 1.0;
                } else if (status.contains("warning")) {
                    totalPoints += 0.5;
                }
                // "fail" adds 0
            }

            int calculatedScore = totalAudits == 0 ? 0 : (int) Math.round((totalPoints / totalAudits) * 100);


            scoreMap.put("overall", calculatedScore);


            ensureSubScore(scoreMap, "structure", calculatedScore);
            ensureSubScore(scoreMap, "readability", calculatedScore);
            ensureSubScore(scoreMap, "seo", calculatedScore);


            result.put(SCORE_KEY, scoreMap);

            log.info("🎯 Score Self-healed: overall={}, audits={}/{}", calculatedScore, (int)totalPoints, totalAudits);
        } else {

            ensureSubScore(scoreMap, "structure", overallScore);
            ensureSubScore(scoreMap, "readability", overallScore);
            ensureSubScore(scoreMap, "seo", overallScore);
            result.put(SCORE_KEY, scoreMap);
            log.info("✅ Score already exists: overall={}", overallScore);
        }
    }

    /**
     * Helper method to extract integer from various types
     */
    private Integer extractIntegerValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String str) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException e) {
                log.warn("⚠️ Cannot parse string to integer: {}", str);
                return null;
            }
        }

        return null;
    }

    /**
     * Helper method to ensure subscore exists
     */
    private void ensureSubScore(Map<String, Object> scoreMap, String key, int defaultValue) {
        Object existing = scoreMap.get(key);
        Integer value = extractIntegerValue(existing);

        if (value == null || value == 0) {
            scoreMap.put(key, defaultValue);
        }
    }

    @SuppressWarnings("unchecked")
    private AuditReport createReportObject(AnalyzeRequest req, Map<String, Object> res, String type, String title, String ip) {
        AuditReport report = new AuditReport();

        String finalTitle = title != null && title.length() > 190 ? title.substring(0, 190) + "..." : title;
        report.setTitle(finalTitle);

        String text = req.getText();
        report.setUrlOrText(text != null && text.length() > 95000 ? text.substring(0, 95000) : text);

        report.setType(type);
        report.setIpAddress(ip);
        report.setStatus("completed");

        // Safely get score map
        Object scoreObj = res.get(SCORE_KEY);
        if (scoreObj instanceof Map) {
            report.setScore((Map<String, Object>) scoreObj);
        } else {
            // Fallback: create minimal score map
            Map<String, Object> fallbackScore = new HashMap<>();
            fallbackScore.put("overall", 0);
            fallbackScore.put("structure", 0);
            fallbackScore.put("readability", 0);
            fallbackScore.put("seo", 0);
            report.setScore(fallbackScore);
            log.warn("⚠️ Score object was not a Map, using fallback");
        }

        report.setAudits((List<Map<String, Object>>) res.get(AUDITS_KEY));
        report.setSuggestions((List<Map<String, Object>>) res.get("suggestions"));

        return report;
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AuditReport>> getHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)));
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<AuditReport> getReport(@PathVariable String id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/debug/report/{id}")
    public ResponseEntity<Map<String, Object>> debugReport(@PathVariable String id) {
        return repository.findById(id)
                .map(report -> {
                    Map<String, Object> debug = new HashMap<>();
                    debug.put("id", report.getId());
                    debug.put("title", report.getTitle());
                    debug.put("score", report.getScore());
                    debug.put("audits_count", report.getAudits() != null ? report.getAudits().size() : 0);
                    debug.put("status", report.getStatus());
                    return ResponseEntity.ok(debug);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/report/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(@PathVariable String id) {
        if(!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
    }
}
