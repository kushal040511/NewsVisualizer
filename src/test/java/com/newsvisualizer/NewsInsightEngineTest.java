package com.newsvisualizer;

import com.newsvisualizer.model.NewsArticle;
import com.newsvisualizer.model.Source;
import com.newsvisualizer.utils.NewsAnalyzer;
import com.newsvisualizer.utils.NewsInsightEngine;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewsInsightEngineTest {

    @Test
    void buildsClustersAndBriefingFromFetchedArticles() {
        Source sourceA = new Source("a", "Source A");
        Source sourceB = new Source("b", "Source B");

        NewsArticle articleOne = new NewsArticle(
            "Central bank raises rates amid inflation concerns",
            "Policymakers tightened policy again as inflation remained elevated.",
            "The central bank raised rates after another hot inflation reading.",
            "https://example.com/1",
            null,
            LocalDateTime.now().minusHours(1),
            sourceA,
            "Reporter A"
        );

        NewsArticle articleTwo = new NewsArticle(
            "Markets react as inflation keeps pressure on borrowing costs",
            "Traders are watching the inflation path and policy outlook closely.",
            "Markets and banks are adjusting quickly after the inflation update.",
            "https://example.com/2",
            null,
            LocalDateTime.now().minusHours(2),
            sourceB,
            "Reporter B"
        );

        NewsArticle articleThree = new NewsArticle(
            "Technology companies unveil major AI platform updates",
            "A wave of new AI tools was launched at the annual tech conference.",
            "Executives said the new artificial intelligence platform will boost productivity.",
            "https://example.com/3",
            null,
            LocalDateTime.now().minusHours(3),
            sourceA,
            "Reporter C"
        );

        List<NewsArticle> articles = List.of(articleOne, articleTwo, articleThree);
        NewsAnalyzer.analyzeSentiment(articles);

        List<NewsInsightEngine.TopicCluster> clusters = NewsInsightEngine.clusterStories(articles, 3);
        NewsInsightEngine.EditorialBrief brief = NewsInsightEngine.buildEditorialBrief(articles);

        assertFalse(clusters.isEmpty(), "Clusters should be created for the feed");
        assertFalse(brief.summary().isBlank(), "Briefing summary should not be blank");
        assertFalse(brief.topThemes().isEmpty(), "Briefing should contain top themes");
        assertTrue(brief.sourceDiversityScore() > 0.0, "Source diversity should be calculated");
    }

    @Test
    void detectsDuplicatesBuildsBalanceScoresAndCreatesTimelines() {
        Source sourceA = new Source("a", "Source A");
        Source sourceB = new Source("b", "Source B");
        Source sourceC = new Source("c", "Source C");

        NewsArticle articleOne = new NewsArticle(
            "Oil prices jump after refinery outage rattles markets",
            "A major refinery outage is pushing energy traders to reprice supply risk.",
            "Oil prices climbed sharply after a refinery outage disrupted supply expectations.",
            "https://example.com/oil-1",
            null,
            LocalDateTime.now().minusHours(5),
            sourceA,
            "Reporter A"
        );

        NewsArticle articleTwo = new NewsArticle(
            "Refinery outage sends oil prices higher as traders watch supply",
            "Energy markets are reacting to the latest refinery outage and possible shortages.",
            "Supply concerns intensified after the refinery outage sent oil futures higher.",
            "https://example.com/oil-2",
            null,
            LocalDateTime.now().minusHours(3),
            sourceB,
            "Reporter B"
        );

        NewsArticle articleThree = new NewsArticle(
            "Oil traders brace for more volatility after refinery disruption",
            "Another outlet is tracking the same refinery disruption and its market effect.",
            "Traders said the refinery disruption could keep oil prices elevated for days.",
            "https://example.com/oil-3",
            null,
            LocalDateTime.now().minusHours(1),
            sourceC,
            "Reporter C"
        );

        NewsArticle articleFour = new NewsArticle(
            "AI chipmakers unveil faster data center hardware",
            "Technology groups launched new AI hardware at a major conference.",
            "Chipmakers said the latest hardware is designed for growing AI demand.",
            "https://example.com/ai-1",
            null,
            LocalDateTime.now().minusHours(2),
            sourceA,
            "Reporter D"
        );

        List<NewsArticle> articles = List.of(articleOne, articleTwo, articleThree, articleFour);
        NewsAnalyzer.analyzeSentiment(articles);

        List<NewsInsightEngine.DuplicateStory> duplicateStories = NewsInsightEngine.detectDuplicateStories(articles, 5);
        NewsInsightEngine.SourceBalanceReport balanceReport = NewsInsightEngine.buildSourceBalanceReport(articles, 5);
        List<NewsInsightEngine.StoryTimeline> storyTimelines = NewsInsightEngine.buildStoryTimelines(articles, 5, 4);

        assertFalse(duplicateStories.isEmpty(), "Duplicate story packs should be detected across sources");
        assertEquals(3, duplicateStories.get(0).sourceCount(), "The repeated oil storyline should span three sources");
        assertTrue(balanceReport.overallBalanceScore() > 0.0, "Source balance score should be calculated");
        assertFalse(balanceReport.coverageRisk().isBlank(), "Coverage risk should be labeled");
        assertFalse(storyTimelines.isEmpty(), "A multi-source timeline should be generated for the repeated story");
        assertTrue(storyTimelines.get(0).events().size() >= 2, "Timeline should contain multiple chronological events");
    }
}
