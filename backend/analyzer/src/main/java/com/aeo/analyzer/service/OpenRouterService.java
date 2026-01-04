package com.aeo.analyzer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenRouterService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeContent(String content, String modelName) {
        try {
            log.info("🔄 Calling OpenRouter model: {}", modelName);


            String safeContent = content.replace("\"", "'");
            if (safeContent.length() > 20000) {
                safeContent = safeContent.substring(0, 20000);
            }

            String prompt = String.format("""
    You are an expert AEO (Answer Engine Optimization) Auditor.
    Analyze the provided content and return a HIGHLY DETAILED analysis in valid JSON format.

    CRITICAL INSTRUCTIONS:
    1. 'status' MUST be lowercase: passed | warning | failed.
    2. 'suggestions' array MUST contain specific, actionable advice.
    3. Each suggestion MUST have a clear 'title' and a 2-3 sentence 'description' explaining HOW to fix it.
    4. If no code is applicable, set 'codeSnippet' to "".

    CONTENT:
    "%s"

    REQUIRED JSON STRUCTURE:
    {
      "score": { "overall": 85, "structure": 80, "readability": 90, "seo": 85 },
      "audits": [
        { "title": "Schema", "label": "Schema", "status": "failed", "score": 0, "description": "No valid Schema.org markup was detected on the page." }
      ],
      "suggestions": [
        {
          "type": "STRUCTURE",
          "title": "Add FAQ Schema",
          "description": "To improve visibility in AI-driven search results, implement JSON-LD FAQ schema for your 'Programming Basics' section. This helps search engines parse your questions and answers directly.",
          "codeSnippet": "<script type='application/ld+json'>...</script>"
        }
      ],
      "comparison": { "topRanking": 92, "industryAverage": 68 }
    }
    """, safeContent);

            Map<String, Object> body = new HashMap<>();
            body.put("model", modelName);
            body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("HTTP-Referer", "http://localhost:3000");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);


            String rawText = extractText(response.getBody());
            String cleanedJson = cleanMarkdown(rawText);


            objectMapper.readTree(cleanedJson);

            log.info("✅ Successfully analyzed with model: {}", modelName);
            return cleanedJson;

        } catch (JsonProcessingException e) {
            log.error("❌ AI Response is not a valid JSON: {}", e.getMessage());
            throw new IllegalStateException("AI returned invalid JSON structure", e);
        } catch (Exception e) {
            log.error("❌ OpenRouter service error: {}", e.getMessage(), e);
            throw new IllegalStateException("AI analysis service failed", e);
        }
    }

    private String cleanMarkdown(String text) {
        if (text == null || text.trim().isEmpty()) return "{}";

        // 1. මුලින්ම දෙපස හිස් තැන් ඉවත් කරන්න
        String cleaned = text.trim();

        // 2. Regex වෙනුවට සරල string replacement භාවිතා කරන්න (ඉතා ආරක්ෂිතයි)
        cleaned = cleaned.replace("```json", "")
                .replace("```", "")
                .trim();

        // 3. යම් හෙයකින් JSON එක මැද තවමත් ``` ලකුණු තිබේ නම් ඒවා ඉවත් කර
        // පිරිසිදු JSON object එක පමණක් ලබා ගන්න
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');

        if (start != -1 && end != -1 && start < end) {
            return cleaned.substring(start, end + 1);
        }

        return cleaned;
    }

    private String extractText(String jsonBody) {
        try {
            if (jsonBody == null) return "";
            JsonNode root = objectMapper.readTree(jsonBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract AI content", e);
        }
    }
}
