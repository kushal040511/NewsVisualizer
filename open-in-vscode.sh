#!/bin/bash

# News Visualizer - VS Code launcher script for Unix-based systems (macOS/Linux)

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

echo
print_color $CYAN "========================================"
print_color $CYAN "  OPENING NEWS VISUALIZER IN VS CODE"
print_color $CYAN "========================================"
echo

# Check if VS Code is available
if ! command -v code &> /dev/null; then
    print_color $RED "VS Code command 'code' not found in PATH."
    echo
    echo "Please ensure VS Code is installed and:"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "1. Open VS Code"
        echo "2. Press Cmd+Shift+P"
        echo "3. Type 'Shell Command: Install code command in PATH'"
        echo "4. Restart your terminal and try again"
    else
        echo "1. Install VS Code from: https://code.visualstudio.com/"
        echo "2. Or install via package manager:"
        echo "   - Ubuntu/Debian: sudo snap install code --classic"
        echo "   - Arch Linux: sudo pacman -S code"
    fi
    echo
    echo "Alternative: Manually open VS Code and use File > Open Folder"
    read -p "Press Enter to exit..."
    exit 1
fi

echo "Opening News Visualizer project in VS Code..."
echo

# Try to open the workspace file first, then fallback to folder
if [ -f "NewsVisualizer.code-workspace" ]; then
    echo "Opening workspace file..."
    code NewsVisualizer.code-workspace
else
    echo "Opening project folder..."
    code .
fi

echo
print_color $GREEN "VS Code should now be opening with the News Visualizer project."
echo
echo "What to do next:"
echo "1. Install recommended extensions when prompted"
echo "2. Wait for Java extension to load the project"
echo "3. Press F5 to run the application"
echo "4. Or use Cmd+Shift+P (macOS) / Ctrl+Shift+P (Linux) and type 'Tasks: Run Task'"
echo
echo "For detailed instructions, see VSCODE-SETUP.md"
echo
read -p "Press Enter to exit..."