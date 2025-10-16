package com.newsvisualizer.test;

import com.newsvisualizer.utils.ArticleSummarizer;

public class SummaryTest {
    public static void main(String[] args) {
        System.out.println("Testing Article Summarizer with Keyword Highlighting...\n");
        
        // Test with a Times of India URL to show enhanced extraction and highlighting
        String testUrl = "https://timesofindia.indiatimes.com/topic/2001-Parliament-attack";
        
        ArticleSummarizer.ArticleSummary summary = ArticleSummarizer.summarizeFromUrl(testUrl);
        
        System.out.println("Title: " + summary.getTitle());
        System.out.println("Word Count: " + summary.getWordCount());
        System.out.println("Key Points: " + summary.getKeyPoints().size());
        System.out.println("Keywords: " + summary.getKeywords().size());
        
        System.out.println("\nSummary (with HTML keyword highlighting):");
        System.out.println("=" + "=".repeat(60));
        System.out.println(summary.getSummary());
        System.out.println("=" + "=".repeat(60));
        
        System.out.println("\nKey Points:");
        for (String point : summary.getKeyPoints()) {
            System.out.println("• " + point);
        }
        
        System.out.println("\nTop Keywords:");
        summary.getKeywords().forEach((keyword, frequency) -> 
            System.out.println("- " + keyword + " (" + frequency + ")"));
    }
}