package com.aeo.analyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ModelRouterService {

    private static final Logger log = LoggerFactory.getLogger(ModelRouterService.class);
    private final GeminiService geminiService;
    private final OpenRouterService openRouterService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY_MS = 2000;

    public ModelRouterService(GeminiService geminiService, OpenRouterService openRouterService) {
        this.geminiService = geminiService;
        this.openRouterService = openRouterService;
    }

    public String analyzeWithFailover(String text) {
        String res;

        // Gemini (Primary with Retry)
        res = tryWithRetry("Gemini", () -> geminiService.analyzeContent(text));
        if (isValid(res)) return injectModelInfo(res, "Gemini (Google)");

        // Claude (Backup 1 with Retry)
        res = tryWithRetry("Claude", () -> openRouterService.analyzeContent(text, "anthropic/claude-3.5-sonnet"));
        if (isValid(res)) return injectModelInfo(res, "Claude 3.5 Sonnet");

        // GPT Fallback (Backup 2 with Retry)
        res = tryWithRetry("GPT-4o-mini", () -> openRouterService.analyzeContent(text, "openai/gpt-4o-mini"));
        if (isValid(res)) return injectModelInfo(res, "GPT-4o-mini");

        return getErrorJson();
    }


    private String tryWithRetry(String modelName, AIRequestBuilder request) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                log.info("Attempting {} (Try {}/{})...", modelName, (i + 1), MAX_RETRIES);
                String result = request.execute();
                if (isValid(result)) return result;
            } catch (Exception e) {
                log.error("{} failed on try {}: {}", modelName, (i + 1), e.getMessage());
                if (i < MAX_RETRIES - 1) {
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        return null;
    }

    private boolean isValid(String res) {
        return res != null && !res.trim().isEmpty() && res.contains("{");
    }

    private String injectModelInfo(String json, String modelName) {
        try {
            ObjectNode node = (ObjectNode) objectMapper.readTree(json);
            node.put("model_info", modelName);
            return node.toString();
        } catch (Exception e) { return json; }
    }

    private String getErrorJson() {
        return "{\"score\":{\"overall\":0},\"audits\":[{\"title\":\"System Unavailable\",\"status\":\"fail\",\"description\":\"All AI models failed after multiple retries. Please check your internet or API limits.\"}]}";
    }

    // Functional Interface for Retries
    @FunctionalInterface
    interface AIRequestBuilder {
        String execute() throws Exception;
    }
}
