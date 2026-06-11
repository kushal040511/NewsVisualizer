package com.newsvisualizer.utils;

import com.newsvisualizer.model.NewsArticle;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Heuristic editorial insight engine for turning fetched coverage into
 * newsroom-style briefings and topic clusters.
 */
public final class NewsInsightEngine {
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "the", "and", "for", "with", "that", "this", "from", "into", "over", "about", "after",
        "before", "under", "between", "latest", "breaking", "news", "report", "reports", "says",
        "say", "amid", "into", "across", "their", "there", "where", "which", "while", "will",
        "would", "could", "should", "have", "has", "had", "been", "being", "were", "was", "are",
        "our", "your", "its", "his", "her", "them", "they", "also", "more", "than", "just",
        "into", "out", "new", "now", "today", "week", "month", "year", "years", "after", "during",
        "global", "market", "update", "live", "watch", "explained"
    ));

    private static final Set<String> URGENT_TERMS = Set.of(
        "attack", "war", "crisis", "emergency", "storm", "quake", "earthquake", "explosion",
        "outage", "strike", "protest", "raid", "alert", "threat", "sanction", "collapse",
        "inflation", "rates", "tariff", "ceasefire", "evacuation", "wildfire"
    );

    private NewsInsightEngine() {
    }

    public static EditorialBrief buildEditorialBrief(List<NewsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return new EditorialBrief(
                "Newsroom Briefing",
                "No articles are loaded yet. Fetch a news feed first to generate an editorial briefing.",
                List.of(),
                List.of(),
                "Unavailable",
                0.0,
                "No lead story available",
                0
            );
        }

        List<TopicCluster> clusters = clusterStories(articles, 4);
        List<DuplicateStory> duplicateStories = detectDuplicateStories(articles, 3);
        SourceBalanceReport balanceReport = buildSourceBalanceReport(articles, 6);
        List<StoryTimeline> timelines = buildStoryTimelines(articles, 1, 4);
        NewsArticle leadStory = pickLeadStory(articles);
        int recentCount = NewsAnalyzer.getRecentArticles(articles, 6).size();
        double diversityScore = calculateSourceDiversityScore(articles);
        String urgency = detectUrgencyLevel(articles, recentCount);

        List<String> topThemes = clusters.stream()
            .map(cluster -> toTitleCase(cluster.label()) + " (" + cluster.articleCount() + " articles)")
            .toList();

        List<String> watchList = buildWatchList(articles, clusters, duplicateStories, balanceReport, timelines, recentCount, diversityScore);
        String leadHeadline = leadStory.getTitle() != null ? leadStory.getTitle() : "Lead story unavailable";

        String summary = buildSummaryBody(
            articles,
            clusters,
            duplicateStories,
            balanceReport,
            timelines,
            leadHeadline,
            urgency,
            diversityScore,
            recentCount,
            watchList
        );

        return new EditorialBrief(
            "Newsroom Briefing",
            summary,
            topThemes,
            watchList,
            urgency,
            diversityScore,
            leadHeadline,
            recentCount
        );
    }

    public static List<TopicCluster> clusterStories(List<NewsArticle> articles, int limit) {
        if (articles == null || articles.isEmpty()) {
            return List.of();
        }

        return buildClusteredArticleMap(articles).entrySet().stream()
            .map(entry -> new TopicCluster(
                entry.getKey(),
                entry.getValue().size(),
                entry.getValue().stream()
                    .map(article -> article.getTitle() == null ? "Untitled" : article.getTitle())
                    .limit(3)
                    .toList(),
                entry.getValue().stream()
                    .map(article -> article.getSource() != null ? article.getSource().getName() : "Unknown")
                    .distinct()
                    .limit(3)
                    .toList()
            ))
            .sorted(Comparator.comparingInt(TopicCluster::articleCount).reversed())
            .limit(limit)
            .toList();
    }

    public static List<DuplicateStory> detectDuplicateStories(List<NewsArticle> articles, int limit) {
        if (articles == null || articles.isEmpty()) {
            return List.of();
        }

        List<NewsArticle> sortedArticles = articles.stream()
            .sorted(Comparator.comparing(NewsArticle::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();

        List<List<NewsArticle>> groups = new ArrayList<>();
        Map<NewsArticle, Set<String>> titleKeywords = new LinkedHashMap<>();
        for (NewsArticle article : sortedArticles) {
            titleKeywords.put(article, extractTitleKeywords(article));
        }

        for (NewsArticle article : sortedArticles) {
            Set<String> articleTerms = titleKeywords.get(article);
            int bestIndex = -1;
            double bestSimilarity = 0.0;

            for (int i = 0; i < groups.size(); i++) {
                List<NewsArticle> group = groups.get(i);
                double similarity = group.stream()
                    .mapToDouble(existing -> duplicateGroupFit(article, existing, articleTerms, titleKeywords.get(existing)))
                    .average()
                    .orElse(0.0);
                if (similarity >= 0.32 && similarity > bestSimilarity) {
                    bestIndex = i;
                    bestSimilarity = similarity;
                }
            }

            if (bestIndex >= 0) {
                groups.get(bestIndex).add(article);
            } else {
                groups.add(new ArrayList<>(List.of(article)));
            }
        }

        return groups.stream()
            .filter(group -> group.size() >= 2)
            .filter(group -> group.stream()
                .map(NewsInsightEngine::sourceName)
                .distinct()
                .count() >= 2)
            .map(group -> {
                List<String> sources = group.stream()
                    .map(NewsInsightEngine::sourceName)
                    .distinct()
                    .toList();
                double duplicateScore = averagePairSimilarity(group, titleKeywords) * 100.0;
                NewsArticle representative = pickLeadStory(group);
                return new DuplicateStory(
                    representative.getTitle() == null ? "Repeated story cluster" : representative.getTitle(),
                    group.size(),
                    sources.size(),
                    duplicateScore,
                    sources,
                    group.stream()
                        .map(article -> article.getTitle() == null ? "Untitled" : article.getTitle())
                        .distinct()
                        .limit(4)
                        .toList()
                );
            })
            .sorted(Comparator.comparingInt(DuplicateStory::articleCount).reversed()
                .thenComparing(Comparator.comparingDouble(DuplicateStory::duplicateScore).reversed()))
            .limit(limit)
            .toList();
    }

    public static double calculateSourceDiversityScore(List<NewsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return 0.0;
        }
        long uniqueSources = articles.stream()
            .map(article -> article.getSource() != null ? article.getSource().getName() : "Unknown")
            .distinct()
            .count();
        return (uniqueSources * 100.0) / articles.size();
    }

    public static List<SourceProfile> buildSourceProfiles(List<NewsArticle> articles, int limit) {
        if (articles == null || articles.isEmpty()) {
            return List.of();
        }

        return articles.stream()
            .collect(Collectors.groupingBy(
                article -> article.getSource() != null ? article.getSource().getName() : "Unknown",
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .entrySet().stream()
            .map(entry -> {
                List<NewsArticle> sourceArticles = entry.getValue();
                NewsArticle latest = sourceArticles.stream()
                    .filter(article -> article.getPublishedAt() != null)
                    .max(Comparator.comparing(NewsArticle::getPublishedAt))
                    .orElse(sourceArticles.get(0));
                double avgSentiment = sourceArticles.stream()
                    .mapToDouble(NewsArticle::getSentimentScore)
                    .average()
                    .orElse(0.0);
                return new SourceProfile(
                    entry.getKey(),
                    sourceArticles.size(),
                    avgSentiment,
                    latest.getTitle() == null ? "Latest headline unavailable" : latest.getTitle(),
                    latest.getPublishedAt()
                );
            })
            .sorted(Comparator.comparingInt(SourceProfile::articleCount).reversed())
            .limit(limit)
            .toList();
    }

    public static SourceBalanceReport buildSourceBalanceReport(List<NewsArticle> articles, int limit) {
        if (articles == null || articles.isEmpty()) {
            return new SourceBalanceReport(0.0, "Unavailable", "None", 0.0, 0.0, List.of());
        }

        Map<String, List<NewsArticle>> grouped = articles.stream()
            .collect(Collectors.groupingBy(
                NewsInsightEngine::sourceName,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        int totalArticles = articles.size();
        double fairShare = grouped.isEmpty() ? 0.0 : 100.0 / grouped.size();
        double entropy = 0.0;
        double minSentiment = Double.POSITIVE_INFINITY;
        double maxSentiment = Double.NEGATIVE_INFINITY;
        String dominantSource = "Unknown";
        double dominantShare = 0.0;

        List<SourceBalanceScore> scores = new ArrayList<>();
        for (Map.Entry<String, List<NewsArticle>> entry : grouped.entrySet()) {
            int count = entry.getValue().size();
            double share = (count * 100.0) / totalArticles;
            double averageSentiment = entry.getValue().stream()
                .mapToDouble(NewsArticle::getSentimentScore)
                .average()
                .orElse(0.0);

            if (share > dominantShare) {
                dominantShare = share;
                dominantSource = entry.getKey();
            }
            minSentiment = Math.min(minSentiment, averageSentiment);
            maxSentiment = Math.max(maxSentiment, averageSentiment);

            double probability = count / (double) totalArticles;
            entropy -= probability * Math.log(probability);

            scores.add(new SourceBalanceScore(
                entry.getKey(),
                count,
                share,
                averageSentiment,
                share - fairShare,
                toneLabel(averageSentiment)
            ));
        }

        double maxEntropy = grouped.size() <= 1 ? 1.0 : Math.log(grouped.size());
        double overallBalanceScore = grouped.size() <= 1 ? 0.0 : (entropy / maxEntropy) * 100.0;
        String coverageRisk = coverageRisk(overallBalanceScore, dominantShare, grouped.size());

        return new SourceBalanceReport(
            overallBalanceScore,
            coverageRisk,
            dominantSource,
            dominantShare,
            maxSentiment - minSentiment,
            scores.stream()
                .sorted(Comparator.comparingInt(SourceBalanceScore::articleCount).reversed())
                .limit(limit)
                .toList()
        );
    }

    public static List<StoryTimeline> buildStoryTimelines(List<NewsArticle> articles, int limit, int maxEvents) {
        if (articles == null || articles.isEmpty()) {
            return List.of();
        }

        return buildClusteredArticleMap(articles).entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 2)
            .filter(entry -> entry.getValue().stream().map(NewsInsightEngine::sourceName).distinct().count() >= 2)
            .map(entry -> buildTimeline(entry.getKey(), entry.getValue(), maxEvents))
            .sorted(Comparator.comparingInt(StoryTimeline::articleCount).reversed())
            .limit(limit)
            .toList();
    }

    public static List<BreakingSignal> buildBreakingSignals(List<NewsArticle> articles, int limit) {
        if (articles == null || articles.isEmpty()) {
            return List.of();
        }

        return articles.stream()
            .sorted(Comparator.comparingDouble(NewsInsightEngine::leadScore).reversed())
            .limit(limit)
            .map(article -> new BreakingSignal(
                article.getTitle() == null ? "Untitled article" : article.getTitle(),
                containsUrgentTerm(articleText(article))
                    ? "Urgent terms detected in the coverage"
                    : "Fresh coverage signal based on recency and tone",
                article.getSource() != null ? article.getSource().getName() : "Unknown",
                article.getPublishedAt(),
                leadScore(article)
            ))
            .toList();
    }

    public static String detectUrgencyLevel(List<NewsArticle> articles, int recentCount) {
        if (articles == null || articles.isEmpty()) {
            return "Unavailable";
        }

        long urgentHits = articles.stream()
            .map(NewsInsightEngine::articleText)
            .filter(text -> containsUrgentTerm(text))
            .count();

        if (recentCount >= Math.max(4, articles.size() / 2) || urgentHits >= 3) {
            return "High";
        }
        if (recentCount >= 2 || urgentHits >= 1) {
            return "Medium";
        }
        return "Low";
    }

    private static String buildSummaryBody(List<NewsArticle> articles,
                                           List<TopicCluster> clusters,
                                           List<DuplicateStory> duplicateStories,
                                           SourceBalanceReport balanceReport,
                                           List<StoryTimeline> timelines,
                                           String leadHeadline,
                                           String urgency,
                                           double diversityScore,
                                           int recentCount,
                                           List<String> watchList) {
        StringBuilder builder = new StringBuilder();
        builder.append("Lead Story\n");
        builder.append(leadHeadline).append("\n\n");

        builder.append("Coverage Pulse\n");
        builder.append("- Total articles: ").append(articles.size()).append('\n');
        builder.append("- Fresh coverage (last 6h): ").append(recentCount).append('\n');
        builder.append("- Source diversity score: ").append(String.format(Locale.US, "%.1f%%", diversityScore)).append('\n');
        builder.append("- Editorial urgency: ").append(urgency).append("\n\n");

        builder.append("Story Clusters\n");
        if (clusters.isEmpty()) {
            builder.append("- No dominant clusters were identified.\n\n");
        } else {
            int index = 1;
            for (TopicCluster cluster : clusters) {
                builder.append(index++)
                    .append(". ")
                    .append(toTitleCase(cluster.label()))
                    .append(" - ")
                    .append(cluster.articleCount())
                    .append(" articles across ")
                    .append(String.join(", ", cluster.sources()))
                    .append('\n');
            }
            builder.append('\n');
        }

        builder.append("Feed Structure\n");
        builder.append("- Duplicate story packs: ").append(duplicateStories.size()).append('\n');
        builder.append("- Source balance score: ")
            .append(String.format(Locale.US, "%.1f/100", balanceReport.overallBalanceScore()))
            .append(" (").append(balanceReport.coverageRisk()).append(")\n");
        if (!timelines.isEmpty()) {
            StoryTimeline leadTimeline = timelines.get(0);
            builder.append("- Lead timeline: ")
                .append(toTitleCase(leadTimeline.label()))
                .append(" across ")
                .append(leadTimeline.sourceCount())
                .append(" sources, ")
                .append(leadTimeline.coverageWindow())
                .append("\n\n");
        } else {
            builder.append("- Lead timeline: Not enough multi-source coverage yet.\n\n");
        }

        builder.append("Editorial Watchlist\n");
        if (watchList.isEmpty()) {
            builder.append("- No elevated watch items were flagged from the current feed.");
        } else {
            for (String item : watchList) {
                builder.append("- ").append(item).append('\n');
            }
        }

        return builder.toString().trim();
    }

    private static List<String> buildWatchList(List<NewsArticle> articles,
                                               List<TopicCluster> clusters,
                                               List<DuplicateStory> duplicateStories,
                                               SourceBalanceReport balanceReport,
                                               List<StoryTimeline> timelines,
                                               int recentCount,
                                               double diversityScore) {
        List<String> watchList = new ArrayList<>();
        if (!clusters.isEmpty()) {
            TopicCluster leadCluster = clusters.get(0);
            watchList.add("Dominant topic is " + toTitleCase(leadCluster.label()) +
                ", appearing in " + leadCluster.articleCount() + " articles.");
        }
        if (recentCount > 0) {
            watchList.add(recentCount + " article(s) landed in the last six hours, suggesting a live story window.");
        }
        if (diversityScore < 30.0) {
            watchList.add("Coverage is concentrated in a narrow source set. Cross-check with additional outlets.");
        }
        if (!duplicateStories.isEmpty()) {
            DuplicateStory leadDuplicate = duplicateStories.get(0);
            watchList.add("Duplicate coverage is heavy around \"" + leadDuplicate.canonicalHeadline()
                + "\" across " + leadDuplicate.sourceCount() + " outlets.");
        }
        if (balanceReport.overallBalanceScore() > 0.0 && !"Balanced".equals(balanceReport.coverageRisk())) {
            watchList.add("Source balance is " + balanceReport.coverageRisk().toLowerCase(Locale.US)
                + " with " + balanceReport.dominantSource() + " driving "
                + String.format(Locale.US, "%.1f%%", balanceReport.dominantShare()) + " of the feed.");
        }
        if (!timelines.isEmpty()) {
            StoryTimeline leadTimeline = timelines.get(0);
            watchList.add("Timeline to watch: " + toTitleCase(leadTimeline.label()) + " is evolving across "
                + leadTimeline.sourceCount() + " sources.");
        }

        articles.stream()
            .sorted(Comparator.comparingDouble((NewsArticle article) -> Math.abs(article.getSentimentScore())).reversed())
            .limit(2)
            .map(article -> {
                String title = article.getTitle() == null ? "Untitled article" : article.getTitle();
                return "High-emotion headline to monitor: " + title;
            })
            .forEach(watchList::add);

        return watchList.stream().distinct().limit(4).toList();
    }

    private static NewsArticle pickLeadStory(List<NewsArticle> articles) {
        return articles.stream()
            .max(Comparator.comparingDouble(NewsInsightEngine::leadScore))
            .orElse(articles.get(0));
    }

    private static StoryTimeline buildTimeline(String label, List<NewsArticle> articles, int maxEvents) {
        List<NewsArticle> ordered = articles.stream()
            .sorted(Comparator.comparing(NewsArticle::getPublishedAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        Set<String> seenSources = new LinkedHashSet<>();
        List<TimelineEvent> events = new ArrayList<>();
        NewsArticle previous = null;
        for (NewsArticle article : ordered) {
            String development = classifyTimelineDevelopment(article, previous, seenSources);
            events.add(new TimelineEvent(
                article.getPublishedAt(),
                sourceName(article),
                article.getTitle() == null ? "Untitled article" : article.getTitle(),
                development
            ));
            seenSources.add(sourceName(article));
            previous = article;
        }

        LocalDateTime first = ordered.stream()
            .map(NewsArticle::getPublishedAt)
            .filter(value -> value != null)
            .findFirst()
            .orElse(null);
        LocalDateTime last = ordered.stream()
            .map(NewsArticle::getPublishedAt)
            .filter(value -> value != null)
            .reduce((ignored, current) -> current)
            .orElse(null);

        return new StoryTimeline(
            label,
            ordered.size(),
            (int) ordered.stream().map(NewsInsightEngine::sourceName).distinct().count(),
            formatCoverageWindow(first, last, ordered.size()),
            events.stream().limit(maxEvents).toList()
        );
    }

    private static double leadScore(NewsArticle article) {
        double score = Math.abs(article.getSentimentScore()) * 30.0;
        if (article.getPublishedAt() != null) {
            long hoursOld = Math.abs(java.time.Duration.between(article.getPublishedAt(), LocalDateTime.now()).toHours());
            score += Math.max(0, 24 - hoursOld);
        }
        if (containsUrgentTerm(articleText(article))) {
            score += 15.0;
        }
        return score;
    }

    private static Map<String, List<NewsArticle>> buildClusteredArticleMap(List<NewsArticle> articles) {
        Map<String, Integer> globalKeywordFrequency = new HashMap<>();
        Map<NewsArticle, List<String>> articleKeywords = new LinkedHashMap<>();

        for (NewsArticle article : articles) {
            List<String> keywords = extractArticleKeywords(article);
            articleKeywords.put(article, keywords);
            for (String keyword : keywords) {
                globalKeywordFrequency.merge(keyword, 1, Integer::sum);
            }
        }

        Map<String, List<NewsArticle>> grouped = new LinkedHashMap<>();
        for (Map.Entry<NewsArticle, List<String>> entry : articleKeywords.entrySet()) {
            String clusterKey = entry.getValue().stream()
                .sorted(Comparator.<String>comparingInt(keyword -> globalKeywordFrequency.getOrDefault(keyword, 0)).reversed())
                .findFirst()
                .orElse("general");
            grouped.computeIfAbsent(clusterKey, ignored -> new ArrayList<>()).add(entry.getKey());
        }

        return grouped.entrySet().stream()
            .sorted(Comparator.<Map.Entry<String, List<NewsArticle>>>comparingInt(entry -> entry.getValue().size()).reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (left, right) -> left,
                LinkedHashMap::new
            ));
    }

    private static List<String> extractArticleKeywords(NewsArticle article) {
        String text = articleText(article);
        return extractKeywords(text, 6);
    }

    private static Set<String> extractTitleKeywords(NewsArticle article) {
        return new LinkedHashSet<>(extractKeywords(
            List.of(article.getTitle(), article.getDescription()).stream()
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" ")),
            8
        ));
    }

    private static List<String> extractKeywords(String text, int limit) {
        if (text.isBlank()) {
            return List.of();
        }
        Set<String> unique = new HashSet<>();
        List<String> ordered = new ArrayList<>();
        for (String token : text.toLowerCase(Locale.US).split("\\W+")) {
            if (token.length() < 4 || STOP_WORDS.contains(token) || token.matches("\\d+")) {
                continue;
            }
            if (unique.add(token)) {
                ordered.add(token);
            }
        }
        return ordered.stream().limit(limit).toList();
    }

    private static boolean containsUrgentTerm(String text) {
        for (String token : text.toLowerCase(Locale.US).split("\\W+")) {
            if (URGENT_TERMS.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String articleText(NewsArticle article) {
        return List.of(
                article.getTitle(),
                article.getDescription(),
                article.getContent()
            ).stream()
            .filter(value -> value != null && !value.isBlank())
            .collect(Collectors.joining(" "));
    }

    private static String sourceName(NewsArticle article) {
        return article.getSource() != null ? article.getSource().getName() : "Unknown";
    }

    private static double similarity(Set<String> left, Set<String> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        Set<String> union = new HashSet<>(left);
        union.addAll(right);
        return union.isEmpty() ? 0.0 : intersection.size() / (double) union.size();
    }

    private static double averagePairSimilarity(List<NewsArticle> group, Map<NewsArticle, Set<String>> titleKeywords) {
        if (group.size() < 2) {
            return 0.0;
        }
        double total = 0.0;
        int comparisons = 0;
        for (int i = 0; i < group.size(); i++) {
            for (int j = i + 1; j < group.size(); j++) {
                total += similarity(titleKeywords.get(group.get(i)), titleKeywords.get(group.get(j)));
                comparisons++;
            }
        }
        return comparisons == 0 ? 0.0 : total / comparisons;
    }

    private static double duplicateGroupFit(NewsArticle leftArticle,
                                            NewsArticle rightArticle,
                                            Set<String> leftTerms,
                                            Set<String> rightTerms) {
        double similarity = similarity(leftTerms, rightTerms);
        int sharedTerms = sharedKeywordCount(leftTerms, rightTerms);
        boolean closeInTime = withinHours(leftArticle, rightArticle, 24);

        if (sharedTerms >= 3) {
            return Math.max(similarity, 0.68);
        }
        if (sharedTerms >= 2 && closeInTime) {
            return Math.max(similarity, 0.48);
        }
        return similarity;
    }

    private static int sharedKeywordCount(Set<String> left, Set<String> right) {
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return intersection.size();
    }

    private static boolean withinHours(NewsArticle left, NewsArticle right, long hours) {
        if (left.getPublishedAt() == null || right.getPublishedAt() == null) {
            return true;
        }
        long distance = Math.abs(Duration.between(left.getPublishedAt(), right.getPublishedAt()).toHours());
        return distance <= hours;
    }

    private static String toneLabel(double averageSentiment) {
        if (averageSentiment >= 0.18) {
            return "Positive tilt";
        }
        if (averageSentiment <= -0.18) {
            return "Negative tilt";
        }
        return "Balanced tone";
    }

    private static String coverageRisk(double overallBalanceScore, double dominantShare, int sourceCount) {
        if (sourceCount <= 1) {
            return "Single-source view";
        }
        if (dominantShare >= 55.0 || overallBalanceScore < 45.0) {
            return "Concentrated";
        }
        if (dominantShare >= 40.0 || overallBalanceScore < 70.0) {
            return "Watch concentration";
        }
        return "Balanced";
    }

    private static String classifyTimelineDevelopment(NewsArticle article, NewsArticle previous, Set<String> seenSources) {
        if (previous == null) {
            return "Initial report";
        }

        String source = sourceName(article);
        if (!seenSources.contains(source)) {
            return "New source joins coverage";
        }
        if (containsUrgentTerm(articleText(article)) && !containsUrgentTerm(articleText(previous))) {
            return "Escalation signal";
        }
        if (article.getPublishedAt() != null && previous.getPublishedAt() != null) {
            long hours = Math.abs(Duration.between(previous.getPublishedAt(), article.getPublishedAt()).toHours());
            if (hours <= 2) {
                return "Rapid follow-up";
            }
        }
        if (Math.abs(article.getSentimentScore() - previous.getSentimentScore()) >= 0.25) {
            return "Tone shift";
        }
        return "Follow-up coverage";
    }

    private static String formatCoverageWindow(LocalDateTime first, LocalDateTime last, int articleCount) {
        if (first == null || last == null) {
            return articleCount + " linked updates";
        }
        long hours = Math.max(1, Math.abs(Duration.between(first, last).toHours()));
        return hours + "h coverage window";
    }

    private static String toTitleCase(String value) {
        if (value == null || value.isBlank()) {
            return "General";
        }
        return value.substring(0, 1).toUpperCase(Locale.US) + value.substring(1).toLowerCase(Locale.US);
    }

    public record TopicCluster(
        String label,
        int articleCount,
        List<String> headlines,
        List<String> sources
    ) {
    }

    public record EditorialBrief(
        String title,
        String summary,
        List<String> topThemes,
        List<String> watchList,
        String urgency,
        double sourceDiversityScore,
        String leadHeadline,
        int recentArticleCount
    ) {
    }

    public record SourceProfile(
        String sourceName,
        int articleCount,
        double averageSentiment,
        String latestHeadline,
        LocalDateTime latestPublishedAt
    ) {
    }

    public record BreakingSignal(
        String title,
        String reason,
        String sourceName,
        LocalDateTime publishedAt,
        double urgencyScore
    ) {
    }

    public record DuplicateStory(
        String canonicalHeadline,
        int articleCount,
        int sourceCount,
        double duplicateScore,
        List<String> sources,
        List<String> headlines
    ) {
    }

    public record SourceBalanceReport(
        double overallBalanceScore,
        String coverageRisk,
        String dominantSource,
        double dominantShare,
        double toneSpread,
        List<SourceBalanceScore> sourceScores
    ) {
    }

    public record SourceBalanceScore(
        String sourceName,
        int articleCount,
        double coverageShare,
        double averageSentiment,
        double balanceDelta,
        String toneLabel
    ) {
    }

    public record StoryTimeline(
        String label,
        int articleCount,
        int sourceCount,
        String coverageWindow,
        List<TimelineEvent> events
    ) {
    }

    public record TimelineEvent(
        LocalDateTime publishedAt,
        String sourceName,
        String headline,
        String development
    ) {
    }
}
