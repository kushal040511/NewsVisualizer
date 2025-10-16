#!/bin/bash

# News Visualizer - Setup script for Unix-based systems (macOS/Linux)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_color() {
    printf "${1}${2}${NC}\n"
}

# Clear screen and show header
clear
echo
print_color $CYAN "========================================"
print_color $CYAN "   NEWS VISUALIZER - SETUP WIZARD"
print_color $CYAN "========================================"
echo
echo "This script will help you set up the News Visualizer application."
echo
echo "What this script does:"
echo "1. Checks system requirements"
echo "2. Provides setup instructions"
echo "3. Tests the installation"
echo "4. Creates launch shortcuts"
echo
read -p "Press Enter to continue or Ctrl+C to exit..."

clear
echo
print_color $YELLOW "[STEP 1] Checking system requirements..."
echo

# Check operating system
OS="$(uname -s)"
case "${OS}" in
    Linux*)     MACHINE=Linux;;
    Darwin*)    MACHINE=Mac;;
    CYGWIN*)    MACHINE=Cygwin;;
    MINGW*)     MACHINE=MinGw;;
    *)          MACHINE="UNKNOWN:${OS}"
esac
print_color $GREEN "Operating System: $MACHINE"

# Check Java
echo
echo "Checking Java installation..."
if command -v java &> /dev/null; then
    print_color $GREEN "[OK] Java is installed"
    java -version
    JAVA_MISSING=false
else
    print_color $RED "[MISSING] Java is not installed or not in PATH"
    echo
    echo "REQUIRED: Please install Java 11 or higher"
    if [ "$MACHINE" = "Mac" ]; then
        echo "Install with Homebrew: brew install openjdk@11"
        echo "Or download from: https://adoptium.net/"
    elif [ "$MACHINE" = "Linux" ]; then
        echo "Ubuntu/Debian: sudo apt install openjdk-11-jdk"
        echo "CentOS/RHEL: sudo yum install java-11-openjdk-devel"
        echo "Or download from: https://adoptium.net/"
    fi
    JAVA_MISSING=true
fi

echo

# Check Maven
echo "Checking Maven installation..."
if command -v mvn &> /dev/null; then
    print_color $GREEN "[OK] Maven is installed"
    mvn --version
    MAVEN_MISSING=false
else
    print_color $RED "[MISSING] Maven is not installed or not in PATH"
    echo
    echo "REQUIRED: Please install Apache Maven"
    if [ "$MACHINE" = "Mac" ]; then
        echo "Install with Homebrew: brew install maven"
    elif [ "$MACHINE" = "Linux" ]; then
        echo "Ubuntu/Debian: sudo apt install maven"
        echo "CentOS/RHEL: sudo yum install maven"
    fi
    echo "Or download from: https://maven.apache.org/download.cgi"
    echo "Installation guide: https://maven.apache.org/install.html"
    MAVEN_MISSING=true
fi

echo
print_color $YELLOW "[STEP 2] Setup instructions and API key configuration..."
echo
echo "IMPORTANT: To use real news data, you need a NewsAPI key:"
echo
echo "1. Visit: https://newsapi.org"
echo "2. Register for a free account"
echo "3. Get your API key"
echo "4. Edit: src/main/java/com/newsvisualizer/service/NewsApiService.java"
echo "5. Replace \"YOUR_API_KEY_HERE\" with your actual API key"
echo

if [ "$JAVA_MISSING" = true ]; then
    print_color $RED "[ACTION REQUIRED] Install Java first, then run this setup again."
    echo
    read -p "Press Enter to exit..."
    exit 1
fi

if [ "$MAVEN_MISSING" = true ]; then
    print_color $RED "[ACTION REQUIRED] Install Maven first, then run this setup again."
    echo
    read -p "Press Enter to exit..."
    exit 1
fi

print_color $YELLOW "[STEP 3] Testing installation..."
echo
echo "Attempting to compile the project (this may take a few minutes)..."

if mvn clean compile -q; then
    print_color $GREEN "[SUCCESS] Project compiled successfully!"
    echo
    
    print_color $YELLOW "[STEP 4] Creating launch shortcuts..."
    echo
    
    # Make shell scripts executable
    chmod +x run.sh
    chmod +x setup.sh
    
    if [ "$MACHINE" = "Mac" ]; then
        # Create macOS app bundle (optional)
        echo "Making shell scripts executable..."
        print_color $GREEN "[OK] Shell scripts are now executable"
        echo
        echo "You can now run the application with:"
        echo "  ./run.sh"
    else
        echo "Making shell scripts executable..."
        print_color $GREEN "[OK] Shell scripts are now executable"
        echo
        echo "You can now run the application with:"
        echo "  ./run.sh"
    fi
    
    echo
    print_color $GREEN "SETUP COMPLETE!"
    echo
    echo "You can now:"
    echo "1. Run './run.sh' from this directory"
    echo "2. Or use your favorite IDE to open this Maven project"
    echo "3. Or use VS Code with the provided workspace file"
    echo
    echo "Don't forget to configure your NewsAPI key for full functionality!"
else
    print_color $RED "[ERROR] Compilation failed."
    echo "This might be due to:"
    echo "- Internet connection issues (Maven needs to download dependencies)"
    echo "- Java version compatibility issues"
    echo
    echo "Try running: mvn clean compile"
    echo "to see detailed error messages."
fi

echo
echo "Setup wizard completed."
echo
read -p "Press Enter to exit..."