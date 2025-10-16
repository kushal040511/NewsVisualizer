# News Visualizer

A Java application for fetching, analyzing, and visualizing news data with interactive charts and sentiment analysis.

## Features

- **News Fetching**: Retrieve news articles from external APIs (NewsAPI.org)
- **Sentiment Analysis**: Analyze the emotional tone of news articles
- **Data Visualization**: Interactive charts and graphs showing:
  - Sentiment distribution
  - Source distribution
  - Keyword frequency
  - Publication timeline
- **Search & Filter**: Search by keywords, country, or category
- **Modern GUI**: Clean Swing-based interface with tabbed layout

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Internet connection for fetching news data

## Setup Instructions

### 🚀 Quick Setup (Cross-Platform)

**Windows:**
```batch
setup.bat
```

**macOS/Linux:**
```bash
./setup.sh
```

**Universal (Python):**
```bash
python3 launch.py
```

The setup wizard will:
- Check system requirements (Java & Maven)
- Provide OS-specific installation instructions
- Test the installation
- Create appropriate launch shortcuts

2. **Get a NewsAPI Key** (Optional but recommended):
   - Visit [https://newsapi.org](https://newsapi.org) and register for a free account
   - Get your API key
   - Edit `src/main/java/com/newsvisualizer/service/NewsApiService.java`
   - Replace `YOUR_API_KEY_HERE` with your actual API key

### 🖥️ Running the Application

**Method 1: Platform-Specific Launchers**

Windows:
```batch
run.bat                 # Full launcher with compilation
quick-launch.bat       # Quick launcher for compiled project
```

macOS/Linux:
```bash
./run.sh               # Full launcher with compilation
chmod +x run.sh        # Make executable first time
```

**Method 2: Universal Python Launcher**
```bash
python3 launch.py      # Works on all platforms
```

**Method 3: Make Commands** (if Make is installed)
```bash
make run               # Compile and run
make setup             # Run setup wizard
make vscode            # Open in VS Code
make help              # Show all commands
```

**Method 4: Command Line**
```bash
# Compile and run
mvn clean compile
mvn exec:java -Dexec.mainClass="com.newsvisualizer.NewsVisualizerApp"

# Or build JAR and run
mvn clean package
java -jar target/news-visualizer-1.0.0.jar
```

**Method 4: IDE**
- Open the project folder in IntelliJ IDEA, Eclipse, or VS Code
- Import as Maven project
- Run main class: `com.newsvisualizer.NewsVisualizerApp`

## Usage

1. **Launch the application** - The main window will open with search controls at the top

2. **Fetch News**:
   - Enter keywords in the search field (optional)
   - Select a country code (optional)
   - Select a news category (optional)
   - Click "Fetch News"

3. **Analyze Data**:
   - After news is fetched, click "Analyze Data"
   - The application will perform sentiment analysis and generate visualizations

4. **View Results**:
   - **Articles Tab**: View all fetched articles in a table
   - **Sentiment Analysis**: See sentiment distribution and summary statistics
   - **Source Distribution**: View which sources provided the most articles
   - **Keywords**: See the most frequently mentioned keywords
   - **Timeline**: View article publication patterns over time

## Project Structure

```
NewsVisualizer/
├── src/main/java/com/newsvisualizer/
│   ├── NewsVisualizerApp.java          # Main application entry point
│   ├── model/                          # Data models
│   │   ├── NewsArticle.java
│   │   ├── Source.java
│   │   └── NewsResponse.java
│   ├── service/                        # External service integration
│   │   └── NewsApiService.java
│   ├── utils/                          # Utility classes
│   │   └── NewsAnalyzer.java
│   ├── visualization/                  # Chart generation
│   │   └── ChartGenerator.java
│   └── gui/                           # User interface
│       └── MainWindow.java
├── pom.xml                            # Maven configuration
└── README.md                          # This file
```

## Dependencies

- **Apache HttpClient**: For HTTP requests to news APIs
- **Jackson**: For JSON parsing
- **JFreeChart**: For data visualization and charting
- **Apache Commons Lang**: Utility functions
- **SLF4J + Logback**: Logging
- **JUnit**: Unit testing

## Configuration

### API Configuration
Edit `NewsApiService.java` to configure:
- API endpoints
- API keys
- Request parameters
- Timeout settings

### Analysis Configuration
Edit `NewsAnalyzer.java` to modify:
- Sentiment analysis word lists
- Keyword extraction parameters
- Stop words for filtering

## Troubleshooting

### No Articles Found
- Check your internet connection
- Verify API key is valid and configured
- Try different search terms or categories
- Check API quota limits

### Charts Not Displaying
- Ensure JFreeChart dependency is properly loaded
- Check console for any error messages
- Verify data is being fetched successfully

### Build Issues
- Ensure Java 11+ is installed
- Verify Maven is properly configured
- Run `mvn clean` to clear any cached artifacts

## Future Enhancements

- Support for additional news APIs
- Advanced sentiment analysis using machine learning
- Export functionality for charts and data
- Real-time news updates
- More visualization types (word clouds, network graphs)
- Database storage for historical analysis
- Web interface option

## License

This project is created for educational purposes.

## Contributing

Feel free to submit issues and enhancement requests!# NewsVisualizer
