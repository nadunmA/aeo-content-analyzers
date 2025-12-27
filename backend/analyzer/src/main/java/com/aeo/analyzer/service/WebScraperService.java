package com.aeo.analyzer.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Random;

@Service
public class WebScraperService {

    private static final Logger log = LoggerFactory.getLogger(WebScraperService.class);
    private static final int MAX_CONTENT_LENGTH = 80000;

    // බොරු User Agents
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    };

    private final Random random = new Random();

    public String scrapeUrl(String url) throws IOException {
        validateUrl(url);

        // 1. මුලින්ම Playwright ට්‍රයි කරනවා (Best Quality)
        try {
            return executePlaywright(url);
        } catch (Exception e) {
            log.warn("⚠️ Playwright failed. Switching to Jsoup fallback. Error: {}", e.getMessage());

            // 2. Playwright බැරි වුනොත් Jsoup ට්‍රයි කරනවා (Backup)
            return scrapeWithJsoup(url);
        }
    }

    private String executePlaywright(String url) {
        // අලුත් Playwright Version එකට ගැළපෙන settings
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(List.of(
                            "--disable-gpu",
                            "--no-sandbox",
                            "--disable-dev-shm-usage",
                            "--disable-extensions",
                            "--disable-blink-features=AutomationControlled"
                    ));

            Browser browser = playwright.chromium().launch(launchOptions);

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setUserAgent(USER_AGENTS[random.nextInt(USER_AGENTS.length)])
                    .setViewportSize(1920, 1080)
                    .setJavaScriptEnabled(true)
                    .setIgnoreHTTPSErrors(true);

            BrowserContext context = browser.newContext(contextOptions);
            context.setDefaultTimeout(45000); // 45 seconds timeout

            Page page = context.newPage();
            log.info("🌍 Playwright Navigating to: {}", url);

            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            // පොඩ්ඩක් ඉන්නවා (Anti-bot මගහරින්න)
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

            // Cloudflare Check
            String title = page.title();
            if (title != null && (title.contains("Just a moment") || title.contains("Security Check"))) {
                throw new RuntimeException("Blocked by Cloudflare");
            }

            String content;
            try {
                if (page.locator("article").count() > 0) {
                    content = page.locator("article").innerText();
                } else if (page.locator("main").count() > 0) {
                    content = page.locator("main").innerText();
                } else {
                    content = page.locator("body").innerText();
                }
            } catch (Exception e) {
                content = "";
            }

            context.close();
            browser.close();

            if (content.isBlank()) throw new RuntimeException("Empty content from Playwright");
            return cleanText(content);
        }
    }

    // Jsoup Fallback Method
    private String scrapeWithJsoup(String url) throws IOException {
        log.info("⚡ Attempting Jsoup scraping for: {}", url);
        String userAgent = USER_AGENTS[random.nextInt(USER_AGENTS.length)];

        Document doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .referrer("https://www.google.com")
                .timeout(30000)
                .followRedirects(true)
                .ignoreHttpErrors(true) // 403 ආවත් නවතින්නේ නෑ
                .get();

        // Unwanted elements අයින් කරනවා
        doc.select("script, style, noscript, iframe, nav, header, footer, aside, .ad").remove();

        Element article = doc.selectFirst("article");
        String text = (article != null) ? article.text() : doc.body().text();

        return cleanText(text);
    }

    private String cleanText(String text) {
        if (text == null || text.isBlank()) return "";
        String cleaned = text.replaceAll("\\s+", " ").trim();
        if (cleaned.length() > MAX_CONTENT_LENGTH) {
            return cleaned.substring(0, MAX_CONTENT_LENGTH) + "... [truncated]";
        }
        return cleaned;
    }

    private void validateUrl(String urlString) throws IOException {
        try {
            URL url = new URL(urlString);
            String host = url.getHost();
            InetAddress address = InetAddress.getByName(host);
            if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
                throw new IOException("Private IP addresses not allowed");
            }
        } catch (Exception e) {
            throw new IOException("Invalid URL");
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
            return "Analyzed Content";
        }
    }
}
