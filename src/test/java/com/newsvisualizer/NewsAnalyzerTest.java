package com.newsvisualizer;

import com.newsvisualizer.model.NewsArticle;
import com.newsvisualizer.model.Source;
import com.newsvisualizer.utils.NewsAnalyzer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for NewsAnalyzer functionality
 */
public class NewsAnalyzerTest {
    
    @Test
    public void testSentimentAnalysis() {
        // Create test articles
        Source testSource = new Source("test", "Test Source");
        
        NewsArticle positiveArticle = new NewsArticle(
            "Great success in technology breakthrough",
            "Amazing progress made in new technology development",
            "This is wonderful news about excellent progress",
            "http://test.com/1",
            null,
            LocalDateTime.now(),
            testSource,
            "Test Author"
        );
        
        NewsArticle negativeArticle = new NewsArticle(
            "Terrible disaster strikes city",
            "Bad news about awful tragedy and crisis",
            "This is horrible news about terrible problems",
            "http://test.com/2", 
            null,
            LocalDateTime.now(),
            testSource,
            "Test Author"
        );
        
        List<NewsArticle> articles = Arrays.asList(positiveArticle, negativeArticle);
        
        // Perform sentiment analysis
        NewsAnalyzer.analyzeSentiment(articles);
        
        // Check results
        assertTrue(positiveArticle.getSentimentScore() > 0, "Positive article should have positive sentiment score");
        assertTrue(negativeArticle.getSentimentScore() < 0, "Negative article should have negative sentiment score");
        
        assertEquals("😀 Positive", positiveArticle.getSentimentText());
        assertEquals("😞 Negative", negativeArticle.getSentimentText());
    }
    
    @Test
    public void testKeywordExtraction() {
        Source testSource = new Source("test", "Test Source");
        
        NewsArticle article1 = new NewsArticle(
            "Technology news about artificial intelligence",
            "AI technology is advancing rapidly in machine learning",
            "Artificial intelligence and machine learning technology news",
            "http://test.com/1",
            null,
            LocalDateTime.now(),
            testSource,
            "Test Author"
        );
        
        NewsArticle article2 = new NewsArticle(
            "More technology news about AI development", 
            "Technology companies developing artificial intelligence solutions",
            "AI technology development in various companies",
            "http://test.com/2",
            null,
            LocalDateTime.now(),
            testSource,
            "Test Author"
        );
        
        List<NewsArticle> articles = Arrays.asList(article1, article2);
        
        // Extract keywords
        Map<String, Integer> keywords = NewsAnalyzer.extractKeywords(articles, 5);
        
        // Check results
        assertFalse(keywords.isEmpty(), "Keywords should be extracted");
        assertTrue(keywords.containsKey("technology"), "Should contain 'technology' keyword");
        assertTrue(keywords.containsKey("artificial"), "Should contain 'artificial' keyword");
        
        // Check frequency
        assertTrue(keywords.get("technology") >= 2, "Technology should appear at least 2 times");
    }
    
    @Test
    public void testSentimentDistribution() {
        Source testSource = new Source("test", "Test Source");
        
        // Create articles with different sentiments
        NewsArticle positive = createTestArticle("Great excellent amazing", testSource);
        NewsArticle negative = createTestArticle("Terrible horrible awful", testSource);
        NewsArticle neutral = createTestArticle("Normal regular standard", testSource);
        
        List<NewsArticle> articles = Arrays.asList(positive, negative, neutral);
        
        // Analyze sentiment first
        NewsAnalyzer.analyzeSentiment(articles);
        
        // Get distribution
        Map<String, Integer> distribution = NewsAnalyzer.getSentimentDistribution(articles);
        
        // Check results
        assertEquals(3, distribution.size(), "Should have 3 sentiment categories");
        assertTrue(distribution.containsKey("😀 Positive"));
        assertTrue(distribution.containsKey("😞 Negative")); 
        assertTrue(distribution.containsKey("😐 Neutral"));
    }
    
    @Test
    public void testSourceDistribution() {
        Source source1 = new Source("source1", "Source One");
        Source source2 = new Source("source2", "Source Two");
        
        NewsArticle article1 = createTestArticle("Test content", source1);
        NewsArticle article2 = createTestArticle("More content", source1);
        NewsArticle article3 = createTestArticle("Different content", source2);
        
        List<NewsArticle> articles = Arrays.asList(article1, article2, article3);
        
        Map<String, Integer> distribution = NewsAnalyzer.getSourceDistribution(articles);
        
        assertEquals(2, distribution.size(), "Should have 2 sources");
        assertEquals(2, distribution.get("Source One").intValue(), "Source One should have 2 articles");
        assertEquals(1, distribution.get("Source Two").intValue(), "Source Two should have 1 article");
    }
    
    private NewsArticle createTestArticle(String content, Source source) {
        return new NewsArticle(
            "Test Title: " + content,
            "Test Description: " + content,
            "Test Content: " + content,
            "http://test.com/" + Math.random(),
            null,
            LocalDateTime.now(),
            source,
            "Test Author"
        );
    }
}