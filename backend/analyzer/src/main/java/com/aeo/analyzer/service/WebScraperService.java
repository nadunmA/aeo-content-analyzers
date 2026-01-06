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

    // Standard SLF4J Logger replacement for @Slf4j
    private static final Logger log = LoggerFactory.getLogger(WebScraperService.class);

    // Constants
    private static final int MAX_CONTENT_LENGTH = 80000;
    private static final int PLAYWRIGHT_TIMEOUT = 45000;
    private static final int JSOUP_TIMEOUT = 30000;
    private static final int TITLE_EXTRACTION_TIMEOUT = 5000;
    private static final int ANTI_BOT_WAIT_MS = 2000;
    private static final int VIEWPORT_WIDTH = 1920;
    private static final int VIEWPORT_HEIGHT = 1080;

    private static final String ARTICLE_TAG = "article";
    private static final String MAIN_TAG = "main";
    private static final String BODY_TAG = "body";
    private static final String DEFAULT_TITLE = "Analyzed Website Content";
    private static final String TRUNCATED_SUFFIX = "... [truncated]";

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
            log.warn("Playwright failed for URL: {}. Error: {}. Switching to Jsoup fallback.", url, e.getMessage());
            return scrapeWithJsoup(url);
        } catch (Exception e) {
            throw new IOException("Failed to scrape URL: " + url, e);
        }
    }

    public String extractTitleFromUrl(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(getRandomUserAgent())
                    .timeout(TITLE_EXTRACTION_TIMEOUT)
                    .get()
                    .title();
        } catch (IOException e) {
            log.warn("Could not extract title for URL: {}. Using default title.", url);
            return DEFAULT_TITLE;
        }
    }

    private String executePlaywright(String url) {
        try (Playwright playwright = Playwright.create();
             Browser browser = launchBrowser(playwright)) {

            BrowserContext context = createBrowserContext(browser);
            Page page = context.newPage();

            log.info("Navigating to URL with Playwright: {}", url);
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            waitToAvoidBotDetection();
            checkForCloudflareProtection(page);

            String content = extractPageContent(page);

            if (content.isBlank()) {
                throw new IllegalStateException("Empty content extracted from Playwright");
            }

            return cleanText(content);
        }
    }

    private Browser launchBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of(
                        "--disable-gpu",
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-extensions",
                        "--disable-blink-features=AutomationControlled"
                )));
    }

    private BrowserContext createBrowserContext(Browser browser) {
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent(getRandomUserAgent())
                .setViewportSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));

        context.setDefaultTimeout(PLAYWRIGHT_TIMEOUT);
        return context;
    }

    private void waitToAvoidBotDetection() {
        try {
            Thread.sleep(ANTI_BOT_WAIT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Wait interrupted during bot detection avoidance", e);
        }
    }

    private void checkForCloudflareProtection(Page page) {
        String title = page.title();
        if (title != null && (title.contains("Just a moment") || title.contains("Security Check"))) {
            throw new IllegalStateException("Blocked by Cloudflare protection");
        }
    }

    private String extractPageContent(Page page) {
        if (page.locator(ARTICLE_TAG).count() > 0) {
            return page.locator(ARTICLE_TAG).innerText();
        }
        if (page.locator(MAIN_TAG).count() > 0) {
            return page.locator(MAIN_TAG).innerText();
        }
        return page.locator(BODY_TAG).innerText();
    }

    private String scrapeWithJsoup(String url) throws IOException {
        log.info("Attempting Jsoup scraping for: {}", url);

        Document doc = Jsoup.connect(url)
                .userAgent(getRandomUserAgent())
                .timeout(JSOUP_TIMEOUT)
                .get();

        removeUnwantedElements(doc);

        Element article = doc.selectFirst(ARTICLE_TAG);
        String text = (article != null) ? article.text() : doc.body().text();

        return cleanText(text);
    }

    private void removeUnwantedElements(Document doc) {
        doc.select("script, style, noscript, nav, header, footer, aside, .ad").remove();
    }

    private String cleanText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String cleaned = text.replaceAll("\\s+", " ").trim();

        if (cleaned.length() > MAX_CONTENT_LENGTH) {
            return cleaned.substring(0, MAX_CONTENT_LENGTH) + TRUNCATED_SUFFIX;
        }

        return cleaned;
    }

    private void validateUrl(String urlString) throws IOException {
        try {
            URL url = new URL(urlString);
            InetAddress address = InetAddress.getByName(url.getHost());

            if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
                throw new IOException("Private IP addresses are not allowed");
            }
        } catch (UnknownHostException e) {
            throw new IOException("Invalid URL host: " + urlString, e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Invalid URL structure: " + urlString, e);
        }
    }

    private String getRandomUserAgent() {
        return USER_AGENTS[random.nextInt(USER_AGENTS.length)];
    }
}
