@echo off
title News Visualizer - Quick Launch
color 0E

echo.
echo =====================================
echo    NEWS VISUALIZER - QUICK LAUNCH
echo =====================================
echo.

REM Check if Java is available
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java 11 or higher and try again.
    pause
    exit /b 1
)

REM Check if target directory exists (Maven compilation output)
if exist "target\classes" (
    echo Launching from compiled classes...
    echo.
    
    REM Try to run from Maven compiled classes with all dependencies
    if exist "target\news-visualizer-1.0.0.jar" (
        echo Running from packaged JAR...
        java -jar target\news-visualizer-1.0.0.jar
    ) else (
        echo Note: This launcher requires the project to be compiled first.
        echo Please run 'mvn clean package' or use 'run.bat' instead.
        echo.
        echo Attempting to run with Maven...
        where mvn >nul 2>nul
        if %ERRORLEVEL% EQU 0 (
            mvn exec:java -Dexec.mainClass="com.newsvisualizer.NewsVisualizerApp" -q
        ) else (
            echo Maven not found. Please use an IDE or install Maven.
        )
    )
) else (
    echo Project not compiled yet.
    echo Please run one of these first:
    echo 1. setup.bat     (full setup wizard)
    echo 2. run.bat       (compile and run)
    echo 3. mvn clean package
    echo.
    echo Then you can use this quick launcher.
)

echo.
pause