package com.aeo.analyzer.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class WebScraperService {

    public String scrapeUrl(String url) throws IOException {

        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000) // තත්පර 10ක් බලන් ඉන්නවා
                .get();


        return document.body().text();
    }
}