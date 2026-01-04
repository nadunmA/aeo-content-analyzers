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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:2000"},
        methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.DELETE},
        allowedHeaders = "*",
        allowCredentials = "true"
)
public class ContentController {


    private static final String STATUS_KEY = "status";
    private static final String AUDITS_KEY = "audits";
    private static final String SCORE_KEY = "score";
    private static final String SUGGESTIONS_KEY = "suggestions";
    private static final String TITLE_KEY = "title";


    private static final String OVERALL_KEY = "overall";
    private static final String STRUCTURE_KEY = "structure";
    private static final String READABILITY_KEY = "readability";
    private static final String SEO_KEY = "seo";


    private static final String FAILED_VAL = "failed";
    private static final String PASS_VAL = "pass";
    private static final String WARNING_VAL = "warning";


    private static final int MAX_CONTENT_LENGTH = 20000;
    private static final int MAX_TITLE_LENGTH = 190;
    private static final int MAX_TEXT_LENGTH = 95000;
    private static final int PERCENTAGE_MULTIPLIER = 100;


    private static final double PASS_POINTS = 1.0;
    private static final double WARNING_POINTS = 0.5;
    private static final double FAIL_POINTS = 0.0;


    private final ModelRouterService modelRouterService;
    private final AuditReportRepository repository;
    private final WebScraperService webScraperService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Main endpoint to analyze content from URL or text

    @PostMapping("/analyze")
    public ResponseEntity<Object> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            HttpServletRequest httpServletRequest) {

        try {
            String clientIp = httpServletRequest.getRemoteAddr();
            String type = determineContentType(request.getType());

            String contentToAnalyze = extractContent(request, type);
            String title = extractTitle(request, type);

            String aiRaw = modelRouterService.analyzeWithFailover(contentToAnalyze);
            Map<String, Object> result = parseAiResponse(aiRaw);

            processAuditsAndScores(result);

            AuditReport report = createReportObject(request, result, type, title, clientIp);
            AuditReport saved = repository.save(report);

            log.info("Analysis completed successfully. Report ID: {}, Score: {}",
                    saved.getId(), saved.getScore().get(OVERALL_KEY));

            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            log.error("IO error during analysis: {}", e.getMessage());
            return buildErrorResponse("Content extraction failed", e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return buildErrorResponse("Invalid input", e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unexpected error during analysis: {}", e.getMessage(), e);
            return buildErrorResponse("Analysis failed", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AuditReport>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)));
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<AuditReport> getReport(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/debug/report/{id}")
    public ResponseEntity<Map<String, Object>> debugReport(@PathVariable String id) {
        return repository.findById(id)
                .map(this::buildDebugResponse)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/report/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(@PathVariable String id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
    }

    private String determineContentType(String type) {
        return (type != null) ? type.toLowerCase() : "text";
    }

    private String extractContent(AnalyzeRequest request, String type) throws IOException {
        String content = request.getText();

        if ("url".equals(type)) {
            content = webScraperService.scrapeUrl(content);
            if (content.length() > MAX_CONTENT_LENGTH) {
                return content.substring(0, MAX_CONTENT_LENGTH);
            }
        }

        return content;
    }

    private String extractTitle(AnalyzeRequest request, String type) {
        if ("url".equals(type)) {
            return webScraperService.extractTitleFromUrl(request.getText());
        }
        return request.getText();
    }

    private Map<String, Object> parseAiResponse(String aiJson) throws IOException {
        String cleaned = cleanJsonResponse(aiJson);
        Map<String, Object> result = objectMapper.readValue(cleaned, new TypeReference<>() {});

        validateAndInitializeResponseStructure(result);

        return result;
    }

    private String cleanJsonResponse(String aiJson) {
        String cleaned = aiJson.trim()
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("\\s*```\\s*$", "");

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');

        if (start != -1 && end != -1) {
            cleaned = cleaned.substring(start, end + 1);
        }

        return cleaned.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    }

    private void validateAndInitializeResponseStructure(Map<String, Object> result) {
        if (!result.containsKey(SCORE_KEY) || result.get(SCORE_KEY) == null) {
            result.put(SCORE_KEY, new HashMap<String, Object>());
            log.warn("AI response missing score field - initialized empty map");
        }

        if (!result.containsKey(AUDITS_KEY) || result.get(AUDITS_KEY) == null) {
            result.put(AUDITS_KEY, new ArrayList<>());
            log.warn("AI response missing audits field - initialized empty list");
        }
    }

    @SuppressWarnings("unchecked")
    private void processAuditsAndScores(Map<String, Object> result) {
        Object auditsObj = result.get(AUDITS_KEY);
        List<Map<String, Object>> audits = (auditsObj instanceof List)
                ? (List<Map<String, Object>>) auditsObj
                : new ArrayList<>();

        audits.forEach(this::normalizeAudit);
        result.put(AUDITS_KEY, audits);

        calculateFinalScore(result, audits);
    }

    private void normalizeAudit(Map<String, Object> audit) {
        if (!audit.containsKey(TITLE_KEY)) {
            Object label = audit.get("label");
            audit.put(TITLE_KEY, Objects.requireNonNullElse(label, "Optimization Check"));
        }

        String status = audit.getOrDefault(STATUS_KEY, FAILED_VAL).toString().toLowerCase();
        audit.put(STATUS_KEY, status);
    }

    @SuppressWarnings("unchecked")
    private void calculateFinalScore(Map<String, Object> result, List<Map<String, Object>> audits) {
        Map<String, Object> scoreMap = (Map<String, Object>) result.computeIfAbsent(
                SCORE_KEY, k -> new HashMap<>());

        Integer overallScore = extractIntegerValue(scoreMap.get(OVERALL_KEY));

        if (shouldRecalculateScore(overallScore)) {
            int calculatedScore = computeScoreFromAudits(audits);
            updateAllScores(scoreMap, calculatedScore);
            log.info("Score self-healed: overall={}, audits={}/{}",
                    calculatedScore, countPassedAudits(audits), audits.size());
        } else {
            ensureAllSubScores(scoreMap, overallScore);
            log.debug("Score already valid: overall={}", overallScore);
        }

        result.put(SCORE_KEY, scoreMap);
    }

    private boolean shouldRecalculateScore(Integer score) {
        return score == null || score == 0;
    }

    private int computeScoreFromAudits(List<Map<String, Object>> audits) {
        if (audits.isEmpty()) {
            return 0;
        }

        double totalPoints = audits.stream()
                .mapToDouble(this::calculateAuditPoints)
                .sum();

        return (int) Math.round((totalPoints / audits.size()) * PERCENTAGE_MULTIPLIER);
    }

    private double calculateAuditPoints(Map<String, Object> audit) {
        String status = audit.getOrDefault(STATUS_KEY, FAILED_VAL).toString().toLowerCase();

        if (status.contains(PASS_VAL)) {
            return PASS_POINTS;
        }
        if (status.contains(WARNING_VAL)) {
            return WARNING_POINTS;
        }
        return FAIL_POINTS;
    }

    private long countPassedAudits(List<Map<String, Object>> audits) {
        return audits.stream()
                .filter(audit -> {
                    String status = audit.getOrDefault(STATUS_KEY, FAILED_VAL).toString().toLowerCase();
                    return status.contains(PASS_VAL);
                })
                .count();
    }

    private void updateAllScores(Map<String, Object> scoreMap, int score) {
        scoreMap.put(OVERALL_KEY, score);
        scoreMap.put(STRUCTURE_KEY, score);
        scoreMap.put(READABILITY_KEY, score);
        scoreMap.put(SEO_KEY, score);
    }

    private void ensureAllSubScores(Map<String, Object> scoreMap, Integer defaultValue) {
        ensureSubScore(scoreMap, STRUCTURE_KEY, defaultValue);
        ensureSubScore(scoreMap, READABILITY_KEY, defaultValue);
        ensureSubScore(scoreMap, SEO_KEY, defaultValue);
    }

    private void ensureSubScore(Map<String, Object> scoreMap, String key, Integer defaultValue) {
        Integer existing = extractIntegerValue(scoreMap.get(key));

        if (existing == null || existing == 0) {
            scoreMap.put(key, defaultValue);
        }
    }

    private Integer extractIntegerValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer integer) {
            return integer;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String str) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException e) {
                log.warn("Cannot parse string to integer: {}", str);
                return null;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private AuditReport createReportObject(
            AnalyzeRequest req,
            Map<String, Object> res,
            String type,
            String title,
            String ip) {

        AuditReport report = new AuditReport();

        report.setTitle(truncateTitle(title));
        report.setUrlOrText(truncateText(req.getText()));
        report.setType(type);
        report.setIpAddress(ip);
        report.setStatus("completed");
        report.setScore(extractScoreMap(res));
        report.setAudits((List<Map<String, Object>>) res.get(AUDITS_KEY));
        report.setSuggestions((List<Map<String, Object>>) res.get(SUGGESTIONS_KEY));

        return report;
    }

    private String truncateTitle(String title) {
        if (title != null && title.length() > MAX_TITLE_LENGTH) {
            return title.substring(0, MAX_TITLE_LENGTH) + "...";
        }
        return title;
    }

    private String truncateText(String text) {
        if (text != null && text.length() > MAX_TEXT_LENGTH) {
            return text.substring(0, MAX_TEXT_LENGTH);
        }
        return text;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractScoreMap(Map<String, Object> response) {
        Object scoreObj = response.get(SCORE_KEY);

        if (scoreObj instanceof Map) {
            return (Map<String, Object>) scoreObj;
        }

        log.warn("Score object was not a Map, using fallback");
        return createFallbackScoreMap();
    }

    private Map<String, Object> createFallbackScoreMap() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put(OVERALL_KEY, 0);
        fallback.put(STRUCTURE_KEY, 0);
        fallback.put(READABILITY_KEY, 0);
        fallback.put(SEO_KEY, 0);
        return fallback;
    }

    private ResponseEntity<Map<String, Object>> buildDebugResponse(AuditReport report) {
        Map<String, Object> debug = new HashMap<>();
        debug.put("id", report.getId());
        debug.put(TITLE_KEY, report.getTitle());
        debug.put(SCORE_KEY, report.getScore());
        debug.put("auditsCount", report.getAudits() != null ? report.getAudits().size() : 0);
        debug.put(STATUS_KEY, report.getStatus());
        debug.put("type", report.getType());
        debug.put("createdAt", report.getCreatedAt());
        return ResponseEntity.ok(debug);
    }

    private ResponseEntity<Object> buildErrorResponse(String error, String message, HttpStatus status) {
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("error", error);
        errorBody.put("message", message);
        errorBody.put("timestamp", new Date().toString());
        return ResponseEntity.status(status).body(errorBody);
    }
}
