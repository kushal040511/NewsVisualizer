#!/usr/bin/env python3

"""
News Visualizer - Universal Cross-platform Launcher
Supports Windows, macOS, and Linux
"""

import os
import sys
import platform
import subprocess
import time
from pathlib import Path

# Colors for terminal output
class Colors:
    RED = '\033[91m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    WHITE = '\033[97m'
    BOLD = '\033[1m'
    END = '\033[0m'

    @staticmethod
    def print_colored(text, color):
        if platform.system() == "Windows":
            # Windows might not support ANSI colors in older versions
            print(text)
        else:
            print(f"{color}{text}{Colors.END}")

def clear_screen():
    """Clear the terminal screen"""
    os.system('cls' if platform.system() == 'Windows' else 'clear')

def check_command(command):
    """Check if a command is available in the system PATH"""
    try:
        subprocess.run([command, "--version" if command == "mvn" else "-version"], 
                      stdout=subprocess.DEVNULL, 
                      stderr=subprocess.DEVNULL,
                      check=True)
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        return False

def get_install_instructions(tool, os_name):
    """Get installation instructions for a tool based on OS"""
    instructions = {
        "java": {
            "Windows": [
                "Download from: https://adoptium.net/",
                "Or use: winget install EclipseAdoptium.Temurin.11"
            ],
            "Darwin": [
                "Install with Homebrew: brew install openjdk@11",
                "Or download from: https://adoptium.net/"
            ],
            "Linux": [
                "Ubuntu/Debian: sudo apt install openjdk-11-jdk",
                "CentOS/RHEL: sudo yum install java-11-openjdk-devel",
                "Or download from: https://adoptium.net/"
            ]
        },
        "maven": {
            "Windows": [
                "Download from: https://maven.apache.org/download.cgi",
                "Or use: winget install Apache.Maven"
            ],
            "Darwin": [
                "Install with Homebrew: brew install maven",
                "Or download from: https://maven.apache.org/download.cgi"
            ],
            "Linux": [
                "Ubuntu/Debian: sudo apt install maven",
                "CentOS/RHEL: sudo yum install maven",
                "Or download from: https://maven.apache.org/download.cgi"
            ]
        }
    }
    
    return instructions.get(tool, {}).get(os_name, ["Check the official documentation"])

def main():
    clear_screen()
    
    # Display header
    print()
    Colors.print_colored("=" * 50, Colors.BLUE)
    Colors.print_colored("       NEWS VISUALIZER APPLICATION", Colors.BLUE)
    Colors.print_colored("          Universal Launcher", Colors.CYAN)
    Colors.print_colored("=" * 50, Colors.BLUE)
    print()
    
    # Detect operating system
    os_name = platform.system()
    os_version = platform.release()
    arch = platform.machine()
    
    Colors.print_colored(f"Operating System: {os_name} {os_version} ({arch})", Colors.WHITE)
    Colors.print_colored(f"Python Version: {sys.version.split()[0]}", Colors.WHITE)
    print()
    
    # Check Java
    Colors.print_colored("[1/3] Checking Java installation...", Colors.YELLOW)
    java_available = check_command("java")
    
    if java_available:
        Colors.print_colored("[OK] Java found", Colors.GREEN)
        try:
            result = subprocess.run(["java", "-version"], 
                                  capture_output=True, text=True)
            print(result.stderr.split('\n')[0])  # Java version is in stderr
        except:
            pass
    else:
        Colors.print_colored("[ERROR] Java is not installed or not in PATH", Colors.RED)
        print()
        print("Please install Java 11 or higher:")
        for instruction in get_install_instructions("java", os_name):
            print(f"  - {instruction}")
        print()
        print("After installation, restart your terminal and try again.")
        input("Press Enter to exit...")
        return False
    
    print()
    
    # Check Maven
    Colors.print_colored("[2/3] Checking Maven installation...", Colors.YELLOW)
    maven_available = check_command("mvn")
    
    if maven_available:
        Colors.print_colored("[OK] Maven found", Colors.GREEN)
        try:
            result = subprocess.run(["mvn", "--version"], 
                                  capture_output=True, text=True)
            print(result.stdout.split('\n')[0])
        except:
            pass
    else:
        Colors.print_colored("[ERROR] Maven is not installed or not in PATH", Colors.RED)
        print()
        print("Please install Apache Maven:")
        for instruction in get_install_instructions("maven", os_name):
            print(f"  - {instruction}")
        print()
        print("Alternative ways to run the project:")
        print("  1. Use IntelliJ IDEA, Eclipse, or VS Code")
        print("  2. Import as Maven project")
        print("  3. Run main class: com.newsvisualizer.NewsVisualizerApp")
        print()
        input("Press Enter to exit...")
        return False
    
    print()
    
    # Compile and run
    Colors.print_colored("[3/3] Compiling and launching application...", Colors.YELLOW)
    print()
    print("Please wait while the project compiles...")
    
    try:
        # Compile the project
        compile_result = subprocess.run(
            ["mvn", "clean", "compile", "-q"],
            cwd=Path.cwd(),
            capture_output=True,
            text=True
        )
        
        if compile_result.returncode == 0:
            Colors.print_colored("[SUCCESS] Project compiled successfully!", Colors.GREEN)
            print()
            print("Launching News Visualizer GUI...")
            print()
            print("Note: Don't forget to configure your NewsAPI key in:")
            print("  src/main/java/com/newsvisualizer/service/NewsApiService.java")
            print()
            print("The application window should open shortly...")
            print()
            
            # Run the application
            run_result = subprocess.run([
                "mvn", "exec:java", 
                "-Dexec.mainClass=com.newsvisualizer.NewsVisualizerApp",
                "-q"
            ], cwd=Path.cwd())
            
            print()
            print("Application has been closed.")
            
        else:
            Colors.print_colored("[ERROR] Compilation failed!", Colors.RED)
            print()
            print("Error output:")
            print(compile_result.stderr)
            print()
            print("This might be due to:")
            print("  - Missing dependencies")
            print("  - Internet connection issues")
            print("  - Java version compatibility")
            print()
            print("Try running: mvn clean compile")
            print("to see detailed error messages.")
            
    except Exception as e:
        Colors.print_colored(f"[ERROR] Failed to run Maven: {str(e)}", Colors.RED)
    
    print()
    input("Press Enter to exit...")
    return True

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print()
        print("Operation cancelled by user.")
        sys.exit(0)
    except Exception as e:
        Colors.print_colored(f"Unexpected error: {str(e)}", Colors.RED)
        sys.exit(1)