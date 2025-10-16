package com.newsvisualizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsvisualizer.model.NewsResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for fetching news data from external APIs
 */
public class NewsApiService {
    private static final Logger logger = LoggerFactory.getLogger(NewsApiService.class);
    
    // NewsAPI.org endpoints
    private static final String BASE_URL = "https://newsapi.org/v2";
    private static final String EVERYTHING_ENDPOINT = BASE_URL + "/everything";
    private static final String TOP_HEADLINES_ENDPOINT = BASE_URL + "/top-headlines";
    private static final String SOURCES_ENDPOINT = BASE_URL + "/sources";
    
    // Primary API key for NewsAPI.org
    private static final String API_KEY = "47bd89a29d8e4accacf955078e8abceb";
    
    // Alternative news sources when primary API is limited
    private static final String[] ALTERNATIVE_RSS_FEEDS = {
        "https://feeds.bbci.co.uk/news/rss.xml",
        "https://rss.cnn.com/rss/edition.rss",
        "https://www.reuters.com/rssFeed/topNews",
        "https://feeds.npr.org/1001/rss.xml",
        "https://www.aljazeera.com/xml/rss/all.xml"
    };
    
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final IndianRssService indianRssService;
    private final UKRssService ukRssService;
    
    public NewsApiService() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .build();
        
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
        this.objectMapper = new ObjectMapper();
        this.indianRssService = new IndianRssService();
        this.ukRssService = new UKRssService();
    }
    
    /**
     * Fetch top headlines for a specific country and/or category
     */
    public NewsResponse getTopHeadlines(String country, String category) {
        try {
            // Try to get real news from multiple sources
            NewsResponse realNews = fetchFromMultipleSources(country, category);
            if (realNews != null && realNews.getArticles() != null && !realNews.getArticles().isEmpty()) {
                return realNews;
            }
            
            // Enhanced fallback with more realistic mock data
            logger.info("API returned limited results. Generating enhanced mock data as fallback.");
            return getEnhancedMockNewsData(country, category);
            
        } catch (Exception e) {
            logger.error("Error fetching top headlines, falling back to enhanced mock data", e);
            return getEnhancedMockNewsData(country, category);
        }
    }
    
    /**
     * Try to fetch from multiple sources and API endpoints
     */
    private NewsResponse fetchFromMultipleSources(String country, String category) {
        NewsResponse bestResponse = null;
        int maxArticles = 0;
        
        // For Indian news, prioritize RSS feeds first
        if ("in".equals(country)) {
            logger.info("Fetching Indian news from dedicated RSS feeds...");
            NewsResponse rssResponse = indianRssService.getIndianNews(category);
            if (rssResponse != null && rssResponse.getArticles() != null && !rssResponse.getArticles().isEmpty()) {
                logger.info("Successfully fetched {} Indian articles from RSS feeds", rssResponse.getArticles().size());
                return rssResponse; // Return RSS results immediately for Indian news
            }
        }
        
        // For UK news, prioritize RSS feeds first
        if ("gb".equals(country)) {
            logger.info("Fetching UK news from dedicated RSS feeds...");
            NewsResponse rssResponse = ukRssService.getUKNews(category);
            if (rssResponse != null && rssResponse.getArticles() != null && !rssResponse.getArticles().isEmpty()) {
                logger.info("Successfully fetched {} UK articles from RSS feeds", rssResponse.getArticles().size());
                return rssResponse; // Return RSS results immediately for UK news
            }
        }
        
        // Try top headlines
        NewsResponse topHeadlines = tryFetchTopHeadlines(country, category);
        if (topHeadlines != null && topHeadlines.getArticles() != null) {
            if (topHeadlines.getArticles().size() > maxArticles) {
                bestResponse = topHeadlines;
                maxArticles = topHeadlines.getArticles().size();
            }
        }
        
        // Try general search with country-specific keywords
        if (country != null) {
            NewsResponse countryNews = tryFetchWithKeywords(getCountryKeywords(country), country);
            if (countryNews != null && countryNews.getArticles() != null) {
                if (countryNews.getArticles().size() > maxArticles) {
                    bestResponse = countryNews;
                    maxArticles = countryNews.getArticles().size();
                }
            }
        }
        
        // Try category-specific search
        if (category != null && !category.equals("general")) {
            NewsResponse categoryNews = tryFetchWithKeywords(category, null);
            if (categoryNews != null && categoryNews.getArticles() != null) {
                if (categoryNews.getArticles().size() > maxArticles) {
                    bestResponse = categoryNews;
                    maxArticles = categoryNews.getArticles().size();
                }
            }
        }
        
        // If still no good results, try RSS feeds as fallback
        if (bestResponse == null || maxArticles < 5) {
            NewsResponse rssNews = fetchFromRssFeeds(country);
            if (rssNews != null && rssNews.getArticles() != null) {
                if (rssNews.getArticles().size() > maxArticles) {
                    bestResponse = rssNews;
                    maxArticles = rssNews.getArticles().size();
                }
            }
        }
        
        return bestResponse;
    }
    
    private NewsResponse tryFetchTopHeadlines(String country, String category) {
        try {
            Map<String, String> params = new HashMap<>();
            if (country != null && !country.isEmpty()) {
                params.put("country", country);
            }
            if (category != null && !category.isEmpty()) {
                params.put("category", category);
            }
            params.put("pageSize", "100");
            
            return tryApiKey(TOP_HEADLINES_ENDPOINT, params);
        } catch (Exception e) {
            logger.debug("Failed to fetch top headlines: {}", e.getMessage());
            return null;
        }
    }
    
    private NewsResponse tryFetchWithKeywords(String keywords, String country) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("q", keywords);
            params.put("sortBy", "publishedAt");
            params.put("pageSize", "100");
            if (country != null) {
                params.put("language", getLanguageForCountry(country));
            }
            
            return tryApiKey(EVERYTHING_ENDPOINT, params);
        } catch (Exception e) {
            logger.debug("Failed to fetch with keywords '{}': {}", keywords, e.getMessage());
            return null;
        }
    }
    
    private NewsResponse tryApiKey(String endpoint, Map<String, String> params) {
        try {
            String url = buildUrlWithApiKey(endpoint, params, API_KEY);
            NewsResponse response = fetchNewsData(url);
            if (response != null && "ok".equals(response.getStatus()) && 
                response.getArticles() != null && !response.getArticles().isEmpty()) {
                return response;
            }
        } catch (Exception e) {
            logger.debug("API key failed: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Search for news articles by keyword
     */
    public NewsResponse searchNews(String query, String sortBy, String language) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("q", query);
            if (sortBy != null && !sortBy.isEmpty()) {
                params.put("sortBy", sortBy);
            }
            if (language != null && !language.isEmpty()) {
                params.put("language", language);
            }
            params.put("pageSize", "100");
            
            String url = buildUrl(EVERYTHING_ENDPOINT, params);
            return fetchNewsData(url);
        } catch (Exception e) {
            logger.error("Error searching news", e);
            return createErrorResponse("Failed to search news: " + e.getMessage());
        }
    }
    
    /**
     * Get news from specific sources
     */
    public NewsResponse getNewsBySources(String sources) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("sources", sources);
            params.put("pageSize", "100");
            
            String url = buildUrl(EVERYTHING_ENDPOINT, params);
            return fetchNewsData(url);
        } catch (Exception e) {
            logger.error("Error fetching news by sources", e);
            return createErrorResponse("Failed to fetch news by sources: " + e.getMessage());
        }
    }
    
    /**
     * Get news for a specific date range
     */
    public NewsResponse getNewsInDateRange(String query, LocalDate from, LocalDate to) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("q", query != null ? query : "*");
            params.put("from", from.format(DateTimeFormatter.ISO_LOCAL_DATE));
            params.put("to", to.format(DateTimeFormatter.ISO_LOCAL_DATE));
            params.put("sortBy", "publishedAt");
            params.put("pageSize", "100");
            
            String url = buildUrl(EVERYTHING_ENDPOINT, params);
            return fetchNewsData(url);
        } catch (Exception e) {
            logger.error("Error fetching news in date range", e);
            return createErrorResponse("Failed to fetch news in date range: " + e.getMessage());
        }
    }
    
    /**
     * Build URL with parameters using current API key
     */
    private String buildUrl(String baseUrl, Map<String, String> params) {
        return buildUrlWithApiKey(baseUrl, params, API_KEY);
    }
    
    /**
     * Build URL with parameters using specific API key
     */
    private String buildUrlWithApiKey(String baseUrl, Map<String, String> params, String apiKey) {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?apiKey=").append(apiKey);
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                url.append("&")
                   .append(entry.getKey())
                   .append("=")
                   .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
        }
        
        return url.toString();
    }
    
    private String getCountryKeywords(String country) {
        switch (country.toLowerCase()) {
            case "in": return "India";
            case "us": return "United States America";
            case "gb": return "United Kingdom Britain UK";
            case "ca": return "Canada";
            case "au": return "Australia";
            case "de": return "Germany Deutschland";
            case "fr": return "France";
            case "jp": return "Japan";
            case "cn": return "China";
            case "br": return "Brazil";
            case "ru": return "Russia";
            default: return country;
        }
    }
    
    private String getLanguageForCountry(String country) {
        switch (country.toLowerCase()) {
            case "in": return "en"; // English for India (most news sources)
            case "us": case "gb": case "ca": case "au": return "en";
            case "de": return "de";
            case "fr": return "fr";
            case "jp": return "ja";
            case "cn": return "zh";
            case "br": return "pt";
            case "ru": return "ru";
            default: return "en";
        }
    }
    
    /**
     * Fetch news data from URL
     */
    private NewsResponse fetchNewsData(String url) {
        try {
            logger.info("Fetching news from: {}", url.replaceAll("apiKey=[^&]*", "apiKey=***"));
            
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", "NewsVisualizer/1.0");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String jsonResponse = EntityUtils.toString(entity);
                    NewsResponse newsResponse = objectMapper.readValue(jsonResponse, NewsResponse.class);
                    
                    logger.info("Fetched {} articles", 
                        newsResponse.getArticles() != null ? newsResponse.getArticles().size() : 0);
                    
                    return newsResponse;
                }
            }
        } catch (IOException e) {
            logger.error("IO error while fetching news", e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching news", e);
        }
        
        return createErrorResponse("Failed to fetch news data");
    }
    
    /**
     * Create error response
     */
    private NewsResponse createErrorResponse(String message) {
        NewsResponse response = new NewsResponse();
        response.setStatus("error");
        response.setMessage(message);
        response.setTotalResults(0);
        return response;
    }
    
    /**
     * Create demo/mock news data for testing when API is not available
     * Supports both country and category filtering
     */
    public NewsResponse getMockNewsData(String country, String category) {
        return generateMockNews(country, category);
    }
    
    /**
     * Enhanced mock news data with more articles and better distribution
     */
    public NewsResponse getEnhancedMockNewsData(String country, String category) {
        return generateEnhancedMockNews(country, category);
    }
    
    // Backward compatibility
    public NewsResponse getMockNewsData(String country) {
        return getMockNewsData(country, null);
    }
    
    private NewsResponse generateEnhancedMockNews(String country, String category) {
        NewsResponse response = new NewsResponse();
        response.setStatus("ok");
        response.setTotalResults(50); // More articles for better visualization
        
        java.util.List<com.newsvisualizer.model.NewsArticle> mockArticles = new java.util.ArrayList<>();
        
        // Generate multiple batches of articles with different timestamps
        for (int batch = 0; batch < 5; batch++) {
            String batchTime = java.time.LocalDateTime.now().minusHours(batch * 4)
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
            
            if (category != null) {
                mockArticles.addAll(generateCategorySpecificArticlesWithTime(category, country, batchTime, batch));
            } else if (country != null) {
                mockArticles.addAll(generateCountrySpecificArticlesWithTime(country, batchTime, batch));
            } else {
                mockArticles.addAll(generateCountrySpecificArticlesWithTime("in", batchTime, batch));
            }
        }
        
        response.setArticles(mockArticles);
        
        String filterInfo = "";
        if (category != null && country != null) {
            filterInfo = category + " news from " + country;
        } else if (category != null) {
            filterInfo = "global " + category + " news";
        } else if (country != null) {
            filterInfo = "news from " + country;
        } else {
            filterInfo = "general news";
        }
        
        logger.info("Generated enhanced mock data - {} articles for {}", 
                   mockArticles.size(), filterInfo);
        
        return response;
    }
    
    private NewsResponse generateMockNews(String country, String category) {
        NewsResponse response = new NewsResponse();
        response.setStatus("ok");
        response.setTotalResults(12);
        
        java.util.List<com.newsvisualizer.model.NewsArticle> mockArticles = new java.util.ArrayList<>();
        
        // Generate articles based on category first, then country
        if (category != null) {
            mockArticles.addAll(generateCategorySpecificArticles(category, country));
        } else if (country != null) {
            mockArticles.addAll(generateCountrySpecificArticles(country));
        } else {
            // Default: Indian general news
            mockArticles.addAll(generateCountrySpecificArticles("in"));
        }
        
        response.setArticles(mockArticles);
        String filterInfo = "";
        if (category != null && country != null) {
            filterInfo = category + " news from " + country;
        } else if (category != null) {
            filterInfo = "global " + category + " news";
        } else if (country != null) {
            filterInfo = "news from " + country;
        } else {
            filterInfo = "general news";
        }
        
        logger.info("Using mock news data for demonstration - {} articles for {}", 
                   mockArticles.size(), filterInfo);
        
        return response;
    }
    
    private java.util.List<com.newsvisualizer.model.NewsArticle> generateCategorySpecificArticles(String category, String country) {
        java.util.List<com.newsvisualizer.model.NewsArticle> articles = new java.util.ArrayList<>();
        String countryName = getCountryName(country);
        
        switch (category.toLowerCase()) {
            case "business":
                articles.add(createMockArticle(
                    countryName + " Stock Market Reaches New Heights",
                    "Financial markets show strong performance with significant gains across major sectors.",
                    "The stock market in " + countryName + " continues its upward trajectory...",
                    "Business Today", "2025-10-07T10:30:00Z"
                ));
                articles.add(createMockArticle(
                    "Tech Giants Report Record Quarterly Earnings",
                    "Major technology companies announce impressive financial results for the quarter.",
                    "Leading technology firms have posted exceptional earnings...",
                    "Tech Business", "2025-10-07T09:15:00Z"
                ));
                articles.add(createMockArticle(
                    "Cryptocurrency Market Shows Volatility",
                    "Digital currencies experience significant price movements amid regulatory discussions.",
                    "The cryptocurrency market has been experiencing heightened volatility...",
                    "Crypto News", "2025-10-07T08:45:00Z"
                ));
                break;
                
            case "technology":
                articles.add(createMockArticle(
                    "AI Revolution Transforms Healthcare Industry",
                    "Artificial intelligence applications are revolutionizing medical diagnosis and treatment.",
                    "The healthcare sector is witnessing unprecedented transformation...",
                    "AI Health", "2025-10-07T10:30:00Z"
                ));
                articles.add(createMockArticle(
                    "Quantum Computing Breakthrough Announced",
                    "Scientists achieve major milestone in quantum computing development.",
                    "A team of researchers has made a significant breakthrough...",
                    "Quantum Tech", "2025-10-07T09:15:00Z"
                ));
                articles.add(createMockArticle(
                    "5G Network Expansion Accelerates Globally",
                    "Telecommunications companies rapidly deploy 5G infrastructure worldwide.",
                    "The global rollout of 5G technology is gaining momentum...",
                    "Telecom Today", "2025-10-07T08:45:00Z"
                ));
                break;
                
            case "health":
                articles.add(createMockArticle(
                    "Breakthrough Cancer Treatment Shows Promise",
                    "New immunotherapy approach demonstrates remarkable success in clinical trials.",
                    "Medical researchers have developed a groundbreaking cancer treatment...",
                    "Medical News", "2025-10-07T10:30:00Z"
                ));
                articles.add(createMockArticle(
                    "Mental Health Awareness Programs Expand",
                    "Communities worldwide launch comprehensive mental health initiatives.",
                    "Mental health support programs are being implemented...",
                    "Health Today", "2025-10-07T09:15:00Z"
                ));
                articles.add(createMockArticle(
                    "Vaccine Development Reaches New Milestone",
                    "Scientists make significant progress in developing next-generation vaccines.",
                    "Research teams have achieved a major milestone...",
                    "Vaccine Research", "2025-10-07T08:45:00Z"
                ));
                break;
                
            case "sports":
                articles.add(createMockArticle(
                    countryName + " Athletes Excel in International Competition",
                    "National team delivers outstanding performance in recent international tournament.",
                    "Athletes from " + countryName + " have showcased exceptional skill...",
                    "Sports Central", "2025-10-07T10:30:00Z"
                ));
                articles.add(createMockArticle(
                    "Olympic Preparations Intensify",
                    "Athletes and officials ramp up preparations for upcoming Olympic Games.",
                    "With the Olympics approaching, preparation efforts are intensifying...",
                    "Olympic News", "2025-10-07T09:15:00Z"
                ));
                articles.add(createMockArticle(
                    "Football Championship Delivers Thrilling Matches",
                    "National football championship features exciting games and unexpected results.",
                    "The ongoing football championship has been full of surprises...",
                    "Football Today", "2025-10-07T08:45:00Z"
                ));
                break;
                
            case "entertainment":
                articles.add(createMockArticle(
                    "Film Industry Celebrates Record Box Office Year",
                    "Movie theaters report highest attendance figures in recent history.",
                    "The film industry is celebrating an exceptional year...",
                    "Entertainment Weekly", "2025-10-07T10:30:00Z"
                ));
                articles.add(createMockArticle(
                    "Music Festival Season Kicks Off",
                    "Major music festivals announce star-studded lineups for the season.",
                    "The music festival season is about to begin...",
                    "Music News", "2025-10-07T09:15:00Z"
                ));
                articles.add(createMockArticle(
                    "Streaming Platforms Launch Original Content",
                    "Digital streaming services invest heavily in original programming.",
                    "Streaming platforms are significantly expanding their original content...",
                    "Stream Today", "2025-10-07T08:45:00Z"
                ));
                break;
                
            case "science":
                articles.add(createMockArticle(
                    "Space Mission Discovers New Exoplanets",
                    "Astronomical survey identifies potentially habitable worlds beyond our solar system.",
                    "A recent space mission has made remarkable discoveries...",
                    "Space Science", "2025-10-07T10:30:00Z"
                ));
                articles.add(createMockArticle(
                    "Climate Research Reveals Important Findings",
                    "Scientists publish crucial data on climate change patterns and solutions.",
                    "New climate research has provided valuable insights...",
                    "Climate News", "2025-10-07T09:15:00Z"
                ));
                articles.add(createMockArticle(
                    "Marine Biology Expedition Uncovers New Species",
                    "Deep sea exploration leads to discovery of previously unknown marine life.",
                    "Marine biologists have made exciting discoveries...",
                    "Ocean Research", "2025-10-07T08:45:00Z"
                ));
                break;
                
            default: // General news
                articles.addAll(generateCountrySpecificArticles(country != null ? country : "global"));
                break;
        }
        
        return articles;
    }
    
    private java.util.List<com.newsvisualizer.model.NewsArticle> generateCountrySpecificArticles(String country) {
        java.util.List<com.newsvisualizer.model.NewsArticle> articles = new java.util.ArrayList<>();
        
        String countryName = getCountryName(country);
        
        if ("in".equals(country)) {
            // Indian news articles
            articles.add(createMockArticle(
                "India's Tech Sector Shows Strong Growth in Q3 2024",
                "Indian technology companies report impressive quarterly results with significant growth in AI and software exports.",
                "The Indian technology sector continues to demonstrate robust performance...",
                "TechNews India", "2025-10-07T10:30:00Z"
            ));
            articles.add(createMockArticle(
                "Monsoon Recovery Boosts Agricultural Production Across States",
                "Good monsoon rains this year have led to increased agricultural output, providing relief to farmers nationwide.",
                "After a challenging previous season, India's agricultural sector...",
                "Agriculture Today", "2025-10-07T09:15:00Z"
            ));
            articles.add(createMockArticle(
                "Mumbai Metro Expansion Project Reaches New Phase",
                "The ambitious metro expansion in Mumbai enters a crucial phase with new line construction beginning.",
                "Mumbai's transportation infrastructure gets a major boost...",
                "Transport News", "2025-10-07T07:20:00Z"
            ));
        } else if ("us".equals(country)) {
            // US news articles
            articles.add(createMockArticle(
                "United States Economy Shows Strong Quarterly Performance",
                "US economic indicators demonstrate robust growth across multiple sectors.",
                "The United States economy continues to show strength...",
                "US Economics", "2025-10-07T10:30:00Z"
            ));
            articles.add(createMockArticle(
                "Silicon Valley Innovation Drives Tech Advancement",
                "Leading US technology companies announce breakthrough developments in AI and computing.",
                "Silicon Valley remains at the forefront of technological innovation...",
                "US Tech", "2025-10-07T09:15:00Z"
            ));
            articles.add(createMockArticle(
                "NASA Space Mission Achieves Historic Milestone",
                "American space program reaches new heights with successful Mars exploration mission.",
                "NASA's latest space mission has achieved unprecedented success...",
                "US Space", "2025-10-07T08:45:00Z"
            ));
        } else if ("gb".equals(country)) {
            // UK news articles
            articles.add(createMockArticle(
                "United Kingdom Leads European Green Energy Initiative",
                "UK government announces ambitious renewable energy targets and investment plans.",
                "The United Kingdom is positioning itself as a leader...",
                "UK Energy", "2025-10-07T10:30:00Z"
            ));
            articles.add(createMockArticle(
                "London Financial District Adapts to Digital Future",
                "The City of London embraces fintech innovations and digital transformation.",
                "London's financial sector is undergoing significant modernization...",
                "UK Finance", "2025-10-07T09:15:00Z"
            ));
            articles.add(createMockArticle(
                "British Universities Pioneer AI Research",
                "UK academic institutions make groundbreaking advances in artificial intelligence.",
                "British universities are at the cutting edge of AI research...",
                "UK Education", "2025-10-07T08:45:00Z"
            ));
        } else {
            // Generic international news
            articles.add(createMockArticle(
                countryName + " Demonstrates Economic Resilience",
                "National economy shows stability despite global challenges and uncertainties.",
                countryName + " has maintained economic stability...",
                "Global Economics", "2025-10-07T10:30:00Z"
            ));
            articles.add(createMockArticle(
                "Innovation Centers Emerge in " + countryName,
                "New technology hubs are driving innovation and economic growth.",
                "Technology innovation centers in " + countryName + " are gaining recognition...",
                "Tech Global", "2025-10-07T09:15:00Z"
            ));
            articles.add(createMockArticle(
                "Environmental Initiatives Gain Support in " + countryName,
                "Government and private sector collaborate on sustainability projects.",
                "Environmental conservation efforts in " + countryName + "...",
                "Global Environment", "2025-10-07T08:45:00Z"
            ));
        }
        
        return articles;
    }
    
    private String getCountryName(String countryCode) {
        if (countryCode == null) return "Global";
        switch (countryCode.toLowerCase()) {
            case "in": return "India";
            case "us": return "United States";
            case "gb": return "United Kingdom";
            case "ca": return "Canada";
            case "au": return "Australia";
            case "de": return "Germany";
            case "fr": return "France";
            case "jp": return "Japan";
            case "cn": return "China";
            default: return "International";
        }
    }
    
    private com.newsvisualizer.model.NewsArticle createMockArticle(
            String title, String description, String content, String sourceName, String publishedAt) {
        com.newsvisualizer.model.NewsArticle article = new com.newsvisualizer.model.NewsArticle();
        article.setTitle(title);
        article.setDescription(description);
        article.setContent(content);
        article.setUrl("https://example.com/article");
        article.setPublishedAt(publishedAt);
        
        com.newsvisualizer.model.Source source = new com.newsvisualizer.model.Source();
        source.setName(sourceName);
        article.setSource(source);
        
        return article;
    }
    
    /**
     * Close resources
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
            if (indianRssService != null) {
                indianRssService.close();
            }
            if (ukRssService != null) {
                ukRssService.close();
            }
        } catch (IOException e) {
            logger.error("Error closing HTTP client", e);
        }
    }
    
    /**
     * Generate category-specific articles with custom timestamps
     */
    private java.util.List<com.newsvisualizer.model.NewsArticle> generateCategorySpecificArticlesWithTime(
            String category, String country, String publishedAt, int batch) {
        java.util.List<com.newsvisualizer.model.NewsArticle> articles = new java.util.ArrayList<>();
        String countryName = getCountryName(country);
        
        switch (category.toLowerCase()) {
            case "business":
                articles.add(createMockArticle(
                    countryName + " Market Analysis Shows " + getRandomTrend() + " Growth",
                    "Economic indicators demonstrate " + getRandomPerformance() + " performance across sectors.",
                    "Financial markets continue to show " + getRandomTrend().toLowerCase() + " momentum...",
                    "Business " + getRandomSource(), publishedAt
                ));
                break;
            case "technology":
                articles.add(createMockArticle(
                    "Tech Innovation in " + countryName + " " + getTechTrend(batch),
                    "Technology sector demonstrates " + getRandomTechAdvancement() + " in key areas.",
                    "The technology landscape in " + countryName + " continues to evolve...",
                    "Tech " + getRandomSource(), publishedAt
                ));
                break;
            case "health":
                articles.add(createMockArticle(
                    "Healthcare " + getHealthTrend(batch) + " in " + countryName,
                    "Medical sector shows " + getRandomHealthDevelopment() + " in treatment approaches.",
                    "Healthcare professionals report " + getRandomHealthDevelopment().toLowerCase() + " developments...",
                    "Health " + getRandomSource(), publishedAt
                ));
                break;
            case "sports":
                articles.add(createMockArticle(
                    countryName + " Sports " + getSportsTrend(batch),
                    "Athletic competitions showcase " + getRandomSportsOutcome() + " performances.",
                    "Sports enthusiasts celebrate " + getRandomSportsOutcome().toLowerCase() + " results...",
                    "Sports " + getRandomSource(), publishedAt
                ));
                break;
            case "entertainment":
                articles.add(createMockArticle(
                    "Entertainment Industry " + getEntertainmentTrend(batch) + " in " + countryName,
                    "Cultural sector experiences " + getRandomCulturalDevelopment() + " activities.",
                    "The entertainment landscape shows " + getRandomCulturalDevelopment().toLowerCase() + " trends...",
                    "Entertainment " + getRandomSource(), publishedAt
                ));
                break;
            default:
                articles.addAll(generateCountrySpecificArticlesWithTime(country, publishedAt, batch));
                break;
        }
        
        return articles;
    }
    
    /**
     * Generate country-specific articles with custom timestamps
     */
    private java.util.List<com.newsvisualizer.model.NewsArticle> generateCountrySpecificArticlesWithTime(
            String country, String publishedAt, int batch) {
        java.util.List<com.newsvisualizer.model.NewsArticle> articles = new java.util.ArrayList<>();
        String countryName = getCountryName(country);
        
        String[] topics = {"Economic", "Political", "Social", "Environmental", "Cultural"};
        String[] outcomes = {"positive", "significant", "notable", "remarkable", "substantial"};
        String[] developments = {"development", "progress", "advancement", "initiative", "breakthrough"};
        
        String topic = topics[batch % topics.length];
        String outcome = outcomes[batch % outcomes.length];
        String development = developments[batch % developments.length];
        
        articles.add(createMockArticle(
            countryName + " Reports " + topic + " " + development.substring(0, 1).toUpperCase() + development.substring(1),
            "National " + topic.toLowerCase() + " sector demonstrates " + outcome + " " + development + " in key areas.",
            "Recent " + topic.toLowerCase() + " developments in " + countryName + " show " + outcome + " progress...",
            getRandomSource(), publishedAt
        ));
        
        return articles;
    }
    
    // Helper methods for random content generation
    private String getRandomTrend() {
        String[] trends = {"Strong", "Steady", "Robust", "Significant", "Notable"};
        return trends[(int)(Math.random() * trends.length)];
    }
    
    private String getRandomPerformance() {
        String[] performances = {"excellent", "strong", "solid", "impressive", "remarkable"};
        return performances[(int)(Math.random() * performances.length)];
    }
    
    private String getRandomSource() {
        String[] sources = {"Today", "News", "Report", "Times", "Herald", "Post", "Tribune", "Gazette"};
        return sources[(int)(Math.random() * sources.length)];
    }
    
    private String getTechTrend(int batch) {
        String[] trends = {"Advances AI Development", "Embraces Blockchain", "Expands Cloud Services", 
                          "Innovates IoT Solutions", "Develops Green Technology"};
        return trends[batch % trends.length];
    }
    
    private String getRandomTechAdvancement() {
        String[] advancements = {"breakthrough progress", "innovative solutions", "cutting-edge developments", 
                               "revolutionary changes", "transformative innovations"};
        return advancements[(int)(Math.random() * advancements.length)];
    }
    
    private String getHealthTrend(int batch) {
        String[] trends = {"Breakthrough", "Innovation", "Progress", "Development", "Advancement"};
        return trends[batch % trends.length];
    }
    
    private String getRandomHealthDevelopment() {
        String[] developments = {"promising results", "significant improvement", "breakthrough discoveries", 
                               "innovative treatments", "advanced therapies"};
        return developments[(int)(Math.random() * developments.length)];
    }
    
    private String getSportsTrend(int batch) {
        String[] trends = {"Achievement", "Victory", "Success", "Championship", "Excellence"};
        return trends[batch % trends.length];
    }
    
    private String getRandomSportsOutcome() {
        String[] outcomes = {"outstanding", "exceptional", "remarkable", "impressive", "stellar"};
        return outcomes[(int)(Math.random() * outcomes.length)];
    }
    
    private String getEntertainmentTrend(int batch) {
        String[] trends = {"Flourishes", "Expands", "Innovates", "Celebrates", "Transforms"};
        return trends[batch % trends.length];
    }
    
    private String getRandomCulturalDevelopment() {
        String[] developments = {"vibrant", "diverse", "dynamic", "creative", "engaging"};
        return developments[(int)(Math.random() * developments.length)];
    }
    
    /**
     * Check if API key is configured
     */
    public boolean isApiKeyConfigured() {
        return API_KEY != null && !API_KEY.equals("YOUR_API_KEY_HERE") && !API_KEY.isEmpty();
    }
    
    /**
     * Check if RSS feeds are being used for a specific country
     */
    public boolean isUsingRssFeeds(String country) {
        return "in".equals(country);
    }
    
    /**
     * Get available RSS categories for Indian news
     */
    public List<String> getIndianRssCategories() {
        return indianRssService.getAvailableCategories();
    }
    
    /**
     * Get available Indian RSS sources
     */
    public List<String> getIndianRssSources() {
        return indianRssService.getAvailableSources();
    }
    
    /**
     * Get article summary from URL
     */
    public String getArticleSummary(String url) {
        try {
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", "NewsVisualizer/1.0");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String content = EntityUtils.toString(entity);
                    // Simple text extraction - in a real implementation, you'd use a proper HTML parser
                    return extractTextSummary(content);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching article content from URL: {}", url, e);
        }
        
        return "Unable to fetch article summary. The article may require direct access through the original source.";
    }
    
    private String extractTextSummary(String htmlContent) {
        // Basic HTML tag removal and text extraction
        String text = htmlContent.replaceAll("<[^>]+>", "");
        text = text.replaceAll("\\s+", " ").trim();
        
        // Extract first few sentences (basic summary)
        String[] sentences = text.split("\\. ");
        StringBuilder summary = new StringBuilder();
        
        int sentenceCount = Math.min(3, sentences.length);
        for (int i = 0; i < sentenceCount; i++) {
            summary.append(sentences[i].trim());
            if (i < sentenceCount - 1) {
                summary.append(". ");
            }
        }
        
        if (summary.length() > 500) {
            return summary.substring(0, 500) + "...";
        }
        
        return summary.toString();
    }
    
    /**
     * Fetch news from RSS feeds as fallback when API is limited
     */
    private NewsResponse fetchFromRssFeeds(String country) {
        try {
            logger.info("Attempting to fetch news from RSS feeds as fallback");
            
            List<com.newsvisualizer.model.NewsArticle> allArticles = new ArrayList<>();
            
            // Try a few RSS feeds
            for (int i = 0; i < Math.min(3, ALTERNATIVE_RSS_FEEDS.length); i++) {
                try {
                    List<com.newsvisualizer.model.NewsArticle> articles = parseRssFeed(ALTERNATIVE_RSS_FEEDS[i]);
                    if (articles != null) {
                        allArticles.addAll(articles);
                        if (allArticles.size() >= 15) {
                            break; // We have enough articles
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Failed to parse RSS feed {}: {}", ALTERNATIVE_RSS_FEEDS[i], e.getMessage());
                }
            }
            
            if (!allArticles.isEmpty()) {
                NewsResponse response = new NewsResponse();
                response.setStatus("ok");
                response.setTotalResults(allArticles.size());
                response.setArticles(allArticles);
                
                logger.info("Fetched {} articles from RSS feeds", allArticles.size());
                return response;
            }
            
        } catch (Exception e) {
            logger.error("Error fetching from RSS feeds", e);
        }
        
        return null;
    }
    
    /**
     * Simple RSS feed parser (basic implementation)
     */
    private List<com.newsvisualizer.model.NewsArticle> parseRssFeed(String feedUrl) {
        try {
            HttpGet request = new HttpGet(feedUrl);
            request.setHeader("User-Agent", "NewsVisualizer/1.0");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String xmlContent = EntityUtils.toString(entity);
                    return parseRssXml(xmlContent, feedUrl);
                }
            }
        } catch (Exception e) {
            logger.debug("Error parsing RSS feed {}: {}", feedUrl, e.getMessage());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Parse RSS XML content (basic regex-based parsing)
     */
    private List<com.newsvisualizer.model.NewsArticle> parseRssXml(String xmlContent, String feedUrl) {
        List<com.newsvisualizer.model.NewsArticle> articles = new ArrayList<>();
        
        try {
            // Basic regex patterns for RSS parsing
            String itemPattern = "<item[^>]*>(.*?)</item>";
            String titlePattern = "<title[^>]*><!\\[CDATA\\[(.*?)\\]\\]></title>|<title[^>]*>(.*?)</title>";
            String descPattern = "<description[^>]*><!\\[CDATA\\[(.*?)\\]\\]></description>|<description[^>]*>(.*?)</description>";
            String linkPattern = "<link[^>]*>(.*?)</link>";
            String pubDatePattern = "<pubDate[^>]*>(.*?)</pubDate>";
            
            java.util.regex.Pattern itemRegex = java.util.regex.Pattern.compile(itemPattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher itemMatcher = itemRegex.matcher(xmlContent);
            
            int count = 0;
            while (itemMatcher.find() && count < 10) { // Limit to 10 articles per feed
                String itemContent = itemMatcher.group(1);
                
                String title = extractMatch(itemContent, titlePattern);
                String description = extractMatch(itemContent, descPattern);
                String link = extractMatch(itemContent, linkPattern);
                String pubDate = extractMatch(itemContent, pubDatePattern);
                
                if (title != null && !title.trim().isEmpty()) {
                    com.newsvisualizer.model.NewsArticle article = new com.newsvisualizer.model.NewsArticle();
                    article.setTitle(cleanHtml(title));
                    article.setDescription(cleanHtml(description));
                    article.setUrl(link != null ? link.trim() : "");
                    
                    // Set source based on feed URL
                    com.newsvisualizer.model.Source source = new com.newsvisualizer.model.Source();
                    source.setName(getSourceNameFromUrl(feedUrl));
                    article.setSource(source);
                    
                    // Try to parse publication date (basic parsing)
                    try {
                        if (pubDate != null) {
                            article.setPublishedAt(pubDate);
                        } else {
                            // Use current time as fallback
                            article.setPublishedAt(java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
                        }
                    } catch (Exception e) {
                        article.setPublishedAt(java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
                    }
                    
                    articles.add(article);
                    count++;
                }
            }
            
        } catch (Exception e) {
            logger.debug("Error parsing RSS XML: {}", e.getMessage());
        }
        
        return articles;
    }
    
    private String extractMatch(String content, String pattern) {
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = regex.matcher(content);
        
        if (matcher.find()) {
            // Try CDATA content first (group 1), then regular content (group 2)
            String cdata = matcher.group(1);
            if (cdata != null && !cdata.trim().isEmpty()) {
                return cdata;
            }
            if (matcher.groupCount() >= 2) {
                String regular = matcher.group(2);
                if (regular != null && !regular.trim().isEmpty()) {
                    return regular;
                }
            }
        }
        
        return null;
    }
    
    private String cleanHtml(String text) {
        if (text == null) return "";
        // Remove HTML tags and decode basic entities
        return text.replaceAll("<[^>]+>", "")
                  .replaceAll("&lt;", "<")
                  .replaceAll("&gt;", ">")
                  .replaceAll("&amp;", "&")
                  .replaceAll("&quot;", "\"")
                  .replaceAll("&#39;", "'")
                  .trim();
    }
    
    private String getSourceNameFromUrl(String url) {
        try {
            if (url.contains("bbc")) return "BBC News";
            if (url.contains("cnn")) return "CNN";
            if (url.contains("reuters")) return "Reuters";
            if (url.contains("npr")) return "NPR";
            if (url.contains("aljazeera")) return "Al Jazeera";
            
            // Extract domain as fallback
            java.net.URL urlObj = new java.net.URL(url);
            String host = urlObj.getHost();
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (Exception e) {
            return "RSS Feed";
        }
    }
}
