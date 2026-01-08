package com.aeo.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {
    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);


    private static final String TYPE_STR = "type";
    private static final String OBJ_STR = "object";
    private static final String STR_STR = "string";
    private static final String INT_STR = "integer";
    private static final String PROP_STR = "properties";
    private static final String REQ_STR = "required";
    private static final String DESC_STR = "description";
    private static final String TITLE_STR = "title";

    @Value("${gemini.api.url}")
    private String apiUrl;
    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String analyzeContent(String text) throws IOException {
        String fullUrl = apiUrl + "?key=" + apiKey;
        String limitedText = text.substring(0, Math.min(text.length(), 15000));

        String finalPrompt = String.format("""
            Analyze this content for AEO (Answer Engine Optimization).
            Return scores, detailed audits, and ACTIONABLE suggestions.
            For each suggestion, provide a 'description' and 'codeSnippet'.

            CONTENT:
            %s
            """, limitedText);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", finalPrompt)))));
        requestBody.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "responseSchema", buildFullResponseSchema(),
                "temperature", 0.2
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, new HttpHeaders());
        return executeRequest(fullUrl, entity);
    }

    private Map<String, Object> buildFullResponseSchema() {
        // Suggestions Schema
        Map<String, Object> suggestionSchema = Map.of(
                TYPE_STR, OBJ_STR,
                PROP_STR, Map.of(
                        TYPE_STR, Map.of(TYPE_STR, STR_STR, "enum", List.of("STRUCTURE", "CONTENT", "QA", "TECHNICAL")),
                        TITLE_STR, Map.of(TYPE_STR, STR_STR),
                        DESC_STR, Map.of(TYPE_STR, STR_STR),
                        "codeSnippet", Map.of(TYPE_STR, STR_STR)
                ),
                REQ_STR, List.of(TYPE_STR, TITLE_STR, DESC_STR, "codeSnippet")
        );

        // Audit Schema
        Map<String, Object> auditSchema = Map.of(
                TYPE_STR, OBJ_STR,
                PROP_STR, Map.of(
                        TITLE_STR, Map.of(TYPE_STR, STR_STR),
                        "status", Map.of(TYPE_STR, STR_STR, "enum", List.of("pass", "warning", "fail")),
                        DESC_STR, Map.of(TYPE_STR, STR_STR)
                ),
                REQ_STR, List.of(TITLE_STR, "status", DESC_STR)
        );

        return Map.of(
                TYPE_STR, OBJ_STR,
                PROP_STR, Map.of(
                        "score", Map.of(
                                TYPE_STR, OBJ_STR,
                                PROP_STR, Map.of(
                                        "overall", Map.of(TYPE_STR, INT_STR, "minimum", 0, "maximum", 100),
                                        "structure", Map.of(TYPE_STR, INT_STR, "minimum", 0, "maximum", 100),
                                        "readability", Map.of(TYPE_STR, INT_STR, "minimum", 0, "maximum", 100),
                                        "seo", Map.of(TYPE_STR, INT_STR, "minimum", 0, "maximum", 100)
                                ),
                                REQ_STR, List.of("overall", "structure", "readability", "seo")
                        ),
                        "audits", Map.of(TYPE_STR, "array", "items", auditSchema),
                        "suggestions", Map.of(TYPE_STR, "array", "items", suggestionSchema)
                ),
                REQ_STR, List.of("score", "audits", "suggestions")
        );
    }

    private String executeRequest(String url, HttpEntity<Map<String, Object>> entity) throws IOException {
        try {
            log.info("Calling Gemini Direct API...");
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            String responseText = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
            return responseText.trim()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
        } catch (HttpClientErrorException.TooManyRequests | ResourceAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Gemini failure: " + e.getMessage(), e);
        }
    }
}
