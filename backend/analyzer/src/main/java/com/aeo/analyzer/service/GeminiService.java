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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

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

    public String analyzeContent(String text) throws Exception {
        String fullUrl = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String limitedText = text.substring(0, Math.min(text.length(), 15000));

        String finalPrompt = """
            You are an expert in Answer Engine Optimization (AEO).
            Analyze the provided content for:
            - Schema Markup (structured data)
            - Structure & Q&A (headings, lists, questions)
            - Readability (clarity, engagement)
            
            Provide scores (0-100) and detailed audits/suggestions in strict JSON.
            
            CONTENT TO ANALYZE:
            ===================
            %s
            ===================
            """.formatted(limitedText);

        Map<String, Object> part = Map.of("text", finalPrompt);

        // Gemini-compatible schema (no additionalProperties)
        Map<String, Object> scoreSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "total", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                        "schema", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                        "structure", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                        "readability", Map.of("type", "integer", "minimum", 0, "maximum", 100)
                ),
                "required", List.of("total", "schema", "structure", "readability")
        );

        Map<String, Object> auditSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "title", Map.of("type", "string"),
                        "status", Map.of("type", "string", "enum", List.of("pass", "warning", "fail")),
                        "description", Map.of("type", "string")
                ),
                "required", List.of("title", "status", "description")
        );

        Map<String, Object> suggestionSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "type", Map.of("type", "string", "enum", List.of("schema", "qa", "summary", "content")),
                        "title", Map.of("type", "string"),
                        "explanation", Map.of("type", "string"),
                        "code", Map.of("type", "string")
                ),
                "required", List.of("type", "title", "explanation")
        );

        Map<String, Object> comparisonSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "topRanking", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                        "industryAverage", Map.of("type", "integer", "minimum", 0, "maximum", 100)
                ),
                "required", List.of("topRanking", "industryAverage")
        );

        Map<String, Object> responseSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "score", scoreSchema,
                        "comparison", comparisonSchema,
                        "audits", Map.of("type", "array", "items", auditSchema),
                        "suggestions", Map.of("type", "array", "items", suggestionSchema)
                ),
                "required", List.of("score", "comparison", "audits", "suggestions")
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", List.of(part))));

        requestBody.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "responseSchema", responseSchema,
                "temperature", 0.3,
                "maxOutputTokens", 5000
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Calling Gemini API (Input length: {})", limitedText.length());
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            if (response.getBody() == null || response.getBody().trim().isEmpty()) {
                log.warn("Empty response from Gemini");
                throw new RuntimeException("Empty response from Gemini");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String responseText = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText("");

            if (responseText.isBlank()) {
                log.warn("No text content in Gemini response");
                throw new RuntimeException("No content in Gemini response");
            }

            String cleaned = responseText.trim()
                    .replaceAll("^```json\\s*", "")
                    .replaceAll("^```\\s*", "")
                    .replaceAll("\\s*```$", "")
                    .trim();

            if (!cleaned.startsWith("{") || !cleaned.endsWith("}")) {
                log.warn("Invalid JSON from Gemini: {}", responseText);
                throw new RuntimeException("Invalid JSON format from Gemini");
            }

            log.info("✅ Gemini structured output successful!");
            return cleaned;

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Gemini quota exceeded (429) - Triggering failover to OpenRouter models");
            throw e; // Critical: Throw to enable failover
        } catch (ResourceAccessException e) {
            log.error("Gemini connection/timeout error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Gemini API unexpected error: {}", e.getMessage(), e);
            return null;
        }
    }
}