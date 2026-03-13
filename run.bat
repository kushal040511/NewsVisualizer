@echo off
title News Visualizer Launcher
color 0A
echo.
echo =====================================
echo    NEWS VISUALIZER APPLICATION
echo =====================================
echo.
echo Starting News Visualizer Application...
echo.

REM Check if Java is available
echo [1/3] Checking Java installation...
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Java is not installed or not in PATH
    echo.
    echo Please install Java 11 or higher:
    echo - Download from: https://adoptium.net/
    echo - Or use Oracle JDK: https://www.oracle.com/java/technologies/downloads/
    echo.
    echo After installation, restart your computer and try again.
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Java found
)

REM Check if Maven is available
echo [2/3] Checking Maven installation...
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Maven is not installed or not in PATH
    echo.
    echo Please install Maven:
    echo - Download from: https://maven.apache.org/download.cgi
    echo - Follow installation guide: https://maven.apache.org/install.html
    echo.
    echo Alternative ways to run the project:
    echo 1. Use IntelliJ IDEA or Eclipse IDE
    echo 2. Import as Maven project
    echo 3. Run main class: com.newsvisualizer.NewsVisualizerApp
    echo.
    echo Or install Maven and restart your computer, then try again.
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Maven found
)

REM Compile and run the project
echo [3/3] Compiling and launching application...
echo.
echo Please wait while the project compiles...
mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    echo.
    echo This might be due to:
    echo - Missing dependencies
    echo - Internet connection issues
    echo - Java version compatibility
    echo.
    echo Try running: mvn clean compile
    echo to see detailed error messages.
    echo.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Project compiled successfully!
echo.
echo Launching News Visualizer GUI...
echo.
echo Note: Don't forget to configure your NewsAPI key in:
echo src/main/java/com/newsvisualizer/service/NewsApiService.java
echo.
echo The application window should open shortly...
echo.

REM Run the application
mvn exec:java -Dexec.mainClass="com.newsvisualizer.NewsVisualizerApp" -q

echo.
echo Application has been closed.
echo.
pause
