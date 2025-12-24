package com.aeo.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenRouterService {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterService.class);

    @Value("${openrouter.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public OpenRouterService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String analyzeContent(String text, String modelName) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Content Limit
        String limitedText = text.substring(0, Math.min(text.length(), 15000));

        // Improved Prompt with detailed structure
        String prompt = """
            You are an AEO (Answer Engine Optimization) expert. 
            Analyze the following content for SEO, readability, and technical aspects.
            Return ONLY a strict JSON object with this exact structure:
            {
              "score": {
                "total": <integer 0-100>,
                "seo": <integer 0-100>,
                "readability": <integer 0-100>,
                "technical": <integer 0-100>
              },
              "audits": [
                {
                  "title": "<string>",
                  "status": "<pass|fail|warn>",
                  "description": "<string>"
                },
                ... more audits
              ],
              "suggestions": [
                {
                  "priority": "<high|medium|low>",
                  "category": "<seo|readability|technical>",
                  "description": "<string>",
                  "impact": "<string>"
                },
                ... more suggestions
              ],
              "comparison": {
                "topRanking": <integer 0-100>,
                "industryAverage": <integer 0-100>
              }
            }
            Do not include any extra text, explanations, or markdown outside the JSON.
            Content to analyze: %s
            """.formatted(limitedText);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful assistant."),
                Map.of("role", "user", "content", prompt)
        ));

        // Force JSON format
        requestBody.put("response_format", Map.of("type", "json_object"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("🔄 Calling OpenRouter Model: {}", modelName);

        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

        // Extract Content from OpenAI-style response
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("choices").path(0).path("message").path("content").asText();
    }
}