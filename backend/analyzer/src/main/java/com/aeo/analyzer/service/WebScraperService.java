package com.aeo.analyzer.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Service
public class WebScraperService {

    private static final int TIMEOUT_MS = 15000; // 15 sec

    public String scrapeUrl(String url) throws IOException {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .referrer("https://www.google.com")
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(false)
                    .get();

            //Remove noise elements first
            doc.select("script, style, noscript, iframe, nav, header, footer, aside, " +
                    ".advertisement, .ad, [class*=ads], [id*=ads], .sidebar, .comments, .related-posts").remove();

            //Try to find main article content
            Element articleBody = findArticleBody(doc);

            //Fallback to body if no article found
            if (articleBody == null) {
                articleBody = doc.body();
            }

            //Extract and clean text
            String cleanText = articleBody.text()
                    .replaceAll("\\s+", " ")
                    .trim();

            //Limit length for Gemini API
            if (cleanText.length() > 80000) {
                cleanText = cleanText.substring(0, 80000) + "... [content truncated]";
            }

            if (cleanText.length() < 200) {
                throw new IOException("Extracted content too short – possibly not an article page");
            }

            return cleanText;

        } catch (SocketTimeoutException e) {
            throw new IOException("Request timed out – page took too long to load", e);
        } catch (UnknownHostException e) {
            throw new IOException("Invalid or unreachable URL: " + url, e);
        } catch (IOException e) {
            throw new IOException("Failed to fetch or parse page: " + e.getMessage(), e);
        }
    }

    private Element findArticleBody(Document doc) {
        String[] selectors = {
                "article",
                "div.post-content, div.entry-content, div.content",
                "main",
                "div.story-body, div.article-body",
                "div.post, div.blog-post",
                "[role=article]",
                ".post-body, .article-content, .hentry"
        };

        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {

                return elements.stream()
                        .max((a, b) -> Integer.compare(a.text().length(), b.text().length()))
                        .orElse(elements.first());
            }
        }
        return null;
    }

    //Extract clean title
    public String extractTitle(Document doc) {
        //Try Open Graph title first
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null && !ogTitle.attr("content").isBlank()) {
            return ogTitle.attr("content");
        }

        //Fallback to <title> tag
        String title = doc.title();
        return (title != null && !title.isBlank()) ? title : "Untitled Article";
    }
}