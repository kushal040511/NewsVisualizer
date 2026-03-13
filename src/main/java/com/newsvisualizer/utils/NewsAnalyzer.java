package com.newsvisualizer.utils;

import com.newsvisualizer.model.NewsArticle;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for analyzing news data
 */
public class NewsAnalyzer {
    
    // Enhanced sentiment words for better analysis
    private static final Set<String> POSITIVE_WORDS = new HashSet<>(Arrays.asList(
        // Basic positive words
        "good", "great", "excellent", "amazing", "wonderful", "fantastic", "positive", "best", "better",
        "outstanding", "superb", "brilliant", "awesome", "incredible", "magnificent", "marvelous",
        // Success and achievement
        "success", "successful", "win", "winning", "victory", "victorious", "achievement", "accomplish",
        "breakthrough", "progress", "growth", "improve", "improvement", "advance", "rise", "boost",
        "surge", "gain", "profit", "benefit", "advantage", "opportunity", "record", "high",
        // Emotions and feelings
        "love", "happy", "happiness", "joy", "joyful", "celebrate", "celebration", "triumph",
        "pleased", "delighted", "excited", "thrilled", "optimistic", "hope", "hopeful", "confident",
        "proud", "satisfaction", "smile", "laugh", "peace", "peaceful", "calm",
        // Quality and approval
        "quality", "premium", "top", "leading", "first", "winner", "champion", "hero", "star",
        "approve", "support", "agree", "accept", "welcome", "embrace", "praise", "appreciate",
        "thank", "gratitude", "honor", "respect", "admire", "inspire", "motivate",
        // Health and life
        "health", "healthy", "cure", "heal", "recover", "recovery", "save", "rescue", "help",
        "relief", "comfort", "safe", "safety", "secure", "protect", "strengthen", "strong"
    ));
    
    private static final Set<String> NEGATIVE_WORDS = new HashSet<>(Arrays.asList(
        // Basic negative words
        "bad", "terrible", "awful", "horrible", "worst", "worse", "negative", "poor", "fail",
        "failure", "failed", "failing", "loss", "lose", "losing", "lost", "defeat", "beaten",
        // Crisis and problems
        "crisis", "problem", "problems", "issue", "issues", "trouble", "difficulty", "challenge",
        "concern", "worry", "worried", "fear", "afraid", "scared", "panic", "anxiety", "stress",
        "decline", "decrease", "drop", "fall", "crash", "collapse", "breakdown", "recession",
        // Violence and conflict
        "disaster", "catastrophe", "tragedy", "tragic", "death", "die", "died", "kill", "killed",
        "murder", "shooting", "violence", "violent", "attack", "assault", "bomb", "explosion",
        "war", "warfare", "conflict", "fight", "battle", "terrorism", "terrorist", "threat",
        "threaten", "danger", "dangerous", "risk", "risky", "harm", "damage", "destroy",
        // Emotions and feelings
        "hate", "angry", "anger", "rage", "furious", "mad", "upset", "disappointed", "sad",
        "sadness", "depression", "depressed", "miserable", "unhappy", "cry", "tears", "grief",
        "hurt", "pain", "suffer", "suffering", "agony", "torture", "abuse", "victim",
        // Corruption and wrongdoing
        "corrupt", "corruption", "scandal", "fraud", "lie", "lying", "cheat", "steal", "theft",
        "crime", "criminal", "illegal", "arrest", "prison", "jail", "guilty", "blame", "fault",
        // Rejection and disapproval
        "reject", "denial", "refuse", "oppose", "against", "protest", "criticize", "condemn",
        "dispute", "disagree", "controversy", "controversial", "boycott", "ban", "forbid"
    ));
    
    /**
     * Perform basic sentiment analysis on news articles
     */
    public static void analyzeSentiment(List<NewsArticle> articles) {
        for (NewsArticle article : articles) {
            double sentiment = calculateSentiment(article);
            article.setSentimentScore(sentiment);
        }
    }
    
    /**
     * Calculate sentiment score for a single article
     */
    private static double calculateSentiment(NewsArticle article) {
        String text = combineTextContent(article).toLowerCase();
        String[] words = text.split("\\W+");
        
        int positiveCount = 0;
        int negativeCount = 0;
        
        for (String word : words) {
            if (POSITIVE_WORDS.contains(word)) {
                positiveCount++;
            } else if (NEGATIVE_WORDS.contains(word)) {
                negativeCount++;
            }
        }
        
        int totalWords = words.length;
        if (totalWords == 0) return 0.0;
        
        double positiveRatio = (double) positiveCount / totalWords;
        double negativeRatio = (double) negativeCount / totalWords;
        
        return positiveRatio - negativeRatio;
    }
    
    /**
     * Combine title, description, and content for analysis
     */
    private static String combineTextContent(NewsArticle article) {
        StringBuilder content = new StringBuilder();
        
        if (StringUtils.isNotBlank(article.getTitle())) {
            content.append(article.getTitle()).append(" ");
        }
        if (StringUtils.isNotBlank(article.getDescription())) {
            content.append(article.getDescription()).append(" ");
        }
        if (StringUtils.isNotBlank(article.getContent())) {
            content.append(article.getContent());
        }
        
        return content.toString();
    }
    
    /**
     * Extract keywords from articles
     */
    public static Map<String, Integer> extractKeywords(List<NewsArticle> articles, int topN) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        
        // Common stop words to ignore
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", 
            "with", "by", "is", "are", "was", "were", "be", "been", "being", "have", 
            "has", "had", "do", "does", "did", "will", "would", "could", "should",
            "this", "that", "these", "those", "i", "you", "he", "she", "it", "we", "they"
        ));
        
        for (NewsArticle article : articles) {
            String text = combineTextContent(article).toLowerCase();
            String[] words = text.split("\\W+");
            
            for (String word : words) {
                if (word.length() > 2 && !stopWords.contains(word) && !word.matches("\\d+")) {
                    wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                }
            }
        }
        
        return wordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
    }
    
    /**
     * Group articles by publication date
     */
    public static Map<String, List<NewsArticle>> groupByDate(List<NewsArticle> articles) {
        return articles.stream()
                .filter(article -> article.getPublishedAt() != null)
                .collect(Collectors.groupingBy(
                    article -> article.getPublishedAt().toLocalDate().toString(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
    }
    
    /**
     * Group articles by source
     */
    public static Map<String, List<NewsArticle>> groupBySource(List<NewsArticle> articles) {
        return articles.stream()
                .collect(Collectors.groupingBy(
                    article -> article.getSource() != null ? article.getSource().getName() : "Unknown",
                    Collectors.toList()
                ));
    }
    
    /**
     * Group articles by sentiment
     */
    public static Map<String, List<NewsArticle>> groupBySentiment(List<NewsArticle> articles) {
        return articles.stream()
                .collect(Collectors.groupingBy(
                    NewsArticle::getSentimentText,
                    Collectors.toList()
                ));
    }
    
    /**
     * Get articles published in the last N hours
     */
    public static List<NewsArticle> getRecentArticles(List<NewsArticle> articles, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        
        return articles.stream()
                .filter(article -> article.getPublishedAt() != null && 
                                 article.getPublishedAt().isAfter(cutoff))
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate average sentiment score
     */
    public static double getAverageSentiment(List<NewsArticle> articles) {
        return articles.stream()
                .mapToDouble(NewsArticle::getSentimentScore)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Get sentiment distribution
     */
    public static Map<String, Integer> getSentimentDistribution(List<NewsArticle> articles) {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (NewsArticle article : articles) {
            String sentiment = article.getSentimentText();
            distribution.put(sentiment, distribution.getOrDefault(sentiment, 0) + 1);
        }
        
        return distribution;
    }
    
    /**
     * Get source distribution
     */
    public static Map<String, Integer> getSourceDistribution(List<NewsArticle> articles) {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (NewsArticle article : articles) {
            String source = article.getSource() != null ? article.getSource().getName() : "Unknown";
            distribution.put(source, distribution.getOrDefault(source, 0) + 1);
        }
        
        return distribution.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
    }
    
    // Publication timeline functionality removed
}