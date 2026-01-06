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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/content")
@CrossOrigin(origins = "*") // සරල බව සඳහා දැනට මෙසේ යොදා ඇත
public class ContentController {

    private static final Logger log = LoggerFactory.getLogger(ContentController.class);

    private static final String STATUS_KEY = "status";
    private static final String AUDITS_KEY = "audits";
    private static final String SCORE_KEY = "score";
    private static final String SUGGESTIONS_KEY = "suggestions";
    private static final String TITLE_KEY = "title";
    private static final String OVERALL_KEY = "overall";

    private final ModelRouterService modelRouterService;
    private final AuditReportRepository repository;
    private final WebScraperService webScraperService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContentController(ModelRouterService modelRouterService,
                             AuditReportRepository repository,
                             WebScraperService webScraperService) {
        this.modelRouterService = modelRouterService;
        this.repository = repository;
        this.webScraperService = webScraperService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Object> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            HttpServletRequest httpServletRequest) {

        try {
            String clientIp = httpServletRequest.getRemoteAddr();
            String type = (request.getType() != null) ? request.getType().toLowerCase() : "text";

            String contentToAnalyze = extractContent(request, type);
            String title = extractTitle(request, type);

            // AI විශ්ලේෂණය ලබා ගැනීම
            String aiRaw = modelRouterService.analyzeWithFailover(contentToAnalyze);
            Map<String, Object> result = parseAiResponse(aiRaw);

            // AI Model එකේ නම ලබා ගැනීම
            String modelInfo = (String) result.getOrDefault("model_info", "AI Model");

            processAuditsAndScores(result);

            AuditReport report = createReportObject(request, result, type, title, clientIp);

            // Model එකේ නම Report එකට ඇතුළත් කිරීම
            report.setModelUsed(modelInfo);

            AuditReport saved = repository.save(report);

            log.info("Analysis completed via {}. Report ID: {}", modelInfo, saved.getId());
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Analysis failed: {}", e.getMessage());
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

    private String extractTitle(AnalyzeRequest request, String type) {
        if ("url".equals(type)) return webScraperService.extractTitleFromUrl(request.getText());
        return request.getText();
    }

    private Map<String, Object> parseAiResponse(String aiJson) throws IOException {
        String cleaned = aiJson.trim().replace("```json", "").replace("```", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1) cleaned = cleaned.substring(start, end + 1);

        return objectMapper.readValue(cleaned, new TypeReference<>() {});
    }

    private void processAuditsAndScores(Map<String, Object> result) {
        List<Map<String, Object>> audits = (List<Map<String, Object>>) result.getOrDefault(AUDITS_KEY, new ArrayList<>());
        audits.forEach(audit -> {
            if (!audit.containsKey(TITLE_KEY)) audit.put(TITLE_KEY, "Optimization Check");
            audit.put(STATUS_KEY, audit.getOrDefault(STATUS_KEY, "failed").toString().toLowerCase());
        });
        result.put(AUDITS_KEY, audits);
    }

    private AuditReport createReportObject(AnalyzeRequest req, Map<String, Object> res, String type, String title, String ip) {
        AuditReport report = new AuditReport();
        report.setTitle(title.length() > 190 ? title.substring(0, 190) + "..." : title);
        report.setUrlOrText(req.getText().length() > 95000 ? req.getText().substring(0, 95000) : req.getText());
        report.setType(type);
        report.setIpAddress(ip);
        report.setStatus("completed");
        report.setScore(extractScoreMap(res));
        report.setAudits((List<Map<String, Object>>) res.get(AUDITS_KEY));
        report.setSuggestions((List<Map<String, Object>>) res.get(SUGGESTIONS_KEY));
        return report;
    }

    private Map<String, Object> extractScoreMap(Map<String, Object> response) {
        Object scoreObj = response.get(SCORE_KEY);
        if (scoreObj instanceof Map) return (Map<String, Object>) scoreObj;
        return Map.of("overall", 0, "structure", 0, "readability", 0, "seo", 0);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AuditReport>> getHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)));
    }
}
