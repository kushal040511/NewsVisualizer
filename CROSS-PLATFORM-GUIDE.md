# News Visualizer - Cross-Platform Guide

This guide covers running the News Visualizer application on Windows, macOS, and Linux.

## 🌍 Platform Support

✅ **Windows 10/11** - Native batch scripts and Python launcher  
✅ **macOS** - Shell scripts and Python launcher  
✅ **Linux** - Shell scripts and Python launcher (Ubuntu, Debian, CentOS, Arch, etc.)

## 📋 Prerequisites by Platform

### Windows
```powershell
# Java 11+ (Required)
winget install EclipseAdoptium.Temurin.11

# Maven (Required)
winget install Apache.Maven

# Python 3 (Optional - for universal launcher)
winget install Python.Python.3
```

### macOS
```bash
# Using Homebrew (recommended)
brew install openjdk@11
brew install maven
brew install python3

# Or download directly:
# Java: https://adoptium.net/
# Maven: https://maven.apache.org/download.cgi
```

### Linux (Ubuntu/Debian)
```bash
# Update package list
sudo apt update

# Install Java and Maven
sudo apt install openjdk-11-jdk maven python3

# Verify installations
java -version
mvn --version
python3 --version
```

### Linux (CentOS/RHEL/Fedora)
```bash
# Using yum/dnf
sudo yum install java-11-openjdk-devel maven python3
# OR
sudo dnf install java-11-openjdk-devel maven python3
```

### Linux (Arch)
```bash
# Using pacman
sudo pacman -S jdk11-openjdk maven python
```

## 🚀 Running the Application

### Option 1: Platform-Specific Scripts

**Windows:**
```batch
# Double-click or run in Command Prompt/PowerShell
setup.bat                    # First-time setup
run.bat                      # Main launcher
quick-launch.bat            # Quick launcher
open-in-vscode.bat          # Open in VS Code
```

**macOS/Linux:**
```bash
# Make scripts executable (first time only)
chmod +x *.sh

# Run the application
./setup.sh                  # First-time setup
./run.sh                    # Main launcher
./open-in-vscode.sh         # Open in VS Code
```

### Option 2: Universal Python Launcher

**All Platforms:**
```bash
python3 launch.py           # Universal launcher
```
or on Windows:
```cmd
python launch.py
```

### Option 3: Make Commands (if GNU Make is installed)

**All Platforms:**
```bash
make help                   # Show available commands
make setup                  # Run setup wizard
make run                    # Compile and run
make vscode                 # Open in VS Code
make test                   # Run unit tests
make package                # Create JAR file
make clean                  # Clean build files
```

### Option 4: Direct Maven Commands

**All Platforms:**
```bash
# Compile and run
mvn clean compile
mvn exec:java -Dexec.mainClass="com.newsvisualizer.NewsVisualizerApp"

# Create standalone JAR
mvn clean package
java -jar target/news-visualizer-1.0.0.jar
```

## 🛠 Development Setup by Platform

### Windows Development
**Recommended IDEs:**
- IntelliJ IDEA Community
- Eclipse IDE
- VS Code with Java extensions

**Installation:**
```powershell
# Install development tools
winget install JetBrains.IntelliJ.Community
winget install Microsoft.VisualStudioCode
winget install Git.Git
```

### macOS Development
**Recommended IDEs:**
- IntelliJ IDEA Community
- VS Code with Java extensions
- Eclipse IDE

**Installation:**
```bash
# Using Homebrew
brew install --cask intellij-idea-ce
brew install --cask visual-studio-code
brew install git
```

### Linux Development
**Ubuntu/Debian:**
```bash
# Install development tools
sudo apt install git
sudo snap install intellij-idea-community --classic
sudo snap install code --classic

# Or download directly from websites
```

## 📁 File Structure by Platform

### Windows Files
```
├── run.bat                 # Windows launcher
├── setup.bat               # Windows setup wizard  
├── quick-launch.bat        # Windows quick launcher
├── open-in-vscode.bat     # Windows VS Code launcher
├── create_shortcut.vbs    # Desktop shortcut creator
└── create_shortcut.ps1    # PowerShell shortcut creator
```

### Unix Files (macOS/Linux)
```
├── run.sh                 # Unix launcher
├── setup.sh               # Unix setup wizard
├── open-in-vscode.sh     # Unix VS Code launcher
```

### Universal Files
```
├── launch.py              # Python universal launcher
├── Makefile               # Cross-platform Make commands
├── NewsVisualizer.code-workspace  # VS Code workspace
└── .vscode/               # VS Code configuration
```

## 🔧 Troubleshooting by Platform

### Windows Issues

**Java/Maven not found:**
```powershell
# Check if installed
java -version
mvn --version

# Add to PATH if needed
# System Properties > Environment Variables > PATH
# Add: C:\Program Files\Java\jdk-11\bin
# Add: C:\Program Files\Apache\maven\bin
```

**PowerShell execution policy:**
```powershell
# If scripts won't run
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### macOS Issues

**Command not found errors:**
```bash
# Install Command Line Tools
xcode-select --install

# Check PATH
echo $PATH

# Add to PATH in ~/.zshrc or ~/.bash_profile
export PATH="/opt/homebrew/bin:$PATH"
```

**Permission denied:**
```bash
# Make scripts executable
chmod +x *.sh
```

### Linux Issues

**Java not found:**
```bash
# Check Java installation
which java
java -version

# Set JAVA_HOME if needed
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64' >> ~/.bashrc
```

**Maven not found:**
```bash
# Install Maven
sudo apt install maven  # Ubuntu/Debian
sudo yum install maven  # CentOS/RHEL
sudo pacman -S maven    # Arch

# Or download and extract manually
```

## 🎯 Quick Start by Platform

### Windows Quick Start
1. **Install prerequisites:** Java 11+ and Maven
2. **Run:** Double-click `setup.bat`
3. **Launch:** Double-click desktop shortcut or `run.bat`

### macOS Quick Start
1. **Install prerequisites:** `brew install openjdk@11 maven`
2. **Setup:** `chmod +x *.sh && ./setup.sh`
3. **Launch:** `./run.sh`

### Linux Quick Start
1. **Install prerequisites:** `sudo apt install openjdk-11-jdk maven`
2. **Setup:** `chmod +x *.sh && ./setup.sh`
3. **Launch:** `./run.sh`

### Universal Quick Start (Any Platform)
1. **Install:** Java 11+, Maven, Python 3
2. **Run:** `python3 launch.py`

## 📱 Platform-Specific Features

### Windows
- Desktop shortcut creation
- Windows-style colored console output
- PowerShell and Command Prompt support
- Windows path handling

### macOS
- Homebrew integration
- Native macOS terminal colors
- Support for both Intel and Apple Silicon
- macOS-style keyboard shortcuts in VS Code

### Linux
- Package manager integration (apt, yum, pacman)
- Distribution-specific instructions
- Native shell integration
- X11/Wayland GUI support

## 🔗 Additional Resources

- [Java Installation Guide](https://adoptium.net/)
- [Maven Installation Guide](https://maven.apache.org/install.html)
- [VS Code Java Tutorial](https://code.visualstudio.com/docs/java/java-tutorial)
- [Cross-platform Java Development](https://docs.oracle.com/javase/tutorial/)

---

**Need help?** Check the main [README.md](README.md) or [VS Code Setup Guide](VSCODE-SETUP.md) for more detailed information.