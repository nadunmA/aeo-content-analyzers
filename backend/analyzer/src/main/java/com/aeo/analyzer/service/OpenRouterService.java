package com.aeo.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private final String apiUrl = "https://openrouter.ai/api/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeContent(String content, String modelName) {
        try {
            log.info("🔄 Calling OpenRouter Model: {}", modelName);

            // ✅ FIX: Prompt uses lowercase 'passed', 'warning', 'failed' strictly!
            String prompt = String.format("""
    You are an expert AEO Auditor. Analyze the content and return a STRICT JSON response.

    CRITICAL INSTRUCTIONS:
    1. For the 'suggestions' array, the 'codeSnippet' field MUST NEVER BE NULL.
    2. If the suggestion is about content (e.g., "Add more examples"), provide a Markdown example of the improved content in 'codeSnippet'.
    3. If the suggestion is technical/SEO, provide the HTML/CSS/JS code.
    4. Force generation of a valid string for 'codeSnippet' in all cases.

    CONTENT:
    "%s"

    ---
    REQUIRED JSON OUTPUT FORMAT:
    {
        "score": {
            "overall": 85,
            "structure": 80,
            "readability": 90,
            "seo": 85
        },
        "audits": [
            {
                "title": "Title Optimization", 
                "label": "Title Optimization", 
                "status": "passed",
                "score": 100,
                "description": "Title is concise."
            },
            {
                "title": "Heading Structure",
                "label": "Heading Structure",
                "status": "warning",
                "score": 60,
                "description": "H1 present but hierarchy skipped."
            },
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
                "title": "Fix Headings",
                "description": "Ensure H1 is followed by H2.",
                "codeSnippet": "<h1>Main Title</h1>\\n<h2>Subheading</h2>\\n<p>Content...</p>"
            },
            {
                "type": "CONTENT",
                "title": "Add Examples",
                "description": "Include real-world scenarios.",
                "codeSnippet": "### Example Scenario\\nHere is a practical example of how this works..."
            }
        ],
        "comparison": {
            "topRanking": 92,
            "industryAverage": 68
        }
    }
    """, content.replace("\"", "'").substring(0, Math.min(content.length(), 20000)));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", "http://localhost:3000");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            return extractTextFromOpenRouterResponse(response.getBody());

        } catch (Exception e) {
            log.error("❌ OpenRouter Failed for model {}: {}", modelName, e.getMessage());
            throw new RuntimeException("AI Service Unavailable");
        }
    }

    private String extractTextFromOpenRouterResponse(String jsonResponse) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(jsonResponse);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return jsonResponse;
        }
    }
}