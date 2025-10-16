# UK RSS Integration for NewsVisualizer

## Overview
This document describes the integration of UK news sources into the NewsVisualizer application, allowing users to fetch news from major UK news outlets when selecting "United Kingdom (gb)" from the country dropdown.

## Implemented Features

### 1. UKRssService
A new service class that handles RSS feeds from major UK news sources:

**Supported UK News Sources:**
- BBC News (https://feeds.bbci.co.uk/news/rss.xml)
- The Guardian (https://www.theguardian.com/uk/rss)  
- Sky News (https://feeds.skynews.com/feeds/rss/home.xml)
- The Independent (https://www.independent.co.uk/rss)
- Reuters UK (https://feeds.reuters.com/reuters/UKdomesticNews)
- Evening Standard (https://www.standard.co.uk/rss)
- Daily Mail (https://www.dailymail.co.uk/home/index.rss)
- The Telegraph (https://www.telegraph.co.uk/rss.xml)
- GOV.UK News (https://www.gov.uk/government/news.atom)

### 2. NewsApiService Integration
Modified the existing NewsApiService to:
- Initialize UKRssService alongside IndianRssService
- Detect when country code "gb" is selected
- Prioritize UK RSS feeds for UK news requests
- Handle resource cleanup for UK RSS service

### 3. UI Enhancements
Updated MainWindow.java to:
- Display appropriate status messages for UK RSS feeds
- Detect and label UK articles correctly in the results
- Show "📡 Fetching news from UK RSS feeds..." status messages
- Recognize UK news sources in the article detection logic

## How It Works

1. **User Selection**: When user selects "United Kingdom (gb)" from the country dropdown
2. **Service Detection**: NewsApiService detects the "gb" country code
3. **RSS Fetching**: UKRssService fetches articles from multiple UK RSS feeds concurrently
4. **Data Processing**: Articles are processed, cleaned, and sorted by publication date
5. **UI Display**: Results are displayed with appropriate UK-specific status messages

## Technical Implementation

### RSS Feed Processing
- Fetches from up to 6 UK RSS feeds simultaneously for faster response
- Limits to 8 articles per feed to prevent overwhelming the UI
- Sorts articles by publication date (most recent first)
- Cleans HTML tags and decodes entities from article text
- Sets proper source metadata (country: "gb", language: "en")

### Error Handling
- Graceful failure handling for individual RSS feeds
- Continues processing even if some feeds are unavailable
- Logs warnings for failed feeds without breaking the user experience
- Fallback mechanisms in place for network issues

### Performance Optimizations
- HTTP client with proper timeouts (15 seconds)
- Connection pooling and reuse
- Limited concurrent requests to avoid overwhelming servers
- Efficient memory usage with article limits

## Usage Instructions

1. Start the NewsVisualizer application
2. Select "United Kingdom (gb)" from the country dropdown
3. Optionally select a category (General, Business, Technology, etc.)
4. Click "📊 Fetch News"
5. The application will display "📡 Fetching news from UK RSS feeds..." 
6. Results will show articles from various UK news sources
7. Status will update to show successful fetch with count

## Supported Categories
- General (default)
- Business
- Technology  
- Sports
- Health
- Entertainment

Note: Category filtering is applied at the service level, though RSS feeds may not always have perfect category separation.

## RSS Feed Status
All major UK news RSS feeds have been tested and confirmed working as of implementation date. The system gracefully handles temporary feed unavailability.

## Future Enhancements
- Add more regional UK news sources
- Implement category-specific RSS feeds where available
- Add RSS feed health monitoring
- Consider adding Northern Ireland, Scotland, Wales specific sources
- Implement caching for better performance