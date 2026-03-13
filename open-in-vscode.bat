@echo off
title Open News Visualizer in VS Code
color 0D

echo.
echo ========================================
echo   OPENING NEWS VISUALIZER IN VS CODE
echo ========================================
echo.

REM Check if VS Code is available
where code >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo VS Code command 'code' not found in PATH.
    echo.
    echo Please ensure VS Code is installed and:
    echo 1. Open VS Code
    echo 2. Press Ctrl+Shift+P
    echo 3. Type "Shell Command: Install 'code' command in PATH"
    echo 4. Restart your terminal and try again
    echo.
    echo Alternative: Manually open VS Code and use File ^> Open Folder
    pause
    exit /b 1
)

echo Opening News Visualizer project in VS Code...
echo.

REM Try to open the workspace file first, then fallback to folder
if exist "NewsVisualizer.code-workspace" (
    echo Opening workspace file...
    code NewsVisualizer.code-workspace
) else (
    echo Opening project folder...
    code .
)

echo.
echo VS Code should now be opening with the News Visualizer project.
echo.
echo What to do next:
echo 1. Install recommended extensions when prompted
echo 2. Wait for Java extension to load the project
echo 3. Press F5 to run the application
echo 4. Or use Ctrl+Shift+P and type "Tasks: Run Task"
echo.
echo For detailed instructions, see VSCODE-SETUP.md
echo.
pause