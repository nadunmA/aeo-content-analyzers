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

        //first use gemini
        try {

            log.info("🚀 Attempting Primary Model: Gemini (Google Direct)...");
            return geminiService.analyzeContent(text);

        }catch (Exception e) {

            log.warn("⚠️ Gemini Failed. Switching to Claude 3 Haiku...");
        }


        //second use open router Claude 3 Haiku
        try {

            log.info("🛡️ [2/4] Trying Backup 1: Claude 3 Haiku...");
            return openRouterService.analyzeContent(text, "anthropic/claude-3-haiku");

        }catch (Exception e) {

            log.warn("⚠️ Claude Failed. Switching to GPT-4o-mini...");

        }

        //third use GPT-4o-mini
        try {

            log.info("🛡️ [3/4] Trying Backup 2: GPT-4o-mini...");
            return openRouterService.analyzeContent(text, "openai/gpt-4o-mini");

        } catch (Exception e) {

            log.warn("⚠️ GPT-4o-mini Failed. Switching to DeepSeek...");

        }

        //fourth use DeepSeek
        try {

            log.info("🛡️ [4/4] Trying Last Resort: DeepSeek V3...");

            return openRouterService.analyzeContent(text, "deepseek/deepseek-chat");
        } catch (Exception e) {

            log.error("❌ CRITICAL: All 4 AI Models Failed!");
            return getErrorJson();
        }

    }

    private String getErrorJson() {
        return "{ \"score\": { \"total\": 0 }, \"audits\": [{\"title\":\"System Unavailable\", \"status\":\"fail\", \"description\":\"All AI services (Gemini, Claude, GPT, DeepSeek) are currently unresponsive. Please try again later.\"}] }";
    }

}

