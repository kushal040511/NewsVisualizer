package com.newsvisualizer.adapter;

import com.newsvisualizer.model.NewsArticle;
import com.newsvisualizer.model.Source;
import java.time.LocalDateTime;

/**
 * Adapter class to convert between different news item formats
 * Bridges the gap between the legacy newsApp and NewsVisualizer models
 */
public class NewsItemAdapter {
    
    /**
     * Convert a simple news item to NewsArticle format
     * This simulates the conversion from com.project.news.model.NewsItem
     */
    public static NewsArticle convertToNewsArticle(String title, String summary, String url, String sourceName) {
        NewsArticle article = new NewsArticle();
        
        // Basic properties
        article.setTitle(title != null ? title.trim() : "Untitled");
        article.setDescription(summary != null ? summary.trim() : "No description available");
        article.setContent(summary != null ? summary.trim() : "No content available");
        article.setUrl(url != null ? url.trim() : "");
        
        // Set current time as published date (newsApp doesn't seem to have date info)
        article.setPublishedAt(LocalDateTime.now());
        
        // Create source
        Source source = new Source();
        source.setName(sourceName != null ? sourceName : "Unknown Source");
        source.setId(sourceName != null ? sourceName.toLowerCase().replace(" ", "-") : "unknown");
        article.setSource(source);
        
        // Default values
        article.setAuthor("Unknown");
        article.setCategory("general");
        article.setSentimentScore(0.0); // Neutral sentiment by default
        
        return article;
    }
    
    /**
     * Create a NewsArticle from newsApp-style parameters
     */
    public static NewsArticle createFromNewsApp(String title, String summary, String url, String sourceName) {
        return convertToNewsArticle(title, summary, url, sourceName);
    }
    
    /**
     * Validate and clean news item data
     */
    public static boolean isValidNewsItem(String title, String summary, String url, String sourceName) {
        return title != null && !title.trim().isEmpty() && 
               url != null && !url.trim().isEmpty();
    }
}