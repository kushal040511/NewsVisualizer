# News Visualizer - Quick Start Reference 🚀

## One-Command Launch by Platform

### Windows
```batch
setup.bat        # First time setup + run
run.bat          # Main launcher
```

### macOS
```bash
chmod +x *.sh && ./setup.sh    # First time
./run.sh                       # Main launcher
```

### Linux  
```bash
chmod +x *.sh && ./setup.sh    # First time
./run.sh                       # Main launcher
```

### Universal (Any OS with Python 3)
```bash
python3 launch.py              # Works everywhere
```

## Installation Commands

### Windows (with winget)
```powershell
winget install EclipseAdoptium.Temurin.11
winget install Apache.Maven
```

### macOS (with Homebrew)
```bash
brew install openjdk@11 maven
```

### Ubuntu/Debian Linux
```bash
sudo apt install openjdk-11-jdk maven
```

### CentOS/RHEL/Fedora Linux
```bash
sudo dnf install java-11-openjdk-devel maven
```

## All Available Launchers

| Platform | Script | Description |
|----------|--------|-------------|
| Windows | `setup.bat` | Complete setup wizard |
| Windows | `run.bat` | Main launcher with checks |
| Windows | `quick-launch.bat` | Fast launcher |
| Windows | `open-in-vscode.bat` | Open in VS Code |
| macOS/Linux | `setup.sh` | Complete setup wizard |
| macOS/Linux | `run.sh` | Main launcher with checks |  
| macOS/Linux | `open-in-vscode.sh` | Open in VS Code |
| Universal | `launch.py` | Python cross-platform |
| Any | `make run` | Make command (if installed) |

## Quick Troubleshooting

**Java not found?**
- Windows: `java -version` in Command Prompt
- macOS: `brew install openjdk@11`  
- Linux: `sudo apt install openjdk-11-jdk`

**Maven not found?**
- Windows: `mvn --version` in Command Prompt
- macOS: `brew install maven`
- Linux: `sudo apt install maven`

**Permission denied on macOS/Linux?**
```bash
chmod +x *.sh
```

**Need more help?**
- Read [CROSS-PLATFORM-GUIDE.md](CROSS-PLATFORM-GUIDE.md)
- Check [README.md](README.md)
- See [VSCODE-SETUP.md](VSCODE-SETUP.md) for VS Code

## 🎯 30-Second Start

1. **Install**: Java 11+ and Maven for your OS
2. **Run**: Appropriate setup script for your platform  
3. **Launch**: Use the main launcher or desktop shortcut
4. **Configure**: Add your NewsAPI key (optional)
5. **Enjoy**: Analyze and visualize news data!

---
*Works on Windows 10/11, macOS, and all major Linux distributions*