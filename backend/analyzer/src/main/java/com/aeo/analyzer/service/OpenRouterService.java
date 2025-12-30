package com.aeo.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenRouterService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    private static final String API_URL =
            "https://openrouter.ai/api/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenRouterService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeContent(String content, String modelName) {
        try {
            log.info("🔄 Calling OpenRouter model: {}", modelName);

            // Safe content handling
            String safeContent = content.replace("\"", "'");
            safeContent = safeContent.substring(
                    0, Math.min(safeContent.length(), 20_000)
            );

            String prompt = String.format("""
You are an expert AEO Auditor. Analyze the content and return ONLY valid JSON.

CRITICAL RULES:
1. 'status' MUST be lowercase: passed | warning | failed
2. 'codeSnippet' MUST NEVER be null
3. Content suggestions → Markdown example
4. Technical suggestions → HTML/CSS/JS
5. NO explanations outside JSON

CONTENT:
"%s"

REQUIRED JSON FORMAT:
{
  "score": {
    "overall": 85,
    "structure": 80,
    "readability": 90,
    "seo": 85
  },
  "audits": [
    {
      "title": "Schema Markup",
      "label": "Schema Markup",
      "status": "failed",
      "score": 0,
      "description": "No schema found."
    }
  ],
  "suggestions": [
    {
      "type": "STRUCTURE",
      "title": "Add Schema",
      "description": "Include Article schema.",
      "codeSnippet": "<script type=\\"application/ld+json\\">{}</script>"
    }
  ],
  "comparison": {
    "topRanking": 92,
    "industryAverage": 68
  }
}
""", safeContent);

            // Request body
            Map<String, Object> body = new HashMap<>();
            body.put("model", modelName);
            body.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)
            ));

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("HTTP-Referer", "http://localhost:3000");
            headers.set("X-Title", "AEO Analyzer");
            headers.set("X-Source", "aeo-analyzer-backend");

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(API_URL, entity, String.class);

            String aiText = extractText(response.getBody());

            // Validate JSON strictly
            objectMapper.readTree(aiText);

            return aiText;

        } catch (Exception e) {
            log.error("❌ OpenRouter error: {}", e.getMessage(), e);
            throw new RuntimeException("AI analysis failed");
        }
    }

    private String extractText(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Invalid OpenRouter response");
        }
    }
}
