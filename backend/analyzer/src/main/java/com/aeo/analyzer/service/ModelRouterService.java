package com.aeo.analyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ModelRouterService {

    private static final Logger log = LoggerFactory.getLogger(ModelRouterService.class);

    private final GeminiService geminiService;
    private final OpenRouterService openRouterService;

    public ModelRouterService(GeminiService geminiService, OpenRouterService openRouterService) {
        this.geminiService = geminiService;
        this.openRouterService = openRouterService;
    }

    public String analyzeWithFailover(String text) {

        // First: Use Gemini
        try {
            log.info("🚀 [1/4] Attempting Primary Model: Gemini (Google Direct)...");
            String result = geminiService.analyzeContent(text);

            if (result != null && !result.trim().isEmpty()) {
                log.info("✅ Gemini succeeded");
                return result;
            }
            log.warn("⚠️ Gemini returned empty response");

        } catch (Exception e) {
            log.error("❌ Gemini Failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }

        // Second: Use Claude 3 Haiku (assuming still available; update to claude-3.5-haiku if deprecated)
        try {
            log.info("🛡️ [2/4] Trying Backup 1: Claude 3 Haiku...");
            String result = openRouterService.analyzeContent(text, "anthropic/claude-3-haiku");

            if (result != null && !result.trim().isEmpty()) {
                log.info("✅ Claude 3 Haiku succeeded");
                return result;
            }
            log.warn("⚠️ Claude returned empty response");

        } catch (Exception e) {
            log.error("❌ Claude Failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }

        // Third: Use GPT-4o-mini (assuming still available; update to openai/gpt-4o if needed)
        try {
            log.info("🛡️ [3/4] Trying Backup 2: GPT-4o-mini...");
            String result = openRouterService.analyzeContent(text, "openai/gpt-4o-mini");

            if (result != null && !result.trim().isEmpty()) {
                log.info("✅ GPT-4o-mini succeeded");
                return result;
            }
            log.warn("⚠️ GPT-4o-mini returned empty response");

        } catch (Exception e) {
            log.error("❌ GPT-4o-mini Failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }

        // Fourth: Use DeepSeek (assuming still available; update to latest if needed)
        try {
            log.info("🛡️ [4/4] Trying Last Resort: DeepSeek V3...");
            String result = openRouterService.analyzeContent(text, "deepseek/deepseek-chat");

            if (result != null && !result.trim().isEmpty()) {
                log.info("✅ DeepSeek succeeded");
                return result;
            }
            log.warn("⚠️ DeepSeek returned empty response");

        } catch (Exception e) {
            log.error("❌ DeepSeek Failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }

        // All failed
        log.error("❌ CRITICAL: All 4 AI Models Failed!");
        return getErrorJson();
    }

    private String getErrorJson() {
        return """
            {
              "score": {
                "total": 0,
                "seo": 0,
                "readability": 0,
                "technical": 0
              },
              "audits": [{
                "title": "System Unavailable",
                "status": "fail",
                "description": "All AI services (Gemini, Claude, GPT, DeepSeek) are currently unresponsive. Please try again later."
              }],
              "suggestions": [],
              "comparison": {
                "topRanking": 0,
                "industryAverage": 0
              }
            }
            """;
    }
}