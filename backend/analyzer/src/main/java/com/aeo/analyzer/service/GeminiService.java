package com.aeo.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

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

    public String analyzeContent(String text) {
        String fullUrl = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Limit content to 15,000 chars to prevent timeouts/errors
        String limitedText = text.substring(0, Math.min(text.length(), 15000));

        String finalPrompt = """
            You are an expert in Answer Engine Optimization (AEO).
            Perform a comprehensive audit on the provided content below.
            
            Return ONLY a valid JSON object with this exact structure (No Markdown, No code blocks, just raw JSON):
            {
              "score": { "total": 0-100, "schema": 0-100, "structure": 0-100, "readability": 0-100 },
              "comparison": { "topRanking": 90, "industryAverage": 65 },
              "audits": [ { "title": "string", "status": "pass|warning|fail", "description": "string" } ],
              "suggestions": [ { "type": "schema|qa|summary|content", "title": "string", "explanation": "string", "code": "string example" } ]
            }

            CONTENT TO ANALYZE:
            ===================
            %s
            ===================
            """.formatted(limitedText);

        Map<String, Object> part = Map.of("text", finalPrompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(part))
        ));

        requestBody.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.4,
                "maxOutputTokens", 5000
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Calling Gemini API for AEO analysis (Input length: {})", limitedText.length());
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            if (response.getBody() == null || response.getBody().trim().isEmpty()) {
                log.warn("Empty response from Gemini API");
                return getFallbackJson();
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
                return getFallbackJson();
            }

            // CLEANING LOGIC
            String cleaned = responseText
                    .trim()
                    .replaceAll("^```json", "")
                    .replaceAll("^```", "")
                    .replaceAll("```$", "")
                    .trim();

            // Validation with Logging
            if (!cleaned.startsWith("{") || !cleaned.endsWith("}")) {
                log.warn("⚠️ Gemini returned invalid JSON. Response was: {}", responseText); // This helps debug!
                return getFallbackJson();
            }

            log.info("Gemini analysis successful!");
            return cleaned;

        } catch (ResourceAccessException e) {
            log.error("Gemini API timeout or connection error: {}", e.getMessage());
            return getFallbackJson();
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API: {}", e.getMessage(), e);
            return getFallbackJson();
        }
    }

    private String getFallbackJson() {
        return """
            {
              "score": { "total": 0, "schema": 0, "structure": 0, "readability": 0 },
              "comparison": { "topRanking": 0, "industryAverage": 0 },
              "audits": [
                { "title": "Service Unavailable", "status": "fail", "description": "AI Service is currently offline. Please try again later." }
              ],
              "suggestions": []
            }
            """;
    }
}