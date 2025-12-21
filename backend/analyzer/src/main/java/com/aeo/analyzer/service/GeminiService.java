package com.aeo.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Service
public class GeminiService {

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String analyzeContent(String text) {
        String url = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //AEO Prompt
        String prompt = """
            Perform a comprehensive Answer Engine Optimization (AEO) audit on the following content.
            Content: "%s"
            
            Evaluate based on:
            1. Directness of answers.
            2. Use of structured data (Schema.org).
            3. Clear Q&A formatting.
            4. Key takeaway summaries.
            5. Factual accuracy and citation readiness.

            Return a strict JSON object matching this schema (do not use markdown formatting like ```json):
            {
              "score": { "total": number, "schema": number, "structure": number, "readability": number },
              "audits": [ { "title": string, "status": "pass" | "fail" | "warning", "description": string } ],
              "suggestions": [ { "type": "schema" | "qa" | "summary", "title": string, "code": string, "explanation": string } ]
            }
        """.formatted(text.replace("\"", "\\\""));

        //Request JSON Structure
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        //Generation Config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));
        requestBody.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getBody() == null) {
                return "{}";
            }

            JsonNode rootNode = objectMapper.readTree(response.getBody());

            // 4. Extract Text and Clean it
            String responseText = rootNode.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText();


            return responseText.replace("```json", "").replace("```", "").trim();

        } catch (Exception e) {
            e.printStackTrace();

            return """
                {
                    "score": { "total": 0, "schema": 0, "structure": 0, "readability": 0 },
                    "audits": [ { "title": "Error", "status": "fail", "description": "Analysis failed due to server error." } ],
                    "suggestions": []
                }
            """;
        }
    }
}