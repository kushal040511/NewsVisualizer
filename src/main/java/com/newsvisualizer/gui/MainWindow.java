package com.newsvisualizer.gui;

import com.newsvisualizer.model.NewsArticle;
import com.newsvisualizer.model.NewsResponse;
import com.newsvisualizer.model.SearchHistory;
import com.newsvisualizer.model.User;
import com.newsvisualizer.service.DatabaseService;
import com.newsvisualizer.service.NewsApiService;
import com.newsvisualizer.service.SessionManager;
import com.newsvisualizer.utils.NewsAnalyzer;
import com.newsvisualizer.utils.ArticleSummarizer;
import com.newsvisualizer.visualization.ChartGenerator;
import com.newsvisualizer.gui.NewsAppPanel;
import com.newsvisualizer.gui.TranslationPanel;
import com.newsvisualizer.gui.theme.ModernTheme;
import com.newsvisualizer.gui.components.ModernUIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Main window for the News Visualizer application
 */
public class MainWindow extends JFrame {
    private NewsApiService newsService;
    private SessionManager sessionManager;
    private DatabaseService databaseService;
    private List<NewsArticle> currentArticles;
    
    // GUI Components
    private ModernUIComponents.ModernComboBox countryCombo;
    private ModernUIComponents.ModernComboBox categoryCombo;
    private ModernUIComponents.ModernButton searchButton;
    private ModernUIComponents.ModernButton analyzeButton;
    private ModernUIComponents.ModernTextField urlField;
    private ModernUIComponents.ModernButton summarizeButton;
    private ModernUIComponents.ModernProgressBar progressBar;
    private JLabel statusLabel;
    private ModernUIComponents.ModernTabbedPane tabbedPane;
    private DefaultTableModel tableModel;
    private JTable articlesTable;
    private JMenuBar menuBar;
    private JMenu userMenu;
    
    public MainWindow() {
        initializeServices();
        
        // Show login window if not logged in
        if (!sessionManager.isLoggedIn()) {
            showLoginWindow();
        }
        
        initializeGUI();
        setupEventHandlers();
        setLocationRelativeTo(null);
    }
    
    private void initializeServices() {
        newsService = new NewsApiService();
        sessionManager = SessionManager.getInstance();
        databaseService = DatabaseService.getInstance();
    }
    
    private void showLoginWindow() {
        LoginWindow loginWindow = new LoginWindow(null);
        loginWindow.setVisible(true);
        
        if (!loginWindow.isLoginSuccessful()) {
            // User cancelled login, exit application
            System.exit(0);
        }
    }
    
    private void initializeGUI() {
        setTitle("News Visualizer - Professional News Analysis Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1800, 1200);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1600, 900));
        
        // Set modern theme background
        getContentPane().setBackground(ModernTheme.Colors.BACKGROUND_PRIMARY);
        
        // Create menu bar
        createMenuBar();
        
        // Create main panels with modern theme
        createModernHeader();
        createProfessionalContentArea();
        createModernStatusBar();
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // User menu
        userMenu = new JMenu();
        updateUserMenu();
        
        // Profile menu item
        JMenuItem profileItem = new JMenuItem("👤 Profile & History");
        profileItem.addActionListener(e -> showUserProfile());
        userMenu.add(profileItem);
        
        userMenu.addSeparator();
        
        // Logout menu item
        JMenuItem logoutItem = new JMenuItem("🚪 Logout");
        logoutItem.addActionListener(e -> performLogout());
        userMenu.add(logoutItem);
        
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(userMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void updateUserMenu() {
        if (sessionManager.isLoggedIn()) {
            User currentUser = sessionManager.getCurrentUser();
            userMenu.setText("Hello, " + currentUser.getFirstName());
        } else {
            userMenu.setText("Guest");
        }
    }
    
    private void showUserProfile() {
        if (sessionManager.isLoggedIn()) {
            UserProfileWindow profileWindow = new UserProfileWindow(this);
            profileWindow.setVisible(true);
        }
    }
    
    private void performLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            sessionManager.endSession();
            dispose();
            
            // Show new MainWindow which will prompt for login
            SwingUtilities.invokeLater(() -> {
                new MainWindow().setVisible(true);
            });
        }
    }
    
    private void createModernHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth(), h = getHeight();
                
                // Professional gradient - subtle and elegant
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255),
                    0, h, new Color(240, 242, 247)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, w, h);
                
                // Add subtle bottom border
                g2d.setColor(new Color(226, 232, 240));
                g2d.fillRect(0, h-1, w, 1);
                
                g2d.dispose();
            }
        };
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25)); // Reduced padding significantly
        
        // Create title section
        JPanel titleSection = createTitleSection();
        headerPanel.add(titleSection, BorderLayout.NORTH);
        
        // Create controls in a professional grid layout
        JPanel controlsPanel = createProfessionalControls();
        headerPanel.add(controlsPanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private JPanel createTitleSection() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Much smaller bottom spacing
        
        // Main title
        JLabel titleLabel = new JLabel("📊 News Visualizer Pro");
        titleLabel.setFont(ModernTheme.Fonts.TITLE_MEDIUM);
        titleLabel.setForeground(ModernTheme.Colors.TEXT_PRIMARY);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Professional News Analysis & Visualization Platform");
        subtitleLabel.setFont(ModernTheme.Fonts.BODY_MEDIUM);
        subtitleLabel.setForeground(ModernTheme.Colors.TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        titleStack.add(titleLabel);
        titleStack.add(subtitleLabel);
        
        titlePanel.add(titleStack, BorderLayout.CENTER);
        
        return titlePanel;
    }
    
    private JPanel createProfessionalControls() {
        JPanel mainControlsPanel = new JPanel(new BorderLayout());
        mainControlsPanel.setOpaque(false);
        
        // News Analysis Section
        JPanel newsSection = createControlSection("📰 News Analysis", createNewsControls());
        
        // Article Summarization Section  
        JPanel summarySection = createControlSection("🤖 AI Article Summarizer", createSummaryControls());
        
        // Compact grid layout with optimized spacing
        JPanel gridPanel = new JPanel(new GridLayout(1, 2, 25, 0)); // Reduced spacing to 25px
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(5, 0, 5, 0)); // Minimal vertical padding
        gridPanel.add(newsSection);
        gridPanel.add(summarySection);
        
        mainControlsPanel.add(gridPanel, BorderLayout.CENTER);
        
        // Progress bar at bottom
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setOpaque(false);
        progressPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Reduced top spacing
        
        progressBar = ModernUIComponents.createProgressBar();
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        mainControlsPanel.add(progressPanel, BorderLayout.SOUTH);
        
        return mainControlsPanel;
    }
    
    private JPanel createControlSection(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern card design using theme
                g2d.setColor(ModernTheme.Colors.BACKGROUND_CARD);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ModernTheme.Radius.LARGE, ModernTheme.Radius.LARGE);
                
                // Apply modern shadow
                ModernTheme.Shadows.applyCardShadow(g2d, getWidth(), getHeight(), ModernTheme.Radius.LARGE);
                
                // Modern border
                g2d.setColor(ModernTheme.Colors.BORDER_LIGHT);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, ModernTheme.Radius.LARGE, ModernTheme.Radius.LARGE);
                
                g2d.dispose();
            }
        };
        section.setOpaque(false);
        section.setBorder(new EmptyBorder(15, 20, 15, 20)); // Reduced card padding
        
        // Section title
        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(ModernTheme.Fonts.HEADING_MEDIUM);
        sectionTitle.setForeground(ModernTheme.Colors.TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, ModernTheme.Spacing.SMALL, 0));
        
        section.add(sectionTitle, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createNewsControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        // Country and Category in a row with better spacing
        JPanel selectionRow = new JPanel(new GridLayout(1, 2, 20, 0));
        selectionRow.setOpaque(false);
        
        // Country selection
        countryCombo = ModernUIComponents.createComboBox(new String[]{
            "United States (us)", "India (in)", "United Kingdom (gb)", "Canada (ca)", 
            "Australia (au)", "Germany (de)", "France (fr)", "Japan (jp)", "China (cn)"
        });
        countryCombo.setSelectedItem("India (in)");
        JPanel countryPanel = createFormGroup("Country:", countryCombo);
        selectionRow.add(countryPanel);
        
        // Category selection
        categoryCombo = ModernUIComponents.createComboBox(new String[]{
            "General", "Business", "Entertainment", "Health", "Science", "Sports", "Technology"
        });
        JPanel categoryPanel = createFormGroup("Category:", categoryCombo);
        selectionRow.add(categoryPanel);
        
        panel.add(selectionRow);
        panel.add(Box.createVerticalStrut(10)); // Reduced from 20 to 10
        
        // Action buttons in a row with compact spacing
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 15, 0)); // Reduced from 20 to 15
        buttonRow.setOpaque(false);
        
        searchButton = ModernUIComponents.createPrimaryButton("📊 Fetch News");
        analyzeButton = ModernUIComponents.createSecondaryButton("📈 Analyze Data");
        analyzeButton.setEnabled(false);
        
        buttonRow.add(searchButton);
        buttonRow.add(analyzeButton);
        
        panel.add(buttonRow);
        
        return panel;
    }
    
    private JPanel createSummaryControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        // URL input
        urlField = ModernUIComponents.createTextField(40);
        urlField.setToolTipText("Enter article URL for AI-powered summary");
        JPanel urlPanel = createFormGroup("Article URL:", urlField);
        
        panel.add(urlPanel);
        panel.add(Box.createVerticalStrut(ModernTheme.Spacing.MEDIUM));
        
        // Summarize button
        summarizeButton = ModernUIComponents.createAccentButton("🤖 Generate Summary");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(summarizeButton);
        
        panel.add(buttonPanel);
        
        return panel;
    }
    
    private JPanel createFormGroup(String labelText, JComponent component) {
        JPanel group = new JPanel(new BorderLayout());
        group.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(ModernTheme.Fonts.LABEL);
        label.setForeground(ModernTheme.Colors.TEXT_SECONDARY);
        label.setBorder(new EmptyBorder(0, 0, ModernTheme.Spacing.TINY, 0));
        
        JPanel componentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        componentPanel.setOpaque(false);
        componentPanel.add(component);
        
        group.add(label, BorderLayout.NORTH);
        group.add(componentPanel, BorderLayout.CENTER);
        
        return group;
    }
    
    
    private void createProfessionalContentArea() {
        tabbedPane = ModernUIComponents.createTabbedPane();
        
        // Articles table tab with modern design
        createProfessionalArticlesTab();
        
        // Analysis tabs with professional styling
        createProfessionalPlaceholderTabs();
        
        // Wrap in a modern container
        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setBackground(ModernTheme.Colors.BACKGROUND_PRIMARY);
        contentContainer.setBorder(new EmptyBorder(0, ModernTheme.Spacing.MEDIUM, ModernTheme.Spacing.MEDIUM, ModernTheme.Spacing.MEDIUM));
        contentContainer.add(tabbedPane, BorderLayout.CENTER);
        
        add(contentContainer, BorderLayout.CENTER);
    }
    
    
    
    
    private void setupEventHandlers() {
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchNews();
            }
        });
        
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyzeData();
            }
        });
        
        summarizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                summarizeArticle();
            }
        });
        
        // Enter key in URL field triggers summarization
        urlField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                summarizeArticle();
            }
        });
        
    }
    
    private void fetchNews() {
        // Run in background thread to not block UI
        SwingWorker<NewsResponse, Void> worker = new SwingWorker<NewsResponse, Void>() {
            @Override
            protected NewsResponse doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    searchButton.setEnabled(false);
                    analyzeButton.setEnabled(false);
                    progressBar.setIndeterminate(true);
                    progressBar.setString("Fetching news...");
                    statusLabel.setText("Fetching news articles...");
                });
                
                String countrySelection = (String) countryCombo.getSelectedItem();
                String categorySelection = (String) categoryCombo.getSelectedItem();
                
                // Extract country code from selection
                String country = null;
                if (countrySelection != null && countrySelection.contains("(")) {
                    String extracted = countrySelection.substring(countrySelection.lastIndexOf("(") + 1, countrySelection.lastIndexOf(")"));
                    if (!extracted.isEmpty()) {
                        country = extracted;
                    }
                }
                
                // Convert category to lowercase for API
                String category = null;
                if (categorySelection != null && !categorySelection.equals("General")) {
                    category = categorySelection.toLowerCase();
                }
                
                NewsResponse response;
                
                // Priority: Both -> Country Only -> Category Only -> Default (India)
                if (country != null && category != null) {
                    // Both country and category selected
                    response = newsService.getTopHeadlines(country, category);
                    if ("in".equals(country)) {
                        SwingUtilities.invokeLater(() -> 
                            statusLabel.setText("📡 Fetching " + categorySelection + " news from Indian RSS feeds..."));
                    } else if ("gb".equals(country)) {
                        SwingUtilities.invokeLater(() -> 
                            statusLabel.setText("📡 Fetching " + categorySelection + " news from UK RSS feeds..."));
                    } else {
                        SwingUtilities.invokeLater(() -> 
                            statusLabel.setText("Fetching " + categorySelection + " news from " + countrySelection.split(" ")[0] + "..."));
                    }
                } else if (country != null) {
                    // Only country selected
                    response = newsService.getTopHeadlines(country, null);
                    if ("in".equals(country)) {
                        SwingUtilities.invokeLater(() -> 
                            statusLabel.setText("📡 Fetching all news from Indian RSS feeds..."));
                    } else if ("gb".equals(country)) {
                        SwingUtilities.invokeLater(() -> 
                            statusLabel.setText("📡 Fetching all news from UK RSS feeds..."));
                    } else {
                        SwingUtilities.invokeLater(() -> 
                            statusLabel.setText("Fetching all news from " + countrySelection.split(" ")[0] + "..."));
                    }
                } else if (category != null) {
                    // Only category selected (global)
                    response = newsService.getTopHeadlines(null, category);
                    SwingUtilities.invokeLater(() -> 
                        statusLabel.setText("Fetching global " + categorySelection + " news..."));
                } else {
                    // Default: get Indian general news
                    response = newsService.getTopHeadlines("in", null);
                    SwingUtilities.invokeLater(() -> 
                        statusLabel.setText("📡 Fetching news from Indian RSS feeds (default)..."));
                }
                
                return response;
            }
            
            @Override
            protected void done() {
                try {
                    NewsResponse response = get();
                    processNewsResponse(response);
                } catch (Exception e) {
                    showError("Error fetching news: " + e.getMessage());
                } finally {
                    searchButton.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                    progressBar.setString("Ready");
                }
            }
        };
        
        worker.execute();
    }
    
    private void processNewsResponse(NewsResponse response) {
        if (response.isSuccess() && response.hasArticles()) {
            currentArticles = response.getArticles();
            updateArticlesTable();
            analyzeButton.setEnabled(true);
            
            // Save search history if user is logged in
            saveSearchHistory(response);
            
            // Check data source type
            String countrySelection = (String) countryCombo.getSelectedItem();
            String country = null;
            if (countrySelection != null && countrySelection.contains("(")) {
                String extracted = countrySelection.substring(countrySelection.lastIndexOf("(") + 1, countrySelection.lastIndexOf(")"));
                if (!extracted.isEmpty()) {
                    country = extracted;
                }
            }
            
            // Check if this might be mock data (common source names indicate mock data)
            boolean isMockData = currentArticles.stream()
                .anyMatch(article -> article.getSource() != null && 
                         article.getSource().getName() != null &&
                         (article.getSource().getName().contains("India") ||
                          article.getSource().getName().equals("TechNews India")));
            
            // Check if RSS feeds were used for Indian news
            boolean isIndianRss = "in".equals(country) && !isMockData && currentArticles.stream()
                .anyMatch(article -> article.getSource() != null &&
                         article.getSource().getName() != null &&
                         (article.getSource().getName().contains("Times of India") ||
                          article.getSource().getName().contains("The Hindu") ||
                          article.getSource().getName().contains("NDTV") ||
                          article.getSource().getName().contains("Economic Times")));
            
            // Check if RSS feeds were used for UK news
            boolean isUKRss = "gb".equals(country) && !isMockData && currentArticles.stream()
                .anyMatch(article -> article.getSource() != null &&
                         article.getSource().getName() != null &&
                         (article.getSource().getName().contains("BBC News") ||
                          article.getSource().getName().contains("The Guardian") ||
                          article.getSource().getName().contains("Sky News") ||
                          article.getSource().getName().contains("The Telegraph") ||
                          article.getSource().getName().contains("The Independent")));
            
            if (isIndianRss) {
                statusLabel.setText("📡 Fetched " + currentArticles.size() + " articles from Indian RSS feeds. Click 'Analyze Data' to see visualizations!");
            } else if (isUKRss) {
                statusLabel.setText("📡 Fetched " + currentArticles.size() + " articles from UK RSS feeds. Click 'Analyze Data' to see visualizations!");
            } else if (isMockData) {
                statusLabel.setText("🎆 Fetched " + currentArticles.size() + " demo articles (API unavailable). Click 'Analyze Data' to see visualizations!");
            } else {
                statusLabel.setText("📰 Fetched " + currentArticles.size() + " live articles. Click 'Analyze Data' to generate visualizations!");
            }
        } else {
            String message = response.getMessage() != null ? response.getMessage() : "No articles found";
            showError("Error: " + message);
            statusLabel.setText("⚠️ Error fetching news");
        }
    }
    
    private void saveSearchHistory(NewsResponse response) {
        if (sessionManager.isLoggedIn()) {
            try {
                User currentUser = sessionManager.getCurrentUser();
                
                // Extract search parameters
                String countrySelection = (String) countryCombo.getSelectedItem();
                String categorySelection = (String) categoryCombo.getSelectedItem();
                
                String country = null;
                if (countrySelection != null && countrySelection.contains("(")) {
                    country = countrySelection.substring(countrySelection.lastIndexOf("(") + 1, countrySelection.lastIndexOf(")"));
                }
                
                String category = null;
                if (categorySelection != null && !categorySelection.equals("General")) {
                    category = categorySelection.toLowerCase();
                }
                
                // Create search history record
                SearchHistory searchHistory = new SearchHistory(
                    currentUser.getId(),
                    "headlines",
                    null, // query is null for headlines
                    country,
                    category,
                    response.getArticles().size()
                );
                
                databaseService.saveSearchHistory(searchHistory);
                
            } catch (SQLException e) {
                // Log error but don't interrupt user flow
                System.err.println("Failed to save search history: " + e.getMessage());
            }
        }
    }
    
    private void updateArticlesTable() {
        tableModel.setRowCount(0); // Clear existing data
        
        for (NewsArticle article : currentArticles) {
            Object[] row = {
                article.getTitle(), // Don't truncate title - let horizontal scrolling handle it
                article.getSource() != null ? article.getSource().getName() : "Unknown",
                article.getPublishedAt() != null ? 
                    article.getPublishedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Unknown"
            };
            tableModel.addRow(row);
        }
    }
    
    private void analyzeData() {
        if (currentArticles == null || currentArticles.isEmpty()) {
            showError("No articles to analyze. Please fetch news first.");
            return;
        }
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    analyzeButton.setEnabled(false);
                    progressBar.setIndeterminate(true);
                    progressBar.setString("Analyzing data...");
                    statusLabel.setText("Analyzing news data and generating visualizations...");
                });
                
                // Perform sentiment analysis
                NewsAnalyzer.analyzeSentiment(currentArticles);
                
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    generateVisualizations();
                    updateArticlesTable(); // Refresh table with sentiment data
                    analyzeButton.setEnabled(true);
                    statusLabel.setText("Analysis complete. View results in the different tabs.");
                } catch (Exception e) {
                    showError("Error analyzing data: " + e.getMessage());
                } finally {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                    progressBar.setString("Ready");
                }
            }
        };
        
        worker.execute();
    }
    
    private void summarizeArticle() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            showError("Please enter an article URL to summarize.");
            return;
        }
        
        if (!isValidUrl(url)) {
            showError("Please enter a valid URL (must start with http:// or https://)");
            return;
        }
        
        SwingWorker<ArticleSummarizer.ArticleSummary, Void> worker = new SwingWorker<ArticleSummarizer.ArticleSummary, Void>() {
            @Override
            protected ArticleSummarizer.ArticleSummary doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    summarizeButton.setEnabled(false);
                    progressBar.setIndeterminate(true);
                    progressBar.setString("Fetching and summarizing article...");
                    statusLabel.setText("🔍 Fetching article content and generating AI summary...");
                });
                
                return ArticleSummarizer.summarizeFromUrl(url);
            }
            
            @Override
            protected void done() {
                try {
                    ArticleSummarizer.ArticleSummary summary = get();
                    displayArticleSummary(summary);
                    statusLabel.setText("✨ Article summarized successfully! Check the 'AI Summary' tab.");
                } catch (Exception e) {
                    showError("Error summarizing article: " + e.getMessage());
                    statusLabel.setText("⚠️ Error summarizing article");
                } finally {
                    summarizeButton.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                    progressBar.setString("Ready");
                }
            }
        };
        
        worker.execute();
    }
    
    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
    
    private void displayArticleSummary(ArticleSummarizer.ArticleSummary summary) {
        // Create main panel with modern gradient background
        JPanel summaryPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 255), 0, h, new Color(248, 250, 252));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        
        // Create header panel with title and stats
        JPanel headerPanel = createSummaryHeader(summary);
        summaryPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create main content with cards layout
        JPanel mainContent = createMainSummaryContent(summary);
        
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        
        summaryPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Replace the AI Summary tab (index 4)
        tabbedPane.setComponentAt(4, summaryPanel);
        tabbedPane.setSelectedIndex(4); // Switch to AI Summary tab
    }
    
    private JPanel createSummaryHeader(ArticleSummarizer.ArticleSummary summary) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25)); // Compact padding
        
        // Title with modern styling
        String titleText = summary.getTitle().length() > 80 ? 
            summary.getTitle().substring(0, 80) + "..." : summary.getTitle();
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>"
            + "<h1 style='color: #2C3E50; font-family: Arial, sans-serif; margin: 0; font-size: 28px; line-height: 1.3;'>" // Increased from 24px to 28px
            + "📰 " + titleText + "</h1></div></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // URL and metadata
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        metaPanel.setOpaque(false);
        
        String urlText = summary.getUrl().length() > 60 ? 
            summary.getUrl().substring(0, 60) + "..." : summary.getUrl();
        JLabel urlLabel = new JLabel("<html><div style='text-align: center; color: #7F8C8D; font-size: 12px;'>"
            + "🔗 <a href='" + summary.getUrl() + "'>" + urlText + "</a></div></html>");
        metaPanel.add(urlLabel);
        
        headerPanel.add(metaPanel, BorderLayout.SOUTH);
        
        return headerPanel;
    }
    
    private JPanel createMainSummaryContent(ArticleSummarizer.ArticleSummary summary) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(0, 20, 20, 20)); // Reduced padding
        
        // Check if this is an error summary and handle differently
        boolean isError = summary.getSummary().contains("Access Denied") || 
                         summary.getSummary().contains("Error") || 
                         summary.getSummary().contains("Failed");
        
        if (isError) {
            // For error summaries, show only the error message prominently
            JPanel errorCard = createErrorSummaryCard(summary);
            mainPanel.add(errorCard);
        } else {
            // Normal summary layout with all components
            // Statistics card
            JPanel statsCard = createStatsCard(summary);
            mainPanel.add(statsCard);
            mainPanel.add(Box.createVerticalStrut(15)); // Reduced to 15px
            
            // AI Summary card
            JPanel summaryCard = createSummaryCard(summary);
            mainPanel.add(summaryCard);
            mainPanel.add(Box.createVerticalStrut(15)); // Reduced to 15px
            
            // Key Points card
            if (!summary.getKeyPoints().isEmpty()) {
                JPanel keyPointsCard = createKeyPointsCard(summary);
                mainPanel.add(keyPointsCard);
                mainPanel.add(Box.createVerticalStrut(15)); // Reduced to 15px
            }
            
            // Keywords card
            if (!summary.getKeywords().isEmpty()) {
                JPanel keywordsCard = createKeywordsCard(summary);
                mainPanel.add(keywordsCard);
            }
        }
        
        return mainPanel;
    }
    
    private JPanel createStatsCard(ArticleSummarizer.ArticleSummary summary) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());
        
        JLabel headerLabel = new JLabel("<html><h3 style='color: #3498DB; margin: 0; font-size: 18px;'>📊 Article Statistics</h3></html>");
        headerLabel.setBorder(new EmptyBorder(10, 15, 8, 15)); // Compact header
        card.add(headerLabel, BorderLayout.NORTH);
        
        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 15, 15)); // Compact spacing
        statsGrid.setOpaque(false);
        statsGrid.setBorder(new EmptyBorder(10, 15, 15, 15)); // Compact border padding
        
        // Word count stat
        JPanel wordPanel = createStatItem("📝 Word Count", String.valueOf(summary.getWordCount()), new Color(52, 152, 219));
        statsGrid.add(wordPanel);
        
        // Key points stat
        JPanel pointsPanel = createStatItem("🔑 Key Points", String.valueOf(summary.getKeyPoints().size()), new Color(46, 204, 113));
        statsGrid.add(pointsPanel);
        
        // Keywords stat
        JPanel keywordsPanel = createStatItem("🏷️ Keywords", String.valueOf(summary.getKeywords().size()), new Color(155, 89, 182));
        statsGrid.add(keywordsPanel);
        
        // Reading time estimate
        int readingTime = Math.max(1, summary.getWordCount() / 200); // ~200 words per minute
        JPanel timePanel = createStatItem("⏱️ Read Time", readingTime + " min", new Color(230, 126, 34));
        statsGrid.add(timePanel);
        
        card.add(statsGrid, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createSummaryCard(ArticleSummarizer.ArticleSummary summary) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());
        
        JLabel headerLabel = new JLabel("<html><h3 style='color: #E74C3C; margin: 0; font-size: 18px;'>🤖 AI-Generated Summary</h3></html>");
        headerLabel.setBorder(new EmptyBorder(10, 15, 8, 15)); // Compact header
        card.add(headerLabel, BorderLayout.NORTH);
        
        JTextArea summaryText = new JTextArea(summary.getSummary(), 0, 120); // Increased to 120 columns for even wider text
        summaryText.setWrapStyleWord(true);
        summaryText.setLineWrap(true);
        summaryText.setEditable(false);
        summaryText.setOpaque(true);
        summaryText.setBackground(new Color(255, 255, 255)); // White background
        summaryText.setFont(new Font("Georgia", Font.PLAIN, 18)); // Increased font size to 18 for better readability
        summaryText.setForeground(new Color(44, 62, 80));
        summaryText.setBorder(new EmptyBorder(15, 15, 15, 15)); // Compact padding
        summaryText.setMargin(new Insets(10, 10, 10, 10)); // Compact margin
        
        // Calculate compact dynamic height based on text length
        int textLength = summary.getSummary().length();
        int estimatedHeight = Math.max(200, Math.min(400, (int)(textLength / 2.5))); // More compact sizing
        
        JScrollPane summaryScroll = new JScrollPane(summaryText);
        summaryScroll.setPreferredSize(new Dimension(900, estimatedHeight)); // More reasonable width
        summaryScroll.setMinimumSize(new Dimension(700, 200)); // Compact minimum dimensions
        summaryScroll.setOpaque(false);
        summaryScroll.getViewport().setOpaque(false);
        summaryScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        summaryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        summaryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        summaryScroll.getVerticalScrollBar().setUnitIncrement(20); // Smoother scrolling
        
        card.add(summaryScroll, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createKeyPointsCard(ArticleSummarizer.ArticleSummary summary) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());
        
        JLabel headerLabel = new JLabel("<html><h3 style='color: #27AE60; margin: 0; font-size: 18px;'>💡 Key Insights</h3></html>");
        headerLabel.setBorder(new EmptyBorder(15, 20, 10, 20));
        card.add(headerLabel, BorderLayout.NORTH);
        
        JPanel pointsPanel = new JPanel();
        pointsPanel.setLayout(new BoxLayout(pointsPanel, BoxLayout.Y_AXIS));
        pointsPanel.setOpaque(false);
        pointsPanel.setBorder(new EmptyBorder(5, 20, 20, 20));
        
        for (String point : summary.getKeyPoints()) {
            JPanel pointPanel = createKeyPointItem(point);
            pointsPanel.add(pointPanel);
            pointsPanel.add(Box.createVerticalStrut(8));
        }
        
        card.add(pointsPanel, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createErrorSummaryCard(ArticleSummarizer.ArticleSummary summary) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());
        
        // Error icon and header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 25, 15, 25));
        
        JLabel headerLabel = new JLabel("<html><h3 style='color: #E74C3C; margin: 0; font-size: 20px;'>💫 Access Issue</h3></html>");
        headerPanel.add(headerLabel);
        card.add(headerPanel, BorderLayout.NORTH);
        
        // Error message with proper formatting
        JTextArea errorText = new JTextArea(summary.getSummary(), 0, 90);
        errorText.setWrapStyleWord(true);
        errorText.setLineWrap(true);
        errorText.setEditable(false);
        errorText.setOpaque(true);
        errorText.setBackground(new Color(254, 242, 242)); // Light red background
        errorText.setFont(new Font("SF Pro Text", Font.PLAIN, 16));
        errorText.setForeground(new Color(153, 27, 27)); // Dark red text
        errorText.setBorder(new EmptyBorder(25, 25, 25, 25));
        errorText.setMargin(new Insets(15, 15, 15, 15));
        
        JScrollPane errorScroll = new JScrollPane(errorText);
        errorScroll.setPreferredSize(new Dimension(900, 300));
        errorScroll.setMinimumSize(new Dimension(700, 200));
        errorScroll.setOpaque(false);
        errorScroll.getViewport().setOpaque(false);
        errorScroll.setBorder(BorderFactory.createLineBorder(new Color(254, 226, 226), 2));
        errorScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        errorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        card.add(errorScroll, BorderLayout.CENTER);
        
        // Helpful tips section
        JPanel tipsPanel = new JPanel();
        tipsPanel.setLayout(new BoxLayout(tipsPanel, BoxLayout.Y_AXIS));
        tipsPanel.setOpaque(false);
        tipsPanel.setBorder(new EmptyBorder(15, 25, 25, 25));
        
        JLabel tipsHeader = new JLabel("<html><h4 style='color: #059669; margin: 0;'>💡 Suggested Articles to Try:</h4></html>");
        tipsPanel.add(tipsHeader);
        tipsPanel.add(Box.createVerticalStrut(10));
        
        String[] suggestedSites = {
            "• BBC News: https://www.bbc.com/news",
            "• Reuters: https://www.reuters.com",
            "• Associated Press: https://apnews.com",
            "• The Guardian: https://www.theguardian.com",
            "• NPR: https://www.npr.org"
        };
        
        for (String site : suggestedSites) {
            JLabel siteLabel = new JLabel("<html><span style='color: #374151; font-size: 14px;'>" + site + "</span></html>");
            siteLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
            tipsPanel.add(siteLabel);
        }
        
        card.add(tipsPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createKeywordsCard(ArticleSummarizer.ArticleSummary summary) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());
        
        JLabel headerLabel = new JLabel("<html><h3 style='color: #9B59B6; margin: 0; font-size: 18px;'>🏷️ Trending Keywords</h3></html>");
        headerLabel.setBorder(new EmptyBorder(15, 20, 10, 20));
        card.add(headerLabel, BorderLayout.NORTH);
        
        JPanel keywordsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        keywordsPanel.setOpaque(false);
        keywordsPanel.setBorder(new EmptyBorder(5, 15, 20, 15));
        
        for (Map.Entry<String, Integer> keyword : summary.getKeywords().entrySet()) {
            JPanel keywordTag = createKeywordTag(keyword.getKey(), keyword.getValue());
            keywordsPanel.add(keywordTag);
        }
        
        card.add(keywordsPanel, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2d.setColor(new Color(220, 220, 220, 100));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };
        card.setOpaque(false);
        return card;
    }
    
    private JPanel createStatItem(String label, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel valueLabel = new JLabel("<html><div style='text-align: center; color: " + 
            String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()) + 
            "; font-size: 22px; font-weight: bold; margin-bottom: 5px;'>" + value + "</div></html>");
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel labelLabel = new JLabel("<html><div style='text-align: center; color: #7F8C8D; font-size: 12px;'>" + 
            label + "</div></html>");
        labelLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(labelLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createKeyPointItem(String point) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        // Use JTextArea for better text wrapping instead of HTML labels
        JTextArea pointText = new JTextArea(point, 0, 110); // Increased to 110 columns for even wider wrapping
        pointText.setWrapStyleWord(true);
        pointText.setLineWrap(true);
        pointText.setEditable(false);
        pointText.setOpaque(false);
        pointText.setFont(new Font("Arial", Font.PLAIN, 17)); // Increased font size to 17 for better readability
        pointText.setForeground(new Color(44, 62, 80));
        pointText.setBorder(new EmptyBorder(15, 15, 15, 15)); // Increased padding for better spacing
        pointText.setBackground(new Color(248, 249, 250));
        
        // Create a subtle background for each point
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(248, 249, 250));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.setColor(new Color(220, 220, 220, 80));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(pointText, BorderLayout.CENTER);
        
        panel.add(wrapper, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createKeywordTag(String keyword, int frequency) {
        JPanel tag = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(155, 89, 182, 30));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(new Color(155, 89, 182));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        tag.setOpaque(false);
        tag.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 5));
        
        JLabel keywordLabel = new JLabel("<html><span style='color: #8E44AD; font-weight: bold; font-size: 12px;'>" + 
            keyword + " <span style='color: #BDC3C7;'>" + frequency + "</span></span></html>");
        tag.add(keywordLabel);
        
        return tag;
    }
    
    private void generateVisualizations() {
        // Generate analysis data
        Map<String, Integer> sentimentDist = NewsAnalyzer.getSentimentDistribution(currentArticles);
        Map<String, Integer> sourceDist = NewsAnalyzer.getSourceDistribution(currentArticles);
        Map<String, Integer> keywords = NewsAnalyzer.extractKeywords(currentArticles, 20);
        
        // Timeline functionality removed
        
        // Calculate summary statistics
        double avgSentiment = NewsAnalyzer.getAverageSentiment(currentArticles);
        int uniqueSources = sourceDist.size();
        String topKeyword = keywords.isEmpty() ? "N/A" : keywords.keySet().iterator().next();
        
        // Create summary panel
        JPanel summaryPanel = ChartGenerator.createSummaryPanel(
            currentArticles.size(), avgSentiment, uniqueSources, topKeyword);
        
        // Update tabs with visualizations
        // Sentiment Analysis tab
        JPanel sentimentPanel = new JPanel(new BorderLayout());
        sentimentPanel.add(summaryPanel, BorderLayout.NORTH);
        if (!sentimentDist.isEmpty()) {
            JPanel chartPanel = ChartGenerator.createSentimentChart("Sentiment Distribution", sentimentDist);
            sentimentPanel.add(chartPanel, BorderLayout.CENTER);
        }
        tabbedPane.setComponentAt(1, sentimentPanel);
        
        // Source Distribution tab
        if (!sourceDist.isEmpty()) {
            JPanel sourceChart = ChartGenerator.createHorizontalBarChart(
                "Articles by Source", "Sources", "Number of Articles", sourceDist);
            tabbedPane.setComponentAt(2, sourceChart);
        }
        
        // Keywords tab
        if (!keywords.isEmpty()) {
            JPanel keywordChart = ChartGenerator.createWordFrequencyChart(
                "Top Keywords", keywords);
            tabbedPane.setComponentAt(3, keywordChart);
        }
        
        // Timeline functionality removed
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    
    // Helper methods for enhanced UI components
    private JButton createEnhancedButton(String text, Color[] gradientColors, Color textColor) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            private boolean isPressed = false;
            private float hoverScale = 1.0f;
            private Timer hoverTimer;
            
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        isHovered = true;
                        startHoverAnimation();
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    }
                    
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        isHovered = false;
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                    
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        isPressed = true;
                        repaint();
                    }
                    
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent e) {
                        isPressed = false;
                        repaint();
                    }
                });
            }
            
            private void startHoverAnimation() {
                if (hoverTimer != null) hoverTimer.stop();
                hoverTimer = new Timer(20, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isHovered && hoverScale < 1.08f) {
                            hoverScale += 0.02f;
                        } else if (!isHovered && hoverScale > 1.0f) {
                            hoverScale -= 0.02f;
                        } else {
                            hoverTimer.stop();
                        }
                        repaint();
                    }
                });
                hoverTimer.start();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Apply scale transformation
                if (hoverScale != 1.0f) {
                    double centerX = w / 2.0;
                    double centerY = h / 2.0;
                    g2d.translate(centerX, centerY);
                    g2d.scale(hoverScale, hoverScale);
                    g2d.translate(-centerX, -centerY);
                }
                
                // Shadow effect
                if (isHovered && !isPressed) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(Color.BLACK);
                    g2d.fillRoundRect(3, 5, w - 3, h - 3, 12, 12);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
                
                // Button background with gradient
                Color color1 = isPressed ? gradientColors[1].darker() : 
                              isHovered ? gradientColors[0].brighter() : gradientColors[0];
                Color color2 = isPressed ? gradientColors[1].darker().darker() : 
                              isHovered ? gradientColors[1].brighter() : gradientColors[1];
                
                GradientPaint gradient = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, w, h, 12, 12);
                
                // Glossy highlight
                if (isHovered || isPressed) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    g2d.setColor(Color.WHITE);
                    g2d.fillRoundRect(0, 0, w, h/3, 12, 12);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
                
                // Border with glow
                if (isHovered) {
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2.0f));
                    g2d.drawRoundRect(1, 1, w - 2, h - 2, 12, 12);
                } else {
                    g2d.setColor(gradientColors[1].darker());
                    g2d.setStroke(new BasicStroke(1.0f));
                    g2d.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
                }
                
                g2d.dispose();
                
                // Paint text with shadow effect
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(textColor);
        button.setPreferredSize(new Dimension(160, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        
        return button;
    }
    
    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns) {
            private boolean focused = false;
            private float glowIntensity = 0.0f;
            private Timer glowTimer;
            
            {
                addFocusListener(new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusGained(java.awt.event.FocusEvent e) {
                        focused = true;
                        startGlowAnimation();
                    }
                    
                    @Override
                    public void focusLost(java.awt.event.FocusEvent e) {
                        focused = false;
                    }
                });
            }
            
            private void startGlowAnimation() {
                if (glowTimer != null) glowTimer.stop();
                glowTimer = new Timer(50, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (focused) {
                            glowIntensity = Math.min(1.0f, glowIntensity + 0.1f);
                        } else {
                            glowIntensity = Math.max(0.0f, glowIntensity - 0.1f);
                            if (glowIntensity <= 0.0f) {
                                glowTimer.stop();
                            }
                        }
                        repaint();
                    }
                });
                glowTimer.start();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background with gradient
                GradientPaint bgGradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 250),
                    0, getHeight(), new Color(248, 249, 250)
                );
                g2d.setPaint(bgGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Beautiful blue glow effect when focused
                if (glowIntensity > 0) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowIntensity * 0.7f));
                    g2d.setColor(new Color(30, 144, 255));
                    g2d.setStroke(new BasicStroke(3.0f));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                    
                    // Add outer purple glow
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowIntensity * 0.4f));
                    g2d.setColor(new Color(138, 43, 226));
                    g2d.setStroke(new BasicStroke(4.5f));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                
                // Border
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(200, 206, 212));
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(new Color(44, 62, 80));
        field.setBorder(new EmptyBorder(8, 12, 8, 12));
        field.setOpaque(false);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 35));
        
        return field;
    }
    
    private JProgressBar createEnhancedProgressBar() {
        JProgressBar progressBar = new JProgressBar() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background with glass effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Progress with animated gradient
                if (isIndeterminate()) {
                    int barWidth = getWidth() / 3;
                    int pos = (int)(System.currentTimeMillis() / 8) % (getWidth() + barWidth);
                    
                    Color[] colors = {new Color(138, 43, 226), new Color(30, 144, 255), new Color(25, 25, 112)};
                    for (int i = 0; i < colors.length; i++) {
                        int x = pos - barWidth + i * barWidth / colors.length;
                        if (x < getWidth() && x + barWidth / colors.length > 0) {
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                            g2d.setColor(colors[i]);
                            int startX = Math.max(0, x);
                            int endX = Math.min(getWidth(), x + barWidth / colors.length);
                            g2d.fillRoundRect(startX, 1, endX - startX, getHeight() - 2, 6, 6);
                        }
                    }
                } else if (getValue() > 0) {
                    int width = (int)((double)getValue() / getMaximum() * getWidth());
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(30, 144, 255), width, 0, new Color(138, 43, 226));
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 1, width, getHeight() - 2, 6, 6);
                }
                
                // String painting
                if (isStringPainted() && getString() != null) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    g2d.setColor(new Color(44, 62, 80));
                    g2d.setFont(new Font("Arial", Font.BOLD, 11));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = getString();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = (getHeight() + fm.getAscent()) / 2 - 2;
                    g2d.drawString(text, x, y);
                }
                
                g2d.dispose();
            }
        };
        
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setFont(new Font("Arial", Font.BOLD, 11));
        progressBar.setOpaque(false);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 25));
        
        // Animate the progress bar
        Timer animationTimer = new Timer(50, e -> {
            if (progressBar.isIndeterminate()) {
                progressBar.repaint();
            }
        });
        animationTimer.start();
        
        return progressBar;
    }
    
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<String>(items) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 240),
                    0, getHeight(), new Color(248, 249, 250, 220)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Border
                g2d.setColor(new Color(200, 206, 212));
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        comboBox.setFont(new Font("Arial", Font.BOLD, 12));
        comboBox.setForeground(new Color(44, 62, 80));
        comboBox.setOpaque(false);
        comboBox.setPreferredSize(new Dimension(150, 35));
        comboBox.setBorder(new EmptyBorder(5, 10, 5, 10));
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        comboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                comboBox.repaint();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                comboBox.repaint();
            }
        });
        
        return comboBox;
    }
    
    private JTabbedPane createProfessionalTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Clean white background
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        // Set tab layout policy to handle overflow with scrollable tabs
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
        tabbedPane.setFont(new Font("SF Pro Text", Font.BOLD, 14));
        tabbedPane.setForeground(new Color(55, 65, 81));
        tabbedPane.setOpaque(false);
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        return tabbedPane;
    }
    
    private void createProfessionalArticlesTab() {
        // Create modern table model
        String[] columnNames = {"📰 Title", "🏢 Source", "🗺️ Published"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        articlesTable = new JTable(tableModel) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Add subtle row alternation
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(249, 250, 251));
                for (int i = 1; i < getRowCount(); i += 2) {
                    int y = i * getRowHeight();
                    g2d.fillRect(0, y, getWidth(), getRowHeight());
                }
                g2d.dispose();
            }
        };
        
        articlesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        articlesTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        // Improved column sizing for better content visibility
        articlesTable.getColumnModel().getColumn(0).setPreferredWidth(900); // Increased from 700 to 900
        articlesTable.getColumnModel().getColumn(0).setMinWidth(500); // Increased from 400 to 500
        articlesTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Increased from 250 to 300
        articlesTable.getColumnModel().getColumn(1).setMinWidth(200); // Increased from 150 to 200
        articlesTable.getColumnModel().getColumn(2).setPreferredWidth(220); // Increased from 180 to 220
        articlesTable.getColumnModel().getColumn(2).setMinWidth(160); // Increased from 130 to 160
        
        // Enhanced modern table styling with better readability
        articlesTable.setBackground(Color.WHITE);
        articlesTable.setGridColor(new Color(229, 231, 235));
        articlesTable.setRowHeight(55); // Increased from 45 to 55 for better spacing
        articlesTable.setFont(new Font("SF Pro Text", Font.PLAIN, 14)); // Increased font size from 13 to 14
        articlesTable.setForeground(new Color(55, 65, 81));
        articlesTable.setShowGrid(false);
        articlesTable.setIntercellSpacing(new Dimension(0, 2)); // Increased from 1 to 2 for better separation
        
        // Modern header styling
        articlesTable.getTableHeader().setBackground(new Color(249, 250, 251));
        articlesTable.getTableHeader().setForeground(new Color(75, 85, 99));
        articlesTable.getTableHeader().setFont(new Font("SF Pro Text", Font.BOLD, 13));
        articlesTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));
        
        JScrollPane scrollPane = new JScrollPane(articlesTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tabbedPane.addTab("📰 Articles", scrollPane);
    }
    
    private void createProfessionalPlaceholderTabs() {
        // Sentiment Analysis tab
        JPanel sentimentPanel = createPlaceholderPanel(
            "💭 Sentiment Analysis", 
            "Analyze emotional tone and sentiment patterns in news articles",
            new Color(59, 130, 246)
        );
        tabbedPane.addTab("💭 Sentiment", sentimentPanel);
        
        // Source Distribution tab
        JPanel sourcePanel = createPlaceholderPanel(
            "🏢 Source Distribution", 
            "Visualize news sources and their contribution to your analysis",
            new Color(16, 185, 129)
        );
        tabbedPane.addTab("🏢 Sources", sourcePanel);
        
        // Keywords tab
        JPanel keywordPanel = createPlaceholderPanel(
            "🔑 Keyword Analysis", 
            "Discover trending keywords and topics in the news",
            new Color(245, 101, 101)
        );
        tabbedPane.addTab("🔑 Keywords", keywordPanel);
        
        // Timeline tab removed
        
        // Article Summary tab
        JPanel summaryPanel = createPlaceholderPanel(
            "🤖 AI Article Summary", 
            "Get AI-powered summaries of any news article from around the world",
            new Color(245, 158, 11)
        );
        tabbedPane.addTab("🤖 AI Summary", summaryPanel);
        
        // Translation & Dictionary tab
        TranslationPanel translationPanel = new TranslationPanel();
        tabbedPane.addTab("📖 Dictionary", translationPanel);
        
        // NewsApp Integration tab
        NewsAppPanel newsAppPanel = new NewsAppPanel();
        tabbedPane.addTab("📱 NewsApp Legacy", newsAppPanel);
    }
    
    private JPanel createPlaceholderPanel(String title, String description, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern gradient background
                GradientPaint gradient = ModernTheme.Gradients.BACKGROUND_GRADIENT(getHeight());
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(60, 40, 60, 40));
        
        // Icon placeholder
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 20));
                g2d.fillOval(0, 0, 80, 80);
                
                g2d.setColor(accentColor);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawOval(0, 0, 80, 80);
                
                g2d.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(80, 80));
        iconPanel.setMaximumSize(new Dimension(80, 80));
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ModernTheme.Fonts.TITLE_MEDIUM);
        titleLabel.setForeground(ModernTheme.Colors.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Description
        JLabel descLabel = new JLabel("<html><div style='text-align: center; width: 400px;'>" + description + "</div></html>");
        descLabel.setFont(ModernTheme.Fonts.BODY_LARGE);
        descLabel.setForeground(ModernTheme.Colors.TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        content.add(iconPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(descLabel);
        
        panel.add(content, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void createModernStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Modern status bar background
                g2d.setColor(ModernTheme.Colors.BACKGROUND_SECONDARY);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Top border
                g2d.setColor(ModernTheme.Colors.BORDER_LIGHT);
                g2d.fillRect(0, 0, getWidth(), 1);
                
                g2d.dispose();
            }
        };
        statusPanel.setBorder(new EmptyBorder(ModernTheme.Spacing.MEDIUM, ModernTheme.Spacing.LARGE, ModernTheme.Spacing.MEDIUM, ModernTheme.Spacing.LARGE));
        
        statusLabel = new JLabel("\u2728 Welcome to News Visualizer Pro - Select your preferences and click 'Fetch News' to begin");
        statusLabel.setFont(ModernTheme.Fonts.BODY_SMALL);
        statusLabel.setForeground(ModernTheme.Colors.TEXT_SECONDARY);
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    @Override
    public void dispose() {
        if (newsService != null) {
            newsService.close();
        }
        super.dispose();
    }
}
