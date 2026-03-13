# News Visualizer - Cross-platform Makefile
# Supports Windows, macOS, and Linux

# Default target
.PHONY: help
help:
	@echo "News Visualizer - Available commands:"
	@echo
	@echo "  make run       - Compile and run the application"
	@echo "  make compile   - Compile the project"
	@echo "  make clean     - Clean build artifacts"
	@echo "  make test      - Run unit tests"
	@echo "  make package   - Create JAR file"
	@echo "  make setup     - Run setup wizard"
	@echo "  make vscode    - Open in VS Code"
	@echo "  make install   - Install dependencies"
	@echo

# Detect operating system
UNAME_S := $(shell uname -s 2>/dev/null || echo "Windows")

# Set commands based on OS
ifeq ($(UNAME_S),Darwin)
    # macOS
    OPEN_CMD = open
    SHELL_EXT = .sh
endif
ifeq ($(UNAME_S),Linux)
    # Linux
    OPEN_CMD = xdg-open
    SHELL_EXT = .sh
endif
ifeq ($(UNAME_S),Windows_NT)
    # Windows (if Make is available)
    OPEN_CMD = start
    SHELL_EXT = .bat
endif
ifneq ($(findstring MINGW,$(UNAME_S)),)
    # Windows (MinGW/Git Bash)
    OPEN_CMD = start
    SHELL_EXT = .bat
endif

# Maven commands
MVN = mvn
MAIN_CLASS = com.newsvisualizer.NewsVisualizerApp

.PHONY: compile
compile:
	@echo "Compiling News Visualizer..."
	$(MVN) clean compile

.PHONY: run
run: compile
	@echo "Running News Visualizer..."
	$(MVN) exec:java -Dexec.mainClass="$(MAIN_CLASS)" -q

.PHONY: clean
clean:
	@echo "Cleaning build artifacts..."
	$(MVN) clean

.PHONY: test
test:
	@echo "Running tests..."
	$(MVN) test

.PHONY: package
package:
	@echo "Creating JAR package..."
	$(MVN) clean package

.PHONY: install
install:
	@echo "Installing dependencies..."
	$(MVN) dependency:resolve

.PHONY: setup
setup:
ifeq ($(SHELL_EXT),.sh)
	@chmod +x setup.sh
	@./setup.sh
else
	@setup.bat
endif

.PHONY: vscode
vscode:
ifeq ($(SHELL_EXT),.sh)
	@chmod +x open-in-vscode.sh
	@./open-in-vscode.sh
else
	@open-in-vscode.bat
endif

.PHONY: run-python
run-python:
	@echo "Running with Python launcher..."
	@python3 launch.py

.PHONY: quick
quick:
ifeq ($(SHELL_EXT),.sh)
	@chmod +x run.sh
	@./run.sh
else
	@quick-launch.bat
endif

# Development helpers
.PHONY: dev-setup
dev-setup: install
	@echo "Setting up development environment..."
ifeq ($(SHELL_EXT),.sh)
	@chmod +x *.sh
endif
	@echo "Development setup complete!"

.PHONY: check-deps
check-deps:
	@echo "Checking dependencies..."
	@echo -n "Java: "
	@java -version 2>&1 | head -1 || echo "Not found"
	@echo -n "Maven: "
	@mvn --version 2>/dev/null | head -1 || echo "Not found"
	@echo -n "Python: "
	@python3 --version 2>/dev/null || echo "Not found"

.PHONY: info
info:
	@echo "News Visualizer Project Information"
	@echo "=================================="
	@echo "Operating System: $(UNAME_S)"
	@echo "Shell Extension: $(SHELL_EXT)"
	@echo "Main Class: $(MAIN_CLASS)"
	@echo "Maven Command: $(MVN)"
	@echo