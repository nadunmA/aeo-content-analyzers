package com.aeo.analyzer.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class WebScraperService {

    private static final int TIMEOUT_MS = 20000;
    private static final int MIN_CONTENT_LENGTH = 200;
    private static final int MAX_CONTENT_LENGTH = 80000;
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    };

    private final Random random = new Random();
    private boolean seleniumInitialized = false;

    public WebScraperService() {
        try {
            WebDriverManager.chromedriver().setup();
            seleniumInitialized = true;
            System.out.println("✓ Chrome Selenium initialized successfully");
        } catch (Exception e) {
            System.err.println("⚠ Selenium initialization failed: " + e.getMessage());
            seleniumInitialized = false;
        }
    }

    public String scrapeUrl(String url) throws IOException {
        validateUrl(url); // Added for SSRF protection

        // For Medium, directly use Selenium (it requires JS)
        // WARNING: Scraping Medium may violate their ToS. Consider alternatives or obtain permission.
        if (url.contains("medium.com")) {
            if (seleniumInitialized) {
                try {
                    return scrapeWithSelenium(url);
                } catch (Exception e) {
                    throw new IOException("Medium scraping failed: " + e.getMessage());
                }
            } else {
                throw new IOException("Medium requires Selenium which is not available");
            }
        }

        // For other sites, try Jsoup first
        try {
            return scrapeWithJsoup(url);
        } catch (org.jsoup.HttpStatusException e) {
            if (e.getStatusCode() == 403 || e.getStatusCode() == 429) {
                if (seleniumInitialized) {
                    try {
                        return scrapeWithSelenium(url);
                    } catch (Exception seleniumError) {
                        throw new IOException("Both Jsoup and Selenium failed: " + seleniumError.getMessage());
                    }
                } else {
                    throw new IOException("Access denied (403) - Selenium not available. Site may be blocking bots.");
                }
            }
            throw new IOException("HTTP error " + e.getStatusCode() + ": " + e.getMessage());
        }
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
            if (inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isAnyLocalAddress()) {
                throw new IOException("Invalid host: Local or private IP addresses are not allowed");
            }
        } catch (MalformedURLException | UnknownHostException e) {
            throw new IOException("Invalid URL: " + e.getMessage());
        }
    }

    private String scrapeWithJsoup(String url) throws IOException {
        try {
            Thread.sleep(random.nextInt(2000) + 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            String userAgent = USER_AGENTS[random.nextInt(USER_AGENTS.length)];

            Document doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .referrer("https://www.bing.com")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .maxBodySize(0)
                    .ignoreHttpErrors(false)
                    .get();

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

            if (cleanText.length() > MAX_CONTENT_LENGTH) {
                cleanText = cleanText.substring(0, MAX_CONTENT_LENGTH) + "... [content truncated]";
            }

            if (cleanText.length() < MIN_CONTENT_LENGTH) {
                throw new IOException("Extracted content too short – possibly protected");
            }

            return cleanText;

        } catch (java.net.SocketTimeoutException e) {
            throw new IOException("Request timed out – page took too long to load", e);
        } catch (UnknownHostException e) {
            throw new IOException("Invalid or unreachable URL: " + url, e);
        }
    }

    private String scrapeWithSelenium(String url) throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Headless mode for production
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("user-agent=" + USER_AGENTS[random.nextInt(USER_AGENTS.length)]);

        WebDriver driver = null;
        try {
            driver = new ChromeDriver(options);
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Remove webdriver property
            js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            System.out.println("Loading URL: " + url);
            driver.get(url);

            // Wait for page to load
            Thread.sleep(random.nextInt(3000) + 3000);

            // Scroll to trigger lazy loading
            for (int i = 0; i < 3; i++) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight/3 * " + (i + 1) + ");");
                Thread.sleep(1000);
            }

            // Wait for article content
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("article")));
            } catch (Exception e) {
                System.out.println("Article tag not found, continuing...");
            }

            String content = extractContentFromDriver(driver, url);

            if (content.length() < MIN_CONTENT_LENGTH) {
                throw new Exception("Content too short (" + content.length() + " chars) - may be blocked or paywall");
            }

            System.out.println("Successfully extracted " + content.length() + " characters");
            return content;

        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
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

    private String extractContentFromDriver(WebDriver driver, String url) {
        // 👇 1. මෙන්න මේ කොටස අලුතින් එකතු කරන්න (Add this new block)
        String pageTitle = driver.getTitle().toLowerCase();
        // Cloudflare හෝ වෙනත් Bot Guard එකක් ආවද බලනවා
        if (pageTitle.contains("just a moment") ||
                pageTitle.contains("access denied") ||
                pageTitle.contains("attention required") ||
                pageTitle.contains("security check") ||
                pageTitle.contains("verify you are human")) {

            System.err.println("⛔ Cloudflare/Bot protection detected on: " + url);
            return ""; // හිස් එකක් යවනවා (එතකොට Scraper එක Fail කියලා දැනගන්නවා)
        }
        // ⬆️ අලුත් කොටස ඉවරයි


        // 👇 ඔයාගේ පරණ Code එක එහෙමම තියෙන්න දෙන්න
        StringBuilder content = new StringBuilder();

        try {
            // Medium-specific selectors
            if (url.contains("medium.com")) {
                String[] mediumSelectors = {
                        "article section p",
                        "article p",
                        "[data-selectable-paragraph] p",
                        ".pw-post-body-paragraph"
                };

                for (String selector : mediumSelectors) {
                    List<WebElement> paragraphs = driver.findElements(By.cssSelector(selector));
                    if (!paragraphs.isEmpty()) {
                        System.out.println("Found " + paragraphs.size() + " paragraphs with selector: " + selector);
                        for (WebElement p : paragraphs) {
                            String text = p.getText().trim();
                            if (!text.isEmpty() && text.length() > 10) {
                                content.append(text).append("\n\n");
                            }
                        }
                        if (content.length() > MIN_CONTENT_LENGTH) {
                            break;
                        }
                    }
                }
            }

            // Generic selectors
            if (content.length() < MIN_CONTENT_LENGTH) {
                String[] selectors = {
                        "article section p",
                        "article p",
                        "div.postArticle-content p",
                        ".meteredContent p",
                        "main article p",
                        "main p"
                };

                for (String selector : selectors) {
                    List<WebElement> paragraphs = driver.findElements(By.cssSelector(selector));
                    if (!paragraphs.isEmpty() && paragraphs.size() > 3) {
                        for (WebElement p : paragraphs) {
                            String text = p.getText().trim();
                            if (!text.isEmpty() && text.length() > 20) {
                                content.append(text).append("\n\n");
                            }
                        }
                        break;
                    }
                }
            }

            // Fallback: get all paragraphs
            if (content.length() < MIN_CONTENT_LENGTH) {
                List<WebElement> allP = driver.findElements(By.tagName("p"));
                System.out.println("Fallback: Found " + allP.size() + " total paragraphs");
                for (WebElement p : allP) {
                    String text = p.getText().trim();
                    if (!text.isEmpty() && text.length() > 30) {
                        content.append(text).append("\n\n");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error extracting content: " + e.getMessage());
            content.append(driver.findElement(By.tagName("body")).getText());
        }

        String result = content.toString()
                .replaceAll("\\s+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        if (result.length() > MAX_CONTENT_LENGTH) {
            result = result.substring(0, MAX_CONTENT_LENGTH) + "... [content truncated]";
        }

        return result;
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
            System.err.println("Failed to extract title from URL: " + e.getMessage());
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