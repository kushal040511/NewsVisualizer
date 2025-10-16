package com.newsvisualizer.model;

import java.time.LocalDateTime;

/**
 * Model to track user's search history and analysis results
 */
public class SearchHistory {
    private Long id;
    private Long userId;
    private String searchType; // "headlines", "keywords", "url_summary"
    private String searchQuery;
    private String country;
    private String category;
    private int articlesFound;
    private String analysisResults; // JSON string of analysis data
    private LocalDateTime searchedAt;
    
    // Default constructor
    public SearchHistory() {
        this.searchedAt = LocalDateTime.now();
    }
    
    // Constructor for news search
    public SearchHistory(Long userId, String searchType, String searchQuery, String country, String category, int articlesFound) {
        this();
        this.userId = userId;
        this.searchType = searchType;
        this.searchQuery = searchQuery;
        this.country = country;
        this.category = category;
        this.articlesFound = articlesFound;
    }
    
    // Constructor for URL summarization
    public SearchHistory(Long userId, String url, String summaryResult) {
        this();
        this.userId = userId;
        this.searchType = "url_summary";
        this.searchQuery = url;
        this.analysisResults = summaryResult;
        this.articlesFound = 1;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getSearchType() {
        return searchType;
    }
    
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getArticlesFound() {
        return articlesFound;
    }
    
    public void setArticlesFound(int articlesFound) {
        this.articlesFound = articlesFound;
    }
    
    public String getAnalysisResults() {
        return analysisResults;
    }
    
    public void setAnalysisResults(String analysisResults) {
        this.analysisResults = analysisResults;
    }
    
    public LocalDateTime getSearchedAt() {
        return searchedAt;
    }
    
    public void setSearchedAt(LocalDateTime searchedAt) {
        this.searchedAt = searchedAt;
    }
    
    public String getDisplayTitle() {
        switch (searchType) {
            case "headlines":
                StringBuilder title = new StringBuilder("Headlines");
                if (country != null) {
                    title.append(" from ").append(country.toUpperCase());
                }
                if (category != null && !category.equals("general")) {
                    title.append(" - ").append(category.substring(0, 1).toUpperCase()).append(category.substring(1));
                }
                return title.toString();
            case "keywords":
                return "Search: \"" + (searchQuery != null ? searchQuery : "General") + "\"";
            case "url_summary":
                return "Article Summary";
            default:
                return "Unknown Search";
        }
    }
    
    public String getSearchDescription() {
        switch (searchType) {
            case "headlines":
            case "keywords":
                return articlesFound + " articles found";
            case "url_summary":
                return "Article summarized";
            default:
                return "";
        }
    }
    
    @Override
    public String toString() {
        return "SearchHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", searchType='" + searchType + '\'' +
                ", searchQuery='" + searchQuery + '\'' +
                ", country='" + country + '\'' +
                ", category='" + category + '\'' +
                ", articlesFound=" + articlesFound +
                ", searchedAt=" + searchedAt +
                '}';
    }
}