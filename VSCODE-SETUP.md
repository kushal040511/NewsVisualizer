# News Visualizer - VS Code Setup Guide

This guide will help you set up and work with the News Visualizer project in Visual Studio Code.

## 🚀 Quick Start

### Method 1: Open Workspace File
1. **Double-click** `NewsVisualizer.code-workspace` to open the project in VS Code
2. VS Code will automatically configure the project settings

### Method 2: Open Folder
1. **Open VS Code**
2. **File → Open Folder** 
3. **Select** the `NewsVisualizer` folder
4. VS Code will detect it as a Java/Maven project

## 📋 Prerequisites

### Required Extensions
VS Code will automatically recommend these extensions when you open the project:

**Essential:**
- `Extension Pack for Java` (vscjava.vscode-java-pack)
- `Maven for Java` (vscjava.vscode-maven)
- `Language Support for Java` (redhat.java)

**Optional but Recommended:**
- `PowerShell` (ms-vscode.powershell)
- `Code Runner` (formulahendry.code-runner)

### Install Extensions
1. Open the project in VS Code
2. Press `Ctrl+Shift+P` and type "Extensions: Show Recommended Extensions"
3. Click "Install All" for workspace recommendations

## 🔧 Project Configuration

### Automatic Setup
The project includes pre-configured VS Code settings:

- **Java Source Paths**: `src/main/java`
- **Output Path**: `target/classes`
- **Maven Integration**: Enabled
- **Auto-formatting**: Enabled on save
- **Tab Size**: 4 spaces

### Manual Java Setup (if needed)
1. Press `Ctrl+Shift+P`
2. Type "Java: Configure Runtime"
3. Ensure Java 11+ is selected

## 🏃‍♂️ Running the Application

### Method 1: Debug/Run Button
1. **Open** `src/main/java/com/newsvisualizer/NewsVisualizerApp.java`
2. **Click** the "▶️ Run" button above the `main` method
3. **Or click** "🐛 Debug" for debugging mode

### Method 2: Launch Configuration
1. **Press** `F5` or go to **Run and Debug** panel (`Ctrl+Shift+D`)
2. **Select** "Run News Visualizer" from dropdown
3. **Click** the green play button

### Method 3: Tasks
1. **Press** `Ctrl+Shift+P`
2. **Type** "Tasks: Run Task"
3. **Choose** from available tasks:
   - `maven: run` - Compile and run
   - `maven: clean compile` - Clean build
   - `Run News Visualizer (Windows)` - Use batch script
   - `Setup Project` - Run setup wizard

### Method 4: Terminal
1. **Open Terminal** (`Ctrl+`` ` )
2. **Run commands**:
   ```bash
   # Compile and run
   mvn clean compile exec:java -Dexec.mainClass="com.newsvisualizer.NewsVisualizerApp"
   
   # Or use batch file
   .\run.bat
   ```

## 🔍 Debugging

### Setting Breakpoints
1. **Click** in the left margin next to line numbers to set breakpoints
2. **Press** `F5` to start debugging
3. **Use** debug controls to step through code

### Debug Configurations Available
- **Run News Visualizer**: Normal execution
- **Debug News Visualizer**: With debugging enabled
- **Run Tests**: Execute unit tests

## 🧪 Testing

### Running Tests
1. **Method 1**: Press `Ctrl+Shift+P` → "Java: Run Tests"
2. **Method 2**: Right-click on test file → "Run Tests"
3. **Method 3**: Use Task → "maven: test"

### Test Coverage
- Tests are located in `src/test/java/`
- VS Code will show test results in the Test Explorer

## 🛠 Building and Packaging

### Available Maven Tasks
Access via `Ctrl+Shift+P` → "Tasks: Run Task":

- **maven: clean** - Clean build artifacts
- **maven: compile** - Compile source code
- **maven: test** - Run unit tests
- **maven: package** - Create JAR file
- **maven: clean compile** - Clean and compile

### Build Shortcuts
- **Build**: `Ctrl+Shift+B` (default build task)
- **Quick Build**: `Ctrl+F5` (run without debugging)

## 📝 Code Features

### IntelliSense and Auto-completion
- **Auto-import**: Automatically adds import statements
- **Code completion**: `Ctrl+Space` for suggestions
- **Quick fixes**: `Ctrl+.` for error fixes
- **Rename**: `F2` to rename variables/methods

### Code Snippets
Type these prefixes and press `Tab`:
- `psvm` → `public static void main`
- `sout` → `System.out.println`
- `logger` → Logger instance
- `newsarticle` → NewsArticle constructor
- `try` → Try-catch block with logging
- `test` → JUnit test method template

### Formatting
- **Auto-format**: Saves automatically format code
- **Manual format**: `Shift+Alt+F`
- **Organize imports**: `Shift+Alt+O`

## 📁 Project Structure in VS Code

### Explorer Panel
The project structure will appear as:
```
📁 News Visualizer
├── 📁 src
│   ├── 📁 main/java/com/newsvisualizer
│   │   ├── 📄 NewsVisualizerApp.java (main class)
│   │   ├── 📁 gui
│   │   ├── 📁 model
│   │   ├── 📁 service
│   │   ├── 📁 utils
│   │   └── 📁 visualization
│   └── 📁 test/java
├── 📁 target (build output - hidden)
├── 📄 pom.xml
└── 📄 README.md
```

### Maven Panel
- **Dependencies**: View project dependencies
- **Lifecycle**: Run Maven phases
- **Plugins**: Execute Maven plugins

## ⚙️ Troubleshooting

### Common Issues

**1. Java Extension Not Working**
- Reload VS Code: `Ctrl+Shift+P` → "Developer: Reload Window"
- Check Java version: `Ctrl+Shift+P` → "Java: Configure Runtime"

**2. Maven Not Detected**
- Ensure Maven is in PATH
- Restart VS Code after installing Maven
- Check: `Terminal` → `mvn --version`

**3. Project Not Loading**
- Delete `.vscode/settings.json` and restart VS Code
- Run: `Java: Reload Projects` command

**4. Build Errors**
- Run: `maven: clean compile` task
- Check output panel for detailed errors

### Getting Help
1. **Command Palette**: `Ctrl+Shift+P` → Type your question
2. **Problems Panel**: `Ctrl+Shift+M` to see errors/warnings
3. **Output Panel**: Check "Java" channel for detailed logs

## 🎯 Tips for Productive Development

### Keyboard Shortcuts
- `Ctrl+Shift+E` - Explorer panel
- `Ctrl+Shift+D` - Debug panel
- `Ctrl+`` ` - Terminal
- `Ctrl+Shift+P` - Command palette
- `Ctrl+Shift+G` - Git panel
- `F12` - Go to definition
- `Ctrl+F12` - Go to implementation

### Multiple Terminals
- **New Terminal**: `Ctrl+Shift+`` ` 
- **Split Terminal**: `Ctrl+Shift+5`
- **Switch Terminal**: `Ctrl+PageUp/PageDown`

### Workspaces
- **Save Workspace**: `File → Save Workspace As`
- **Open Recent**: `File → Open Recent`

## 🔗 Useful VS Code Commands

| Command | Description |
|---------|-------------|
| `Java: Reload Projects` | Refresh Java project |
| `Java: Configure Runtime` | Set Java version |
| `Tasks: Run Task` | Execute predefined tasks |
| `Developer: Reload Window` | Restart VS Code |
| `Extensions: Show Recommended Extensions` | Install suggested extensions |

---

**Happy Coding! 🎉**

For more help, check the main [README.md](README.md) or the VS Code Java documentation.