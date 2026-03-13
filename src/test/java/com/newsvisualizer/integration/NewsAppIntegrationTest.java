package com.newsvisualizer.integration;

import com.newsvisualizer.adapter.NewsItemAdapter;
import com.newsvisualizer.model.NewsArticle;
import com.newsvisualizer.service.NewsAppService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class to verify NewsApp integration with NewsVisualizer
 */
public class NewsAppIntegrationTest {
    
    private NewsAppService newsAppService;
    
    @BeforeEach
    void setUp() {
        newsAppService = new NewsAppService();
    }
    
    @Test
    void testNewsAppServiceCreation() {
        assertNotNull(newsAppService, "NewsAppService should be created successfully");
    }
    
    @Test
    void testFetchNewsAppStyle() {
        List<NewsArticle> articles = newsAppService.fetchNewsAppStyle();
        
        assertNotNull(articles, "Articles list should not be null");
        assertFalse(articles.isEmpty(), "Articles list should contain some articles");
        
        // Verify that articles have required fields
        for (NewsArticle article : articles) {
            assertNotNull(article.getTitle(), "Article should have a title");
            assertNotNull(article.getUrl(), "Article should have a URL");
            assertNotNull(article.getSource(), "Article should have a source");
        }
    }
    
    @Test
    void testNewsItemAdapter() {
        String title = "Test News Article";
        String summary = "This is a test news article summary";
        String url = "https://example.com/news/1";
        String source = "Test Source";
        
        NewsArticle article = NewsItemAdapter.createFromNewsApp(title, summary, url, source);
        
        assertNotNull(article, "Article should be created");
        assertEquals(title, article.getTitle(), "Title should match");
        assertEquals(summary, article.getDescription(), "Description should match");
        assertEquals(url, article.getUrl(), "URL should match");
        assertEquals(source, article.getSource().getName(), "Source name should match");
    }
    
    @Test
    void testValidation() {
        assertTrue(NewsItemAdapter.isValidNewsItem("Title", "Summary", "http://example.com", "Source"),
                  "Valid news item should pass validation");
        
        assertFalse(NewsItemAdapter.isValidNewsItem("", "Summary", "http://example.com", "Source"),
                   "News item with empty title should fail validation");
        
        assertFalse(NewsItemAdapter.isValidNewsItem("Title", "Summary", "", "Source"),
                   "News item with empty URL should fail validation");
    }
    
    @Test
    void testSearchNews() {
        List<NewsArticle> allArticles = newsAppService.fetchNewsAppStyle();
        
        if (!allArticles.isEmpty()) {
            // Test search with a keyword that should match
            String firstTitle = allArticles.get(0).getTitle();
            String searchKeyword = firstTitle.split(" ")[0]; // Use first word
            
            List<NewsArticle> searchResults = newsAppService.searchNews(searchKeyword);
            
            assertNotNull(searchResults, "Search results should not be null");
            // Should find at least one result that contains the keyword
            assertTrue(searchResults.stream().anyMatch(article -> 
                article.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())),
                "Search should find articles containing the keyword");
        }
    }
    
    @Test
    void testGetAvailableFeeds() {
        var feeds = newsAppService.getAvailableFeeds();
        
        assertNotNull(feeds, "Available feeds should not be null");
        assertFalse(feeds.isEmpty(), "Should have some RSS feeds configured");
    }
}