package com.newsvisualizer.service;

import com.newsvisualizer.adapter.NewsItemAdapter;
import com.newsvisualizer.model.NewsArticle;
import com.newsvisualizer.model.NewsResponse;
import com.newsvisualizer.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service that provides newsApp-style functionality integrated with NewsVisualizer
 * This recreates the functionality from the legacy newsApp JAR
 */
public class NewsAppService {
    private static final Logger logger = LoggerFactory.getLogger(NewsAppService.class);
    
    // RSS feeds similar to what newsApp might have used
    private static final Map<String, String> RSS_FEEDS = new LinkedHashMap<String, String>() {{
        put("BBC World", "http://feeds.bbci.co.uk/news/world/rss.xml");
        put("CNN", "http://rss.cnn.com/rss/edition.rss");
        put("Reuters", "http://feeds.reuters.com/reuters/topNews");
        put("Associated Press", "https://feeds.apnews.com/rss/apf-topnews");
        put("NPR", "https://feeds.npr.org/1001/rss.xml");
        put("The Guardian", "https://www.theguardian.com/world/rss");
    }};
    
    private final IndianRssService indianRssService;
    
    public NewsAppService() {
        this.indianRssService = new IndianRssService();
    }
    
    /**
     * Fetch news in the style of the original newsApp
     * This provides a simplified interface similar to the original application
     */
    public List<NewsArticle> fetchNewsAppStyle() {
        return fetchNewsAppStyleByCategory("general");
    }
    
    /**
     * Fetch news by category in newsApp style
     */
    public List<NewsArticle> fetchNewsAppStyleByCategory(String category) {
        logger.info("Fetching news in newsApp style for category: {}", category);
        
        List<NewsArticle> allArticles = new ArrayList<>();
        
        try {
            // Use existing IndianRssService to get Indian news by category
            NewsResponse indianNews = indianRssService.getIndianNews(category);
            if (indianNews != null && indianNews.getArticles() != null) {
                allArticles.addAll(indianNews.getArticles());
                logger.info("Added {} Indian news articles for category: {}", 
                           indianNews.getArticles().size(), category);
            }
            
            // Add category-specific simulated articles
            List<NewsArticle> simulatedArticles = createSimulatedNewsItemsByCategory(category);
            allArticles.addAll(simulatedArticles);
            
        } catch (Exception e) {
            logger.error("Error fetching newsApp style news for category: {}", category, e);
        }
        
        // Filter articles by category if not general
        if (!"general".equals(category)) {
            allArticles = filterArticlesByCategory(allArticles, category);
        }
        
        // Sort by title (simple sorting like the original might have done)
        allArticles.sort((a, b) -> {
            if (a.getTitle() == null) return 1;
            if (b.getTitle() == null) return -1;
            return a.getTitle().compareTo(b.getTitle());
        });
        
        // Limit to reasonable number
        if (allArticles.size() > 20) {
            allArticles = allArticles.subList(0, 20);
        }
        
        logger.info("Returning {} articles in newsApp style for category: {}", 
                   allArticles.size(), category);
        return allArticles;
    }
    
    /**
     * Create some simulated news items that might represent what newsApp fetched
     */
    private List<NewsArticle> createSimulatedNewsItems() {
        List<NewsArticle> articles = new ArrayList<>();
        
        // Simulate some general news items
        String[] sampleTitles = {
            "Technology Advances in 2024",
            "Global Economic Updates",
            "Climate Change Conference Results",
            "Space Exploration Milestone",
            "Healthcare Innovation Breakthrough"
        };
        
        String[] sampleSources = {
            "Tech News",
            "Economic Times",
            "Environmental Post",
            "Space Daily",
            "Health Tribune"
        };
        
        String[] sampleSummaries = {
            "Latest technological developments are shaping the future across various industries.",
            "Economic indicators show mixed signals as markets adapt to global changes.",
            "Climate scientists present new findings at the international conference.",
            "New space mission achieves significant milestone in exploration.",
            "Medical researchers announce breakthrough in treatment methods."
        };
        
        for (int i = 0; i < sampleTitles.length; i++) {
            String title = sampleTitles[i];
            String source = sampleSources[i];
            String summary = sampleSummaries[i];
            String url = "https://example.com/news/" + (i + 1);
            
            if (NewsItemAdapter.isValidNewsItem(title, summary, url, source)) {
                NewsArticle article = NewsItemAdapter.createFromNewsApp(title, summary, url, source);
                articles.add(article);
            }
        }
        
        return articles;
    }
    
    /**
     * Create simulated news items based on category
     */
    private List<NewsArticle> createSimulatedNewsItemsByCategory(String category) {
        List<NewsArticle> articles = new ArrayList<>();
        
        switch (category.toLowerCase()) {
            case "sports":
                articles.addAll(createSportsArticles());
                break;
            case "business":
                articles.addAll(createBusinessArticles());
                break;
            case "technology":
                articles.addAll(createTechnologyArticles());
                break;
            case "entertainment":
                articles.addAll(createEntertainmentArticles());
                break;
            case "health":
                articles.addAll(createHealthArticles());
                break;
            case "science":
                articles.addAll(createScienceArticles());
                break;
            default:
                articles.addAll(createSimulatedNewsItems());
                break;
        }
        
        return articles;
    }
    
    /**
     * Filter articles by category based on title and content
     */
    private List<NewsArticle> filterArticlesByCategory(List<NewsArticle> articles, String category) {
        List<NewsArticle> filtered = new ArrayList<>();
        String[] keywords = getCategoryKeywords(category);
        
        for (NewsArticle article : articles) {
            if (articleMatchesCategory(article, keywords)) {
                filtered.add(article);
            }
        }
        
        // If no articles match the specific category, return some general ones
        if (filtered.isEmpty() && !articles.isEmpty()) {
            return articles.subList(0, Math.min(3, articles.size()));
        }
        
        return filtered;
    }
    
    private boolean articleMatchesCategory(NewsArticle article, String[] keywords) {
        String text = (article.getTitle() + " " + article.getDescription() + " " + 
                      (article.getSource() != null ? article.getSource().getName() : "")).toLowerCase();
        
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    private String[] getCategoryKeywords(String category) {
        switch (category.toLowerCase()) {
            case "sports":
                return new String[]{"sports", "cricket", "football", "tennis", "basketball", "match", "tournament", "player", "team", "score"};
            case "business":
                return new String[]{"business", "economy", "market", "financial", "company", "stock", "investment", "trade", "profit", "corporate"};
            case "technology":
                return new String[]{"technology", "tech", "AI", "artificial intelligence", "computer", "software", "digital", "innovation", "startup", "app"};
            case "entertainment":
                return new String[]{"entertainment", "movie", "film", "actor", "actress", "music", "celebrity", "bollywood", "hollywood", "show"};
            case "health":
                return new String[]{"health", "medical", "hospital", "disease", "treatment", "medicine", "doctor", "patient", "wellness", "fitness"};
            case "science":
                return new String[]{"science", "research", "study", "discovery", "experiment", "scientist", "laboratory", "innovation", "breakthrough", "space"};
            default:
                return new String[]{};
        }
    }
    
    /**
     * Simulate the RSS fetching that newsApp would have done
     */
    public InputStream fetchFeed(String feedUrl) throws Exception {
        logger.debug("Fetching RSS feed: {}", feedUrl);
        
        URL url = new URL(feedUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "NewsApp-NewsVisualizer/1.0");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            return connection.getInputStream();
        } else {
            throw new Exception("HTTP error code: " + responseCode);
        }
    }
    
    /**
     * Get available RSS feeds
     */
    public Map<String, String> getAvailableFeeds() {
        return new HashMap<>(RSS_FEEDS);
    }
    
    /**
     * Search news by keyword (simple implementation)
     */
    public List<NewsArticle> searchNews(String keyword) {
        logger.info("Searching news for keyword: {}", keyword);
        
        List<NewsArticle> allArticles = fetchNewsAppStyle();
        List<NewsArticle> matchingArticles = new ArrayList<>();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return allArticles;
        }
        
        String searchTerm = keyword.toLowerCase().trim();
        
        for (NewsArticle article : allArticles) {
            boolean matches = false;
            
            // Search in title
            if (article.getTitle() != null && 
                article.getTitle().toLowerCase().contains(searchTerm)) {
                matches = true;
            }
            
            // Search in description
            if (!matches && article.getDescription() != null && 
                article.getDescription().toLowerCase().contains(searchTerm)) {
                matches = true;
            }
            
            // Search in source name
            if (!matches && article.getSource() != null && 
                article.getSource().getName() != null &&
                article.getSource().getName().toLowerCase().contains(searchTerm)) {
                matches = true;
            }
            
            if (matches) {
                matchingArticles.add(article);
            }
        }
        
        logger.info("Found {} articles matching '{}'", matchingArticles.size(), keyword);
        return matchingArticles;
    }
    
    // Category-specific article creators
    private List<NewsArticle> createSportsArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        String[] titles = {
            "Cricket World Cup Finals Set for This Weekend",
            "Tennis Championship Delivers Exciting Matches",
            "Football Transfer News: Major Signings Announced",
            "Olympic Training Camps Begin Preparations",
            "Basketball League Playoffs Heat Up"
        };
        String[] summaries = {
            "Cricket fans worldwide anticipate the championship match between top teams.",
            "Professional tennis players compete in thrilling tournament matches.",
            "Major football clubs announce significant player acquisitions for the season.",
            "Athletes begin intensive training programs for upcoming Olympic games.",
            "Professional basketball teams compete in high-stakes playoff matches."
        };
        return createArticlesFromData(titles, summaries, "Sports News");
    }
    
    private List<NewsArticle> createBusinessArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        String[] titles = {
            "Stock Markets Show Positive Growth This Quarter",
            "Tech Startups Receive Record Investment Funding",
            "Global Trade Relations Impact Local Markets",
            "Corporate Earnings Reports Exceed Expectations",
            "Economic Indicators Point to Steady Recovery"
        };
        String[] summaries = {
            "Financial markets demonstrate strong performance across multiple sectors.",
            "Technology companies attract significant venture capital investments.",
            "International trade policies influence regional business developments.",
            "Major corporations report better than anticipated quarterly results.",
            "Key economic metrics suggest sustained growth and stability."
        };
        return createArticlesFromData(titles, summaries, "Business Times");
    }
    
    private List<NewsArticle> createTechnologyArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        String[] titles = {
            "Artificial Intelligence Breakthrough in Healthcare",
            "New Smartphone Technology Revolutionizes Photography",
            "Cybersecurity Advances Protect Digital Infrastructure",
            "Software Innovation Improves Remote Work Efficiency",
            "Tech Giants Announce Sustainable Computing Initiatives"
        };
        String[] summaries = {
            "AI systems demonstrate significant improvements in medical diagnosis accuracy.",
            "Mobile device cameras achieve professional-quality image capture capabilities.",
            "Advanced security systems provide enhanced protection against cyber threats.",
            "New applications streamline collaboration for distributed work teams.",
            "Technology companies commit to environmentally friendly computing solutions."
        };
        return createArticlesFromData(titles, summaries, "Tech Today");
    }
    
    private List<NewsArticle> createEntertainmentArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        String[] titles = {
            "Bollywood Film Festival Celebrates Cinema Excellence",
            "Music Awards Ceremony Honors Outstanding Artists",
            "Television Series Receives Critical Acclaim",
            "Celebrity Charity Event Raises Funds for Education",
            "Film Industry Adopts New Production Technologies"
        };
        String[] summaries = {
            "Annual film festival showcases exceptional talent in Indian cinema.",
            "Music industry recognizes achievements of performers and composers.",
            "New television drama series earns praise from critics and audiences.",
            "Entertainment personalities support educational initiatives through fundraising.",
            "Movie production companies implement innovative filming techniques."
        };
        return createArticlesFromData(titles, summaries, "Entertainment Weekly");
    }
    
    private List<NewsArticle> createHealthArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        String[] titles = {
            "Medical Research Reveals New Treatment Options",
            "Health Campaign Promotes Preventive Care Awareness",
            "Hospital System Implements Advanced Patient Care",
            "Nutrition Study Shows Benefits of Traditional Diet",
            "Mental Health Support Programs Expand Nationwide"
        };
        String[] summaries = {
            "Scientific studies identify promising therapies for chronic conditions.",
            "Public health initiatives encourage regular medical checkups and wellness.",
            "Healthcare facilities adopt cutting-edge medical equipment and procedures.",
            "Research demonstrates positive health impacts of regional food traditions.",
            "Mental wellness resources become more accessible across communities."
        };
        return createArticlesFromData(titles, summaries, "Health Tribune");
    }
    
    private List<NewsArticle> createScienceArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        String[] titles = {
            "Space Mission Achieves Remarkable Scientific Discovery",
            "Environmental Research Provides Climate Insights",
            "Laboratory Breakthrough Advances Materials Science",
            "Renewable Energy Technology Shows Promise",
            "Archaeological Find Reveals Ancient Civilization"
        };
        String[] summaries = {
            "Space exploration mission uncovers significant findings about planetary systems.",
            "Climate scientists present important data about environmental changes.",
            "Materials research leads to development of innovative manufacturing processes.",
            "Clean energy solutions demonstrate improved efficiency and sustainability.",
            "Archaeological team discovers artifacts providing historical insights."
        };
        return createArticlesFromData(titles, summaries, "Science Daily");
    }
    
    private List<NewsArticle> createArticlesFromData(String[] titles, String[] summaries, String sourceName) {
        List<NewsArticle> articles = new ArrayList<>();
        
        for (int i = 0; i < titles.length && i < summaries.length; i++) {
            String url = "https://example.com/news/" + sourceName.toLowerCase().replace(" ", "-") + "/" + (i + 1);
            
            if (NewsItemAdapter.isValidNewsItem(titles[i], summaries[i], url, sourceName)) {
                NewsArticle article = NewsItemAdapter.createFromNewsApp(titles[i], summaries[i], url, sourceName);
                articles.add(article);
            }
        }
        
        return articles;
    }
    
    /**
     * Close resources
     */
    public void close() {
        try {
            if (indianRssService != null) {
                indianRssService.close();
            }
        } catch (Exception e) {
            logger.error("Error closing NewsApp services", e);
        }
    }
}
