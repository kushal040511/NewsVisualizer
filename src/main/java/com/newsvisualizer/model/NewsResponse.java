package com.newsvisualizer.model;

import java.util.List;

/**
 * Represents the response from a news API
 */
public class NewsResponse {
    private String status;
    private int totalResults;
    private List<NewsArticle> articles;
    private String code;
    private String message;
    
    public NewsResponse() {}
    
    public NewsResponse(String status, int totalResults, List<NewsArticle> articles) {
        this.status = status;
        this.totalResults = totalResults;
        this.articles = articles;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getTotalResults() {
        return totalResults;
    }
    
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
    
    public List<NewsArticle> getArticles() {
        return articles;
    }
    
    public void setArticles(List<NewsArticle> articles) {
        this.articles = articles;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Check if the response indicates success
     */
    public boolean isSuccess() {
        return "ok".equals(status);
    }
    
    /**
     * Check if the response has articles
     */
    public boolean hasArticles() {
        return articles != null && !articles.isEmpty();
    }
    
    @Override
    public String toString() {
        return "NewsResponse{" +
                "status='" + status + '\'' +
                ", totalResults=" + totalResults +
                ", articlesCount=" + (articles != null ? articles.size() : 0) +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}