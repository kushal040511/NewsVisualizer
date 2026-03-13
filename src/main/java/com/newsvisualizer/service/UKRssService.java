package com.newsvisualizer.service;

import com.newsvisualizer.model.NewsArticle;
import com.newsvisualizer.model.NewsResponse;
import com.newsvisualizer.model.Source;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for fetching UK news from RSS feeds
 */
public class UKRssService {
    private static final Logger logger = LoggerFactory.getLogger(UKRssService.class);
    
    // Comprehensive list of UK news RSS feeds
    private static final Map<String, String> UK_RSS_FEEDS = new LinkedHashMap<String, String>() {{
        // Major UK News Sources
        put("BBC News", "https://feeds.bbci.co.uk/news/rss.xml");
        put("The Guardian", "https://www.theguardian.com/uk/rss");
        put("Sky News", "https://feeds.skynews.com/feeds/rss/home.xml");
        put("The Independent", "https://www.independent.co.uk/rss");
        put("Reuters UK", "https://feeds.reuters.com/reuters/UKdomesticNews");
        put("Evening Standard", "https://www.standard.co.uk/rss");
        put("Daily Mail", "https://www.dailymail.co.uk/home/index.rss");
        put("The Telegraph", "https://www.telegraph.co.uk/rss.xml");
        put("GOV.UK News", "https://www.gov.uk/government/news.atom");
    }};
    
    private final CloseableHttpClient httpClient;
    private final SyndFeedInput feedInput;
    
    public UKRssService() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(15000)
                .setSocketTimeout(15000)
                .setRedirectsEnabled(true)
                .build();
        
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setUserAgent("NewsVisualizer/1.0 (RSS Reader)")
                .build();
        this.feedInput = new SyndFeedInput();
    }
    
    /**
     * Fetch UK news from RSS feeds
     */
    public NewsResponse getUKNews(String category) {
        logger.info("Fetching UK news from RSS feeds for category: {}", category != null ? category : "general");
        
        List<NewsArticle> allArticles = new ArrayList<>();
        int successfulFeeds = 0;
        int feedsToProcess = Math.min(6, UK_RSS_FEEDS.size()); // Limit to 6 feeds for faster response
        
        int currentFeed = 0;
        for (Map.Entry<String, String> feed : UK_RSS_FEEDS.entrySet()) {
            if (currentFeed >= feedsToProcess) break;
            
            try {
                List<NewsArticle> articles = fetchFromRssFeed(feed.getValue(), feed.getKey());
                if (articles != null && !articles.isEmpty()) {
                    allArticles.addAll(articles);
                    successfulFeeds++;
                    logger.debug("Successfully fetched {} articles from {}", articles.size(), feed.getKey());
                }
                
                currentFeed++;
            } catch (Exception e) {
                logger.warn("Failed to fetch from {}: {}", feed.getKey(), e.getMessage());
                currentFeed++;
            }
        }
        
        // Sort by published date (most recent first)
        allArticles.sort((a, b) -> {
            if (a.getPublishedAt() == null && b.getPublishedAt() == null) return 0;
            if (a.getPublishedAt() == null) return 1;
            if (b.getPublishedAt() == null) return -1;
            return b.getPublishedAt().compareTo(a.getPublishedAt());
        });
        
        // Limit to 30 most recent articles
        if (allArticles.size() > 30) {
            allArticles = allArticles.subList(0, 30);
        }
        
        NewsResponse response = new NewsResponse();
        response.setStatus("ok");
        response.setTotalResults(allArticles.size());
        response.setArticles(allArticles);
        
        logger.info("Successfully fetched {} articles from {} UK RSS feeds", 
                   allArticles.size(), successfulFeeds);
        
        return response;
    }
    
    /**
     * Fetch articles from a single RSS feed
     */
    private List<NewsArticle> fetchFromRssFeed(String feedUrl, String sourceName) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            HttpGet request = new HttpGet(feedUrl);
            request.setHeader("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml");
            request.setHeader("User-Agent", "NewsVisualizer/1.0 (RSS Reader)");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    logger.warn("RSS feed {} returned status: {}", sourceName, 
                               response.getStatusLine().getStatusCode());
                    return articles;
                }
                
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try (XmlReader reader = new XmlReader(entity.getContent())) {
                        SyndFeed feed = feedInput.build(reader);
                        
                        for (SyndEntry entry : feed.getEntries()) {
                            NewsArticle article = convertToNewsArticle(entry, sourceName);
                            if (article != null) {
                                articles.add(article);
                            }
                            
                            // Limit articles per feed
                            if (articles.size() >= 8) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching RSS feed from {}: {}", sourceName, e.getMessage());
        }
        
        return articles;
    }
    
    /**
     * Convert RSS entry to NewsArticle
     */
    private NewsArticle convertToNewsArticle(SyndEntry entry, String sourceName) {
        try {
            NewsArticle article = new NewsArticle();
            
            // Title
            String title = entry.getTitle();
            if (title == null || title.trim().isEmpty()) {
                return null; // Skip articles without title
            }
            article.setTitle(cleanText(title));
            
            // Description
            String description = "";
            if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
                description = cleanText(entry.getDescription().getValue());
            }
            article.setDescription(description);
            
            // Content (use description as content if no separate content)
            article.setContent(description.length() > 0 ? description : "Full article available at source.");
            
            // URL
            article.setUrl(entry.getLink() != null ? entry.getLink() : "");
            
            // Published date
            LocalDateTime publishedAt;
            if (entry.getPublishedDate() != null) {
                publishedAt = entry.getPublishedDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
            } else {
                publishedAt = LocalDateTime.now();
            }
            article.setPublishedAt(publishedAt);
            
            // Source
            Source source = new Source();
            source.setName(sourceName);
            source.setCountry("gb");
            source.setLanguage("en");
            article.setSource(source);
            
            return article;
        } catch (Exception e) {
            logger.debug("Error converting RSS entry to article: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Clean HTML tags and decode entities from text
     */
    private String cleanText(String text) {
        if (text == null) return "";
        
        return text
                .replaceAll("<[^>]+>", "") // Remove HTML tags
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ") // Replace multiple whitespaces with single space
                .trim();
    }
    
    /**
     * Get available UK news categories
     */
    public List<String> getAvailableCategories() {
        return Arrays.asList("general", "business", "technology", "sports", "health", "entertainment");
    }
    
    /**
     * Get list of available RSS sources
     */
    public List<String> getAvailableSources() {
        return new ArrayList<>(UK_RSS_FEEDS.keySet());
    }
    
    /**
     * Test RSS feed connectivity
     */
    public boolean testRssFeed(String feedUrl) {
        try {
            HttpGet request = new HttpGet(feedUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return response.getStatusLine().getStatusCode() == 200;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Close resources
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            logger.error("Error closing HTTP client", e);
        }
    }
}