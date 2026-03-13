@echo off
title News Visualizer - Setup and Installation
color 0B
cls

echo.
echo ========================================
echo    NEWS VISUALIZER - SETUP WIZARD
echo ========================================
echo.
echo This script will help you set up the News Visualizer application.
echo.
echo What this script does:
echo 1. Creates a desktop shortcut
echo 2. Checks system requirements
echo 3. Provides setup instructions
echo 4. Tests the installation
echo.
echo Press any key to continue or Ctrl+C to exit...
pause >nul

cls
echo.
echo [STEP 1] Creating desktop shortcut...
echo.

REM Create desktop shortcut using VBScript
cscript //nologo create_shortcut.vbs
if %ERRORLEVEL% EQU 0 (
    echo [OK] Desktop shortcut created successfully!
) else (
    echo [WARNING] Could not create desktop shortcut automatically.
    echo You can run create_shortcut.vbs manually later.
)

echo.
echo [STEP 2] Checking system requirements...
echo.

REM Check Java
echo Checking Java installation...
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [MISSING] Java is not installed or not in PATH
    echo.
    echo REQUIRED: Please install Java 11 or higher
    echo Download from: https://adoptium.net/
    set JAVA_MISSING=1
) else (
    echo [OK] Java is installed
    java -version
)

echo.

REM Check Maven
echo Checking Maven installation...
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [MISSING] Maven is not installed or not in PATH
    echo.
    echo REQUIRED: Please install Apache Maven
    echo Download from: https://maven.apache.org/download.cgi
    echo Installation guide: https://maven.apache.org/install.html
    set MAVEN_MISSING=1
) else (
    echo [OK] Maven is installed
    mvn --version
)

echo.
echo [STEP 3] Setup instructions and API key configuration...
echo.
echo IMPORTANT: To use real news data, you need a NewsAPI key:
echo.
echo 1. Visit: https://newsapi.org
echo 2. Register for a free account
echo 3. Get your API key
echo 4. Edit: src\main\java\com\newsvisualizer\service\NewsApiService.java
echo 5. Replace "YOUR_API_KEY_HERE" with your actual API key
echo.

if defined JAVA_MISSING (
    echo [ACTION REQUIRED] Install Java first, then run this setup again.
    goto :end
)

if defined MAVEN_MISSING (
    echo [ACTION REQUIRED] Install Maven first, then run this setup again.
    goto :end
)

echo [STEP 4] Testing installation...
echo.
echo Attempting to compile the project (this may take a few minutes)...

mvn clean compile -q
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Project compiled successfully!
    echo.
    echo SETUP COMPLETE!
    echo.
    echo You can now:
    echo 1. Double-click "News Visualizer" shortcut on your desktop
    echo 2. Or run "run.bat" from this folder
    echo 3. Or use your favorite IDE to open this Maven project
    echo.
    echo Don't forget to configure your NewsAPI key for full functionality!
) else (
    echo [ERROR] Compilation failed.
    echo This might be due to:
    echo - Internet connection issues (Maven needs to download dependencies)
    echo - Java version compatibility issues
    echo.
    echo Try running: mvn clean compile
    echo to see detailed error messages.
)

:end
echo.
echo Setup wizard completed.
echo.
echo Press any key to exit...
pause >nul