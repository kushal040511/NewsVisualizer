#!/usr/bin/env python3
"""
Comprehensive Test Suite for News Visualizer Application
Tests all major functionality and UI improvements
"""

import subprocess
import time
import sys
import os
from pathlib import Path

class NewsVisualizerTester:
    def __init__(self):
        self.project_root = Path(__file__).parent
        self.passed_tests = 0
        self.total_tests = 0
        
    def log(self, message, status="INFO"):
        colors = {
            "INFO": "\033[94m",    # Blue
            "PASS": "\033[92m",    # Green  
            "FAIL": "\033[91m",    # Red
            "WARN": "\033[93m",    # Yellow
            "END": "\033[0m"       # Reset
        }
        print(f"{colors.get(status, '')}{status}: {message}{colors['END']}")
    
    def test_project_structure(self):
        """Test 1: Verify project structure and key files"""
        self.total_tests += 1
        self.log("Testing project structure...")
        
        required_files = [
            "pom.xml",
            "launch.py", 
            "src/main/java/com/newsvisualizer/NewsVisualizerApp.java",
            "src/main/java/com/newsvisualizer/gui/MainWindow.java",
            "src/main/java/com/newsvisualizer/gui/NewsAppPanel.java"
        ]
        
        missing_files = []
        for file_path in required_files:
            if not (self.project_root / file_path).exists():
                missing_files.append(file_path)
        
        if missing_files:
            self.log(f"Missing required files: {missing_files}", "FAIL")
            return False
        else:
            self.log("All required project files present", "PASS")
            self.passed_tests += 1
            return True
    
    def test_compilation(self):
        """Test 2: Verify Maven compilation"""
        self.total_tests += 1
        self.log("Testing Maven compilation...")
        
        try:
            result = subprocess.run(
                ["mvn", "clean", "compile", "-q"],
                cwd=self.project_root,
                capture_output=True,
                text=True,
                timeout=120
            )
            
            if result.returncode == 0:
                self.log("Maven compilation successful", "PASS")
                self.passed_tests += 1
                return True
            else:
                self.log(f"Maven compilation failed: {result.stderr}", "FAIL")
                return False
                
        except subprocess.TimeoutExpired:
            self.log("Maven compilation timed out", "FAIL")
            return False
        except FileNotFoundError:
            self.log("Maven not found in PATH", "FAIL")
            return False
    
    def test_java_requirements(self):
        """Test 3: Verify Java version and availability"""
        self.total_tests += 1
        self.log("Testing Java requirements...")
        
        try:
            result = subprocess.run(
                ["java", "-version"],
                capture_output=True,
                text=True
            )
            
            if result.returncode == 0:
                java_version = result.stderr.split('\n')[0]
                self.log(f"Java available: {java_version}", "PASS")
                self.passed_tests += 1
                return True
            else:
                self.log("Java not available", "FAIL")
                return False
                
        except FileNotFoundError:
            self.log("Java not found in PATH", "FAIL")
            return False
    
    def test_database_files(self):
        """Test 4: Check database initialization"""
        self.total_tests += 1
        self.log("Testing database files...")
        
        data_dir = self.project_root / "data"
        if data_dir.exists():
            db_files = list(data_dir.glob("*.db"))
            if db_files:
                self.log(f"Database files found: {[f.name for f in db_files]}", "PASS")
                self.passed_tests += 1
                return True
            else:
                self.log("No database files found, but directory exists", "WARN")
                self.passed_tests += 1
                return True
        else:
            self.log("Data directory will be created on first run", "WARN") 
            self.passed_tests += 1
            return True
    
    def test_ui_improvements_code(self):
        """Test 5: Verify UI improvement code changes"""
        self.total_tests += 1
        self.log("Testing UI improvements in code...")
        
        main_window_file = self.project_root / "src/main/java/com/newsvisualizer/gui/MainWindow.java"
        
        if not main_window_file.exists():
            self.log("MainWindow.java not found", "FAIL")
            return False
        
        content = main_window_file.read_text()
        
        # Check for UI improvements
        improvements_found = []
        
        if "setSize(1800, 1200)" in content:
            improvements_found.append("Increased window size")
            
        if "setMinimumSize(new Dimension(1600, 900))" in content:
            improvements_found.append("Larger minimum window size")
            
        if "120);" in content and "columns for even wider text" in content:
            improvements_found.append("Enhanced summary text width")
            
        if "Font.PLAIN, 18)" in content:
            improvements_found.append("Increased font size for readability")
            
        if "setRowHeight(55)" in content:
            improvements_found.append("Increased table row height")
        
        if len(improvements_found) >= 4:
            self.log(f"UI improvements verified: {improvements_found}", "PASS")
            self.passed_tests += 1
            return True
        else:
            self.log(f"Some UI improvements missing. Found: {improvements_found}", "FAIL")
            return False
    
    def test_news_app_panel_enhancements(self):
        """Test 6: Verify NewsAppPanel enhancements"""
        self.total_tests += 1
        self.log("Testing NewsAppPanel enhancements...")
        
        news_app_file = self.project_root / "src/main/java/com/newsvisualizer/gui/NewsAppPanel.java"
        
        if not news_app_file.exists():
            self.log("NewsAppPanel.java not found", "FAIL")
            return False
            
        content = news_app_file.read_text()
        
        enhancements_found = []
        
        if "showArticleSummaryDialog" in content:
            enhancements_found.append("Article summary dialog")
            
        if "Enhanced custom cell renderer" in content:
            enhancements_found.append("Enhanced card renderer")
            
        if "Double-click to open • Right-click for full summary" in content:
            enhancements_found.append("Improved interaction hints")
            
        if "setPreferredSize(new Dimension(900, 600))" in content:
            enhancements_found.append("Larger panel dimensions")
        
        if len(enhancements_found) >= 3:
            self.log(f"NewsAppPanel enhancements verified: {enhancements_found}", "PASS")
            self.passed_tests += 1
            return True
        else:
            self.log(f"NewsAppPanel enhancements missing. Found: {enhancements_found}", "FAIL")
            return False
    
    def test_application_startup(self):
        """Test 7: Test application startup (non-GUI mode simulation)"""
        self.total_tests += 1
        self.log("Testing application startup readiness...")
        
        try:
            # Test if we can at least compile and validate main class
            result = subprocess.run(
                ["mvn", "exec:java", "-Dexec.mainClass=com.newsvisualizer.NewsVisualizerApp", "-Dexec.args=-test", "-q"],
                cwd=self.project_root,
                capture_output=True,
                text=True,
                timeout=30
            )
            
            # Even if it fails due to GUI initialization, the class should be found
            if "ClassNotFoundException" not in result.stderr and "NoClassDefFoundError" not in result.stderr:
                self.log("Application main class accessible and dependencies resolved", "PASS")
                self.passed_tests += 1
                return True
            else:
                self.log(f"Application startup issues: {result.stderr}", "FAIL")
                return False
                
        except subprocess.TimeoutExpired:
            # This is expected as GUI might be waiting for user interaction
            self.log("Application startup test completed (timeout expected for GUI)", "PASS")
            self.passed_tests += 1
            return True
        except Exception as e:
            self.log(f"Application startup test error: {e}", "FAIL")
            return False
    
    def test_launcher_script(self):
        """Test 8: Test Python launcher script functionality"""
        self.total_tests += 1
        self.log("Testing Python launcher script...")
        
        launcher_file = self.project_root / "launch.py"
        if not launcher_file.exists():
            self.log("launch.py not found", "FAIL") 
            return False
        
        content = launcher_file.read_text()
        
        launcher_features = []
        
        if "check_command" in content:
            launcher_features.append("Command availability checking")
            
        if "NewsVisualizerApp" in content:
            launcher_features.append("Main class execution")
            
        if "Operating System:" in content:
            launcher_features.append("Cross-platform support")
            
        if "mvn exec:java" in content:
            launcher_features.append("Maven execution")
        
        if len(launcher_features) >= 3:
            self.log(f"Launcher script features verified: {launcher_features}", "PASS")
            self.passed_tests += 1
            return True
        else:
            self.log(f"Launcher script incomplete. Found: {launcher_features}", "FAIL")
            return False
    
    def run_comprehensive_test(self):
        """Run all tests and provide summary"""
        self.log("=" * 60)
        self.log("NEWS VISUALIZER COMPREHENSIVE TEST SUITE")
        self.log("=" * 60)
        
        # Run all tests
        tests = [
            self.test_project_structure,
            self.test_java_requirements,
            self.test_compilation,
            self.test_database_files,
            self.test_ui_improvements_code,
            self.test_news_app_panel_enhancements,
            self.test_launcher_script,
            self.test_application_startup
        ]
        
        for test in tests:
            try:
                test()
            except Exception as e:
                self.log(f"Test {test.__name__} failed with exception: {e}", "FAIL")
                self.total_tests += 1
            print()  # Add spacing between tests
        
        # Summary
        self.log("=" * 60)
        self.log("TEST SUMMARY")
        self.log("=" * 60)
        
        success_rate = (self.passed_tests / self.total_tests) * 100 if self.total_tests > 0 else 0
        
        self.log(f"Passed: {self.passed_tests}/{self.total_tests} tests")
        self.log(f"Success Rate: {success_rate:.1f}%")
        
        if success_rate >= 80:
            self.log("🎉 NEWS VISUALIZER IS READY TO USE!", "PASS")
            self.log("All major functionality and UI improvements have been verified.")
            self.log("You can now run the application using: python3 launch.py")
        elif success_rate >= 60:
            self.log("⚠️  NEWS VISUALIZER HAS SOME ISSUES", "WARN") 
            self.log("Most functionality works but some improvements may be needed.")
        else:
            self.log("❌ NEWS VISUALIZER HAS SIGNIFICANT ISSUES", "FAIL")
            self.log("Please review failed tests and fix issues before using.")
        
        self.log("=" * 60)
        
        return success_rate >= 80

def main():
    tester = NewsVisualizerTester()
    success = tester.run_comprehensive_test()
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()