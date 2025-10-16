package com.newsvisualizer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a news article with all relevant information
 */
public class NewsArticle {
    private String title;
    private String description;
    private String content;
    private String url;
    private String urlToImage;
    private LocalDateTime publishedAt;
    private Source source;
    private String author;
    private String category;
    private double sentimentScore;
    
    public NewsArticle() {}
    
    public NewsArticle(String title, String description, String content, 
                      String url, String urlToImage, LocalDateTime publishedAt, 
                      Source source, String author) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.url = url;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;
        this.source = source;
        this.author = author;
        this.sentimentScore = 0.0;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUrlToImage() {
        return urlToImage;
    }
    
    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    @JsonProperty("publishedAt")
    public void setPublishedAt(String publishedAt) {
        if (publishedAt != null && !publishedAt.isEmpty()) {
            try {
                this.publishedAt = LocalDateTime.parse(publishedAt, 
                    DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                this.publishedAt = LocalDateTime.now();
            }
        }
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public Source getSource() {
        return source;
    }
    
    public void setSource(Source source) {
        this.source = source;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public double getSentimentScore() {
        return sentimentScore;
    }
    
    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }
    
    /**
     * Get a summary of the article content
     */
    public String getSummary() {
        if (description != null && !description.isEmpty()) {
            return description.length() > 200 ? 
                description.substring(0, 200) + "..." : description;
        }
        if (content != null && !content.isEmpty()) {
            return content.length() > 200 ? 
                content.substring(0, 200) + "..." : content;
        }
        return "No summary available";
    }
    
    /**
     * Get sentiment as text with emojis
     */
    public String getSentimentText() {
        if (sentimentScore > 0.05) return "😀 Positive";
        if (sentimentScore < -0.05) return "😞 Negative";
        return "😐 Neutral";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsArticle that = (NewsArticle) o;
        return Objects.equals(url, that.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
    
    @Override
    public String toString() {
        return "NewsArticle{" +
                "title='" + title + '\'' +
                ", source=" + (source != null ? source.getName() : "Unknown") +
                ", publishedAt=" + publishedAt +
                ", sentiment=" + getSentimentText() +
                '}';
    }
}