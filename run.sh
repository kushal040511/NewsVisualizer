#!/bin/bash

# News Visualizer - Cross-platform launcher script for Unix-based systems (macOS/Linux)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_color() {
    printf "${1}${2}${NC}\n"
}

# Clear screen and show header
clear
echo
print_color $BLUE "====================================="
print_color $BLUE "   NEWS VISUALIZER APPLICATION"
print_color $BLUE "====================================="
echo
echo "Starting News Visualizer Application..."
echo

# Check if Java is available
print_color $YELLOW "[1/3] Checking Java installation..."
if ! command -v java &> /dev/null; then
    print_color $RED "[ERROR] Java is not installed or not in PATH"
    echo
    echo "Please install Java 11 or higher:"
    echo "- macOS: brew install openjdk@11"
    echo "- Linux: sudo apt install openjdk-11-jdk (Ubuntu/Debian)"
    echo "- Or download from: https://adoptium.net/"
    echo
    echo "After installation, restart your terminal and try again."
    echo
    read -p "Press Enter to exit..."
    exit 1
else
    print_color $GREEN "[OK] Java found"
    java -version
fi

echo

# Check if Maven is available
print_color $YELLOW "[2/3] Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    print_color $RED "[ERROR] Maven is not installed or not in PATH"
    echo
    echo "Please install Maven:"
    echo "- macOS: brew install maven"
    echo "- Linux: sudo apt install maven (Ubuntu/Debian)"
    echo "- Or download from: https://maven.apache.org/download.cgi"
    echo
    echo "Alternative ways to run the project:"
    echo "1. Use IntelliJ IDEA, Eclipse, or VS Code"
    echo "2. Import as Maven project"
    echo "3. Run main class: com.newsvisualizer.NewsVisualizerApp"
    echo
    echo "Or install Maven and restart your terminal, then try again."
    echo
    read -p "Press Enter to exit..."
    exit 1
else
    print_color $GREEN "[OK] Maven found"
    mvn --version
fi

echo

# Compile and run the project
print_color $YELLOW "[3/3] Compiling and launching application..."
echo
echo "Please wait while the project compiles..."

if mvn clean compile -q; then
    print_color $GREEN "[SUCCESS] Project compiled successfully!"
    echo
    echo "Launching News Visualizer GUI..."
    echo
    echo "Note: Don't forget to configure your NewsAPI key in:"
    echo "src/main/java/com/newsvisualizer/service/NewsApiService.java"
    echo
    echo "The application window should open shortly..."
    echo
    
    # Run the application
    mvn exec:java -Dexec.mainClass="com.newsvisualizer.NewsVisualizerApp" -q
else
    print_color $RED "[ERROR] Compilation failed!"
    echo
    echo "This might be due to:"
    echo "- Missing dependencies"
    echo "- Internet connection issues"
    echo "- Java version compatibility"
    echo
    echo "Try running: mvn clean compile"
    echo "to see detailed error messages."
    echo
    read -p "Press Enter to exit..."
    exit 1
fi

echo
echo "Application has been closed."
echo
read -p "Press Enter to exit..."