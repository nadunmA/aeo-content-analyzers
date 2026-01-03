package com.aeo.analyzer.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

@Service
public class WebScraperService {

    private static final Logger log = LoggerFactory.getLogger(WebScraperService.class);
    private static final int MAX_CONTENT_LENGTH = 80000;
    private static final String ARTICLE_TAG = "article";

    // ✅ සියලුම User Agents නැවත ඇතුළත් කළා
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    };

    private final Random random = new Random();

    public String scrapeUrl(String url) throws IOException {
        validateUrl(url);
        try {
            return executePlaywright(url);
        } catch (PlaywrightException | IllegalStateException e) {
            log.warn("⚠️ Playwright failed: {}. Switching to Jsoup fallback.", e.getMessage());
            return scrapeWithJsoup(url);
        } catch (Exception e) {
            throw new IOException("Failed to scrape URL: " + url, e);
        }
    }

    private String executePlaywright(String url) {

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setHeadless(true)
                     .setArgs(List.of(
                             "--disable-gpu",
                             "--no-sandbox",
                             "--disable-dev-shm-usage",
                             "--disable-extensions",
                             "--disable-blink-features=AutomationControlled"
                     )))) {

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(USER_AGENTS[random.nextInt(USER_AGENTS.length)])
                    .setViewportSize(1920, 1080));

            context.setDefaultTimeout(45000);
            Page page = context.newPage();
            log.info("🌍 Playwright Navigating to: {}", url);

            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            // Anti-bot wait
            try { Thread.sleep(2000); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Wait interrupted", e);
            }

            checkCloudflare(page);
            String content = extractPageContent(page);

            if (content.isBlank()) throw new IllegalStateException("Empty content from Playwright");
            return cleanText(content);
        }
    }

    private void checkCloudflare(Page page) {
        String title = page.title();
        if (title != null && (title.contains("Just a moment") || title.contains("Security Check"))) {
            throw new IllegalStateException("Blocked by Cloudflare protection");
        }
    }

    private String extractPageContent(Page page) {
        if (page.locator(ARTICLE_TAG).count() > 0) return page.locator(ARTICLE_TAG).innerText();
        if (page.locator("main").count() > 0) return page.locator("main").innerText();
        return page.locator("body").innerText();
    }

    private String scrapeWithJsoup(String url) throws IOException {
        log.info("⚡ Attempting Jsoup scraping for: {}", url);
        Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENTS[random.nextInt(USER_AGENTS.length)])
                .timeout(30000)
                .get();

        doc.select("script, style, noscript, nav, header, footer, aside, .ad").remove();
        Element article = doc.selectFirst(ARTICLE_TAG);
        return cleanText((article != null) ? article.text() : doc.body().text());
    }

    private String cleanText(String text) {
        if (text == null || text.isBlank()) return "";
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return (cleaned.length() > MAX_CONTENT_LENGTH) ? cleaned.substring(0, MAX_CONTENT_LENGTH) + "... [truncated]" : cleaned;
    }

    private void validateUrl(String urlString) throws IOException {
        try {
            URL url = new URL(urlString);
            InetAddress address = InetAddress.getByName(url.getHost());
            if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
                throw new IOException("Private IP addresses not allowed");
            }
        } catch (UnknownHostException e) {
            throw new IOException("Invalid URL Host: " + urlString, e);
        } catch (Exception e) {
            throw new IOException("Invalid URL structure: " + urlString, e);
        }
    }

    public String extractTitleFromUrl(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENTS[0])
                    .timeout(5000)
                    .get()
                    .title();
        } catch (Exception e) {
            log.warn("Could not extract title for URL: {}. Defaulting title.", url);
            return "Analyzed Website Content";
        }
    }
}
