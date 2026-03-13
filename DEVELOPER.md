# DEVELOPER.md

This file provides guidance for developers and AI assistants when working with code in this repository.

## Project Overview

NewsVisualizer is a cross-platform Java application for fetching, analyzing, and visualizing news data. It uses NewsAPI.org to fetch articles, performs sentiment analysis, and creates interactive charts showing sentiment distribution, source distribution, keyword frequency, and publication timelines.

**Technology Stack:**
- Java 21 (but compatible with Java 11+)
- Maven for build management
- Swing for GUI
- JFreeChart for visualization
- Jackson for JSON processing
- Apache HttpClient for API requests
- JUnit 5 for testing

## Common Development Commands

### Building and Running
```bash
# Full build and run (recommended)
mvn clean compile exec:java -Dexec.mainClass="com.newsvisualizer.NewsVisualizerApp"

# Create standalone JAR
mvn clean package
java -jar target/news-visualizer-1.0.0.jar

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=NewsAnalyzerTest

# Clean build artifacts
mvn clean
```

### Cross-Platform Launchers
The project includes multiple launcher options for different environments:

```bash
# Universal Python launcher (works on all platforms)
python3 launch.py

# Platform-specific scripts
./run.sh           # macOS/Linux
run.bat            # Windows

# Make commands (if Make is installed)
make run           # Compile and run
make test          # Run tests  
make package       # Create JAR
make clean         # Clean build
make help          # Show all commands
```

### Development Setup
```bash
# First-time setup (cross-platform)
./setup.sh         # macOS/Linux
setup.bat          # Windows

# Open in VS Code
./open-in-vscode.sh    # macOS/Linux
open-in-vscode.bat     # Windows
```

## Code Architecture

### Package Structure
- `com.newsvisualizer` - Main application entry point
- `com.newsvisualizer.model` - Data models (NewsArticle, Source, NewsResponse)  
- `com.newsvisualizer.service` - External API integration (NewsApiService)
- `com.newsvisualizer.utils` - Utility classes (NewsAnalyzer for sentiment analysis)
- `com.newsvisualizer.visualization` - Chart generation (ChartGenerator)
- `com.newsvisualizer.gui` - Swing-based user interface (MainWindow)

### Key Architectural Patterns

**MVC Pattern:**
- **Model:** NewsArticle, Source, NewsResponse POJOs with Jackson annotations
- **View:** MainWindow (Swing GUI) with tabbed interface for different visualizations  
- **Controller:** Event handlers in MainWindow coordinate between service calls and UI updates

**Service Layer:**
- `NewsApiService` handles all external API communication with configurable endpoints, timeout settings, and error handling
- Supports multiple API endpoints: top headlines, everything search, sources, date ranges
- API key configuration required (replace `YOUR_API_KEY_HERE` in NewsApiService.java)

**Analysis Pipeline:**
- `NewsAnalyzer` processes fetched articles through sentiment analysis using word-based scoring
- Extracts keywords with stop-word filtering and frequency analysis
- Groups articles by date, source, and sentiment for visualization
- All analysis is done in-memory for real-time results

**Visualization Architecture:**
- `ChartGenerator` creates JFreeChart-based visualizations
- Supports pie charts (sentiment distribution), bar charts (keyword frequency, source distribution), and time series (publication timeline)
- Charts are embedded in Swing panels within the main tabbed interface

### Data Flow
1. User enters search criteria in MainWindow GUI
2. MainWindow calls NewsApiService to fetch articles via HTTP API
3. NewsAnalyzer processes articles for sentiment and keyword extraction
4. ChartGenerator creates visualizations from analyzed data
5. Results populate different tabs in the MainWindow interface

### Background Processing
- Network requests and data analysis run in background threads using SwingWorker pattern
- Progress bars and status updates keep UI responsive during long operations
- Error handling displays user-friendly messages while logging detailed errors

## Configuration

### API Configuration
Edit `src/main/java/com/newsvisualizer/service/NewsApiService.java`:
- Replace `YOUR_API_KEY_HERE` with actual NewsAPI.org API key
- Modify timeout settings (currently 10 seconds)
- Add new endpoints or change base URL if needed
- Adjust page size limits (currently 100 articles max)

### Sentiment Analysis Tuning
Edit `src/main/java/com/newsvisualizer/utils/NewsAnalyzer.java`:
- Modify `POSITIVE_WORDS` and `NEGATIVE_WORDS` sets for different sentiment vocabularies
- Adjust sentiment threshold values in `getSentimentText()` method (currently ±0.1)
- Update stop words list for keyword extraction
- Change keyword extraction limits (currently top 15 for charts)

### GUI Customization
Edit `src/main/java/com/newsvisualizer/gui/MainWindow.java`:
- Modify country and category dropdown options
- Adjust table column widths and display formats
- Change color schemes and UI layouts
- Add new visualization tabs

## Testing

### Test Structure
- Unit tests in `src/test/java/com/newsvisualizer/`
- `NewsAnalyzerTest.java` covers sentiment analysis, keyword extraction, and data grouping functionality
- Tests use JUnit 5 with assertion-based validation

### Running Tests
```bash
# All tests
mvn test

# Specific test class  
mvn test -Dtest=NewsAnalyzerTest

# Test with debugging
mvn test -Dmaven.surefire.debug

# Generate test coverage report (if configured)
mvn jacoco:report
```

### Test Data Patterns
Tests create mock NewsArticle objects with known sentiment words to validate analysis algorithms. When adding new analysis features, follow the pattern of creating test articles with predictable content.

## Deployment Notes

### Prerequisites
- Java 11 or higher (project configured for Java 21)
- Maven 3.6+
- Internet connection for API calls
- NewsAPI.org API key (optional for demo mode)

### Cross-Platform Considerations
- Application uses cross-platform file paths and system look-and-feel
- Launcher scripts provided for Windows (.bat), Unix (.sh), and Python (launch.py)  
- Make commands work on systems with GNU Make installed
- GUI adapts to system themes and screen DPI settings

### Distribution
- `mvn package` creates shaded JAR with all dependencies
- JAR includes manifest with main class for direct execution
- No external dependencies required at runtime beyond Java

## Common Issues and Solutions

### API Key Configuration
If articles aren't fetching, check that API key is configured in `NewsApiService.java`. The application includes mock data functionality for testing without API access.

### Memory Issues with Large Datasets
The application processes all data in memory. For large article sets, consider implementing pagination or streaming processing in the analysis pipeline.

### Chart Display Issues
If charts don't render properly, verify JFreeChart dependency is correctly loaded and system has adequate graphics support for Swing applications.

### Cross-Platform Launch Issues
If platform-specific scripts fail, use the universal Python launcher (`python3 launch.py`) which handles environment detection automatically.