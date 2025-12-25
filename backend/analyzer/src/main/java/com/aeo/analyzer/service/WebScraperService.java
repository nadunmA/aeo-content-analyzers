package com.aeo.analyzer.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;

@Service
public class WebScraperService {

    private static final Logger log = LoggerFactory.getLogger(WebScraperService.class);

    private static final int MIN_CONTENT_LENGTH = 200;
    private static final int MAX_CONTENT_LENGTH = 80000;

    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    };

    private final Random random = new Random();

    public String scrapeUrl(String url) throws IOException {
        validateUrl(url);

        // Primary: Playwright (modern, faster, better anti-bot resistance)
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true); // Set false for local debugging

            Browser browser = playwright.chromium().launch(launchOptions);

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setUserAgent(USER_AGENTS[random.nextInt(USER_AGENTS.length)])
                    .setViewportSize(1920, 1080)
                    .setLocale("en-US")
                    .setTimezoneId("Asia/Colombo")
                    .setJavaScriptEnabled(true)
                    .setBypassCSP(true);

            BrowserContext context = browser.newContext(contextOptions);
            Page page = context.newPage();

            // Navigate with timeout
            page.navigate(url, new Page.NavigateOptions().setTimeout(40000));

            // Wait for network idle (all resources loaded)
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Human-like behavior
            Thread.sleep(random.nextInt(2000) + 1000);
            page.evaluate("window.scrollTo(0, document.body.scrollHeight / 2)");
            Thread.sleep(1000);
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(1000);

            // Cloudflare / Anti-bot detection
            String pageTitle = page.title().toLowerCase();
            String pageSource = page.content().toLowerCase();

            if (pageTitle.contains("just a moment") ||
                    pageTitle.contains("attention required") ||
                    pageTitle.contains("access denied") ||
                    pageTitle.contains("verify you are human") ||
                    pageSource.contains("cf-browser-verification") ||
                    pageSource.contains("checking your browser") ||
                    pageSource.contains("cloudflare")) {

                log.warn("⛔ Anti-bot protection detected (likely Cloudflare) on: {}", url);
                context.close();
                browser.close();
                throw new IOException("Site is protected by Cloudflare or similar anti-bot system");
            }

            String htmlContent = page.content();

            context.close();
            browser.close();

            // Clean HTML with Jsoup
            Document doc = Jsoup.parse(htmlContent);
            doc.select("script, style, noscript, iframe, nav, header, footer, aside, " +
                    ".advertisement, .ad, [class*=ads], [id*=ads], .sidebar, .comments, " +
                    ".related-posts, .social-share, .newsletter").remove();

            Element articleBody = findArticleBody(doc);
            if (articleBody == null) {
                articleBody = doc.body();
            }

            String cleanText = articleBody.text()
                    .replaceAll("\\s+", " ")
                    .trim();

            if (cleanText.length() < MIN_CONTENT_LENGTH) {
                throw new IOException("Extracted content too short – possibly blocked or paywalled");
            }

            if (cleanText.length() > MAX_CONTENT_LENGTH) {
                cleanText = cleanText.substring(0, MAX_CONTENT_LENGTH) + "... [content truncated]";
            }

            log.info("Successfully scraped {} characters from {}", cleanText.length(), url);
            return cleanText;

        } catch (PlaywrightException e) {
            log.warn("Playwright failed for {}: {}. Falling back to Jsoup.", url, e.getMessage());
            return scrapeWithJsoup(url);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Scraping interrupted", e);
        } catch (Exception e) {
            log.error("Unexpected error during Playwright scraping: {}", e.getMessage());
            throw new IOException("Playwright scraping failed: " + e.getMessage(), e);
        }
    }

    private String scrapeWithJsoup(String url) throws IOException {
        try {
            Thread.sleep(random.nextInt(2000) + 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String userAgent = USER_AGENTS[random.nextInt(USER_AGENTS.length)];

        Document doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .referrer("https://www.google.com")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .timeout(20000)
                .followRedirects(true)
                .maxBodySize(0)
                .get();

        doc.select("script, style, noscript, iframe, nav, header, footer, aside, " +
                ".advertisement, .ad, [class*=ads], [id*=ads], .sidebar, .comments, " +
                ".related-posts, .social-share, .newsletter").remove();

        Element articleBody = findArticleBody(doc);
        if (articleBody == null) articleBody = doc.body();

        String cleanText = articleBody.text()
                .replaceAll("\\s+", " ")
                .trim();

        if (cleanText.length() < MIN_CONTENT_LENGTH) {
            throw new IOException("Jsoup extracted content too short");
        }

        if (cleanText.length() > MAX_CONTENT_LENGTH) {
            cleanText = cleanText.substring(0, MAX_CONTENT_LENGTH) + "... [truncated]";
        }

        return cleanText;
    }

    private Element findArticleBody(Document doc) {
        String[] selectors = {
                "article[data-testid=storyContent]",
                "article section",
                "div[class*=postArticle]",
                ".meteredContent",
                "article",
                "div.post-content, div.entry-content, div.content",
                "main article",
                "main",
                "div.story-body, div.article-body",
                "[role=article]",
                ".post-body, .article-content, .e-content"
        };

        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                return elements.stream()
                        .max((a, b) -> Integer.compare(a.text().length(), b.text().length()))
                        .orElse(elements.first());
            }
        }

        Elements allDivs = doc.select("div");
        return allDivs.stream()
                .filter(div -> div.select("p").size() > 3)
                .max((a, b) -> Integer.compare(a.text().length(), b.text().length()))
                .orElse(null);
    }

    private void validateUrl(String urlString) throws IOException {
        try {
            URL url = new URL(urlString);
            String protocol = url.getProtocol().toLowerCase();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                throw new IOException("Invalid protocol: " + protocol);
            }

            String host = url.getHost();
            InetAddress inetAddress = InetAddress.getByName(host);
            if (inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress() ||
                    inetAddress.isLinkLocalAddress() || inetAddress.isAnyLocalAddress()) {
                throw new IOException("Invalid host: Local or private IP addresses are not allowed");
            }
        } catch (MalformedURLException | UnknownHostException e) {
            throw new IOException("Invalid URL: " + e.getMessage());
        }
    }

    public String extractTitleFromUrl(String url) {
        try {
            Thread.sleep(random.nextInt(1000) + 500);

            String userAgent = USER_AGENTS[random.nextInt(USER_AGENTS.length)];

            Document doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .referrer("https://www.google.com")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml")
                    .timeout(10000)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .get();

            return extractTitle(doc);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Untitled Article";
        } catch (Exception e) {
            log.warn("Failed to extract title from URL: {}", e.getMessage());
            return "Untitled Article";
        }
    }

    public String extractTitle(Document doc) {
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null && !ogTitle.attr("content").isBlank()) {
            return cleanTitle(ogTitle.attr("content"));
        }

        Element twitterTitle = doc.selectFirst("meta[name=twitter:title]");
        if (twitterTitle != null && !twitterTitle.attr("content").isBlank()) {
            return cleanTitle(twitterTitle.attr("content"));
        }

        Element h1 = doc.selectFirst("h1");
        if (h1 != null && !h1.text().isBlank()) {
            return cleanTitle(h1.text());
        }

        String title = doc.title();
        if (title != null && !title.isBlank()) {
            title = title.replaceAll("(?i)\\s*[|\\-–—]\\s*.+$", "").trim();
            return cleanTitle(title);
        }

        return "Untitled Article";
    }

    private String cleanTitle(String title) {
        return title.replaceAll("\\s+", " ")
                .replaceAll("[\\r\\n]+", "")
                .trim();
    }
}