package com.newsvisualizer.gui;

import com.newsvisualizer.gui.components.ModernSidebar;
import com.newsvisualizer.gui.components.ModernUIComponents;
import com.newsvisualizer.gui.theme.EnhancedModernTheme;
import com.newsvisualizer.model.NewsArticle;
import com.newsvisualizer.model.NewsResponse;
import com.newsvisualizer.service.NewsApiService;
import com.newsvisualizer.service.SessionManager;
import com.newsvisualizer.service.DatabaseService;
import com.newsvisualizer.utils.NewsAnalyzer;
import com.newsvisualizer.utils.ArticleSummarizer;
import com.newsvisualizer.visualization.ChartGenerator;
import com.newsvisualizer.gui.TranslationPanel;
import com.newsvisualizer.gui.NewsAppPanel;
import com.newsvisualizer.gui.UserProfileWindow;
import com.newsvisualizer.model.SearchHistory;
import com.newsvisualizer.model.User;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Enhanced main window with modern sidebar navigation and clean layout
 */
public class EnhancedMainWindow extends JFrame implements ModernSidebar.NavigationListener {
    
    private ModernSidebar sidebar;
    private JPanel contentArea;
    private CardLayout contentCardLayout;
    
    // Services
    private NewsApiService newsService;
    private SessionManager sessionManager;
    private DatabaseService databaseService;
    
    // Current data
    private List<NewsArticle> currentArticles;
    
    // Content panels for different sections
    private JPanel dashboardPanel;
    private JPanel newsFetchPanel;
    private JPanel analyticsPanel;
    private JPanel chartsPanel;
    private JPanel keywordsPanel;
    private JPanel aiSummaryPanel;
    private JPanel translationPanel;
    private JPanel newsAppPanel;
    private JPanel historyPanel;
    private JPanel settingsPanel;
    
    // UI Components for News Fetch
    private ModernUIComponents.ModernComboBox countryCombo;
    private ModernUIComponents.ModernComboBox categoryCombo;
    private ModernUIComponents.ModernButton fetchButton;
    private ModernUIComponents.ModernProgressBar progressBar;
    private JLabel statusLabel;
    
    // UI Components for AI Summary
    private ModernUIComponents.ModernTextField urlField;
    private ModernUIComponents.ModernButton summarizeButton;
    
    // Results display
    private JTable articlesTable;
    private DefaultTableModel tableModel;
    
    public EnhancedMainWindow() {
        initializeServices();
        
        // Show login if needed
        if (!sessionManager.isLoggedIn()) {
            showLoginWindow();
        }
        
        initializeWindow();
        createSidebar();
        createContentArea();
        createAllContentPanels();
        
        setLocationRelativeTo(null);
        
        // Start with dashboard
        sidebar.selectNavigationItem(ModernSidebar.NavigationItem.DASHBOARD);
        showContent("dashboard");
    }
    
    private void initializeServices() {
        newsService = new NewsApiService();
        sessionManager = SessionManager.getInstance();
        databaseService = DatabaseService.getInstance();
    }
    
    private void showLoginWindow() {
        LoginWindow loginWindow = new LoginWindow(this);
        loginWindow.setVisible(true);
        
        if (!loginWindow.isLoginSuccessful()) {
            System.exit(0);
        }
    }
    
    private void initializeWindow() {
        setTitle("NewsVisualizer - Professional News Analysis Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Get screen dimensions and calculate optimal window size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        
        // Calculate window size as 85% of screen size for optimal viewing
        int windowWidth = (int) (screenWidth * 0.85);
        int windowHeight = (int) (screenHeight * 0.85);
        
        // Set minimum and maximum bounds
        int minWidth = Math.max(1200, screenWidth / 2);  // At least 1200px or half screen
        int minHeight = Math.max(700, screenHeight / 2); // At least 700px or half screen
        int maxWidth = screenWidth - 100;  // Leave some margin
        int maxHeight = screenHeight - 100; // Leave some margin for taskbar/dock
        
        // Apply bounds
        windowWidth = Math.min(Math.max(windowWidth, minWidth), maxWidth);
        windowHeight = Math.min(Math.max(windowHeight, minHeight), maxHeight);
        
        setSize(windowWidth, windowHeight);
        setMinimumSize(new Dimension(minWidth, minHeight));
        setMaximumSize(new Dimension(maxWidth, maxHeight));
        
        // Make window resizable and set extended state
        setResizable(true);
        
        // Check if the calculated size fits the screen well, otherwise maximize
        if (windowWidth > screenWidth * 0.9 || windowHeight > screenHeight * 0.9) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        
        // Set window background
        getContentPane().setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        
        // Log window size for debugging
        System.out.println(String.format("Screen: %dx%d, Window: %dx%d, Min: %dx%d", 
                          screenWidth, screenHeight, windowWidth, windowHeight, minWidth, minHeight));
    }
    
    private void createSidebar() {
        sidebar = new ModernSidebar();
        sidebar.setNavigationListener(this);
        add(sidebar, BorderLayout.WEST);
    }
    
    private void createContentArea() {
        contentCardLayout = new CardLayout();
        contentArea = new JPanel(contentCardLayout);
        
        // Use responsive padding
        int padding = EnhancedModernTheme.Layout.getContentPadding();
        contentArea.setBorder(new EmptyBorder(padding, padding, padding, padding));
        contentArea.setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        
        add(contentArea, BorderLayout.CENTER);
    }
    
    private void createAllContentPanels() {
        createDashboardPanel();
        createNewsFetchPanel();
        createAnalyticsPanel();
        createChartsPanel();
        createKeywordsPanel();
        createAISummaryPanel();
        createTranslationPanel();
        createNewsAppPanel();
        createHistoryPanel();
        createSettingsPanel();
    }
    
    private void createDashboardPanel() {
        dashboardPanel = createContentPanel("Dashboard", "Welcome to NewsVisualizer");
        
        // Responsive grid layout based on screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int rows, cols;
        if (screenSize.width <= 1366) {  // Small screens - single column layout
            rows = 4; cols = 1;
        } else {  // Larger screens - 2x2 grid
            rows = 2; cols = 2;
        }
        
        JPanel content = new JPanel(new GridLayout(rows, cols, EnhancedModernTheme.Spacing.LARGE, EnhancedModernTheme.Spacing.LARGE));
        content.setOpaque(false);
        
        // Quick stats cards
        content.add(createStatsCard(EnhancedModernTheme.Icons.NEWS, "Total Articles", "0", EnhancedModernTheme.Colors.PRIMARY));
        content.add(createStatsCard(EnhancedModernTheme.Icons.ANALYTICS, "Analyses Done", "0", EnhancedModernTheme.Colors.SECONDARY));
        content.add(createStatsCard(EnhancedModernTheme.Icons.CHARTS, "Sentiment Score", "0.0", EnhancedModernTheme.Colors.WARNING));
        content.add(createStatsCard(EnhancedModernTheme.Icons.HISTORY, "Search History", "0", EnhancedModernTheme.Colors.ACCENT));
        
        dashboardPanel.add(content, BorderLayout.CENTER);
        contentArea.add(dashboardPanel, "dashboard");
    }
    
    private void createNewsFetchPanel() {
        newsFetchPanel = createContentPanel("News Fetch", "Fetch and analyze news articles");
        
        // Create fetch controls
        JPanel controlsPanel = createControlsCard();
        newsFetchPanel.add(controlsPanel, BorderLayout.NORTH);
        
        // Create results area
        JPanel resultsPanel = createResultsCard();
        newsFetchPanel.add(resultsPanel, BorderLayout.CENTER);
        
        contentArea.add(newsFetchPanel, "news_fetch");
    }
    
    private JPanel createControlsCard() {
        ModernUIComponents.ModernCard controlsCard = new ModernUIComponents.ModernCard("Fetch Controls");
        controlsCard.setLayout(new BorderLayout());
        controlsCard.setPreferredSize(new Dimension(0, 180));
        
        JPanel controlsContent = new JPanel(new GridLayout(2, 1, 0, EnhancedModernTheme.Spacing.MEDIUM));
        controlsContent.setOpaque(false);
        controlsContent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        // Selection row
        JPanel selectionRow = new JPanel(new GridLayout(1, 2, EnhancedModernTheme.Spacing.LARGE, 0));
        selectionRow.setOpaque(false);
        
        countryCombo = ModernUIComponents.createComboBox(new String[]{
            "United States (us)", "India (in)", "United Kingdom (gb)", "Canada (ca)",
            "Australia (au)", "Germany (de)", "France (fr)", "Japan (jp)", "China (cn)"
        });
        countryCombo.setSelectedItem("India (in)");
        
        categoryCombo = ModernUIComponents.createComboBox(new String[]{
            "General", "Business", "Entertainment", "Health", "Science", "Sports", "Technology"
        });
        
        JPanel countryPanel = createFormGroup("Country:", countryCombo);
        JPanel categoryPanel = createFormGroup("Category:", categoryCombo);
        
        selectionRow.add(countryPanel);
        selectionRow.add(categoryPanel);
        
        // Button and progress row
        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.setOpaque(false);
        
        fetchButton = ModernUIComponents.createPrimaryButton(EnhancedModernTheme.Icons.NEWS + " Fetch News");
        fetchButton.addActionListener(e -> fetchNews());
        
        progressBar = ModernUIComponents.createProgressBar();
        progressBar.setString("Ready");
        
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtonPanel.setOpaque(false);
        leftButtonPanel.add(fetchButton);
        
        buttonRow.add(leftButtonPanel, BorderLayout.WEST);
        buttonRow.add(progressBar, BorderLayout.CENTER);
        
        controlsContent.add(selectionRow);
        controlsContent.add(buttonRow);
        
        controlsCard.add(controlsContent, BorderLayout.CENTER);
        
        return controlsCard;
    }
    
    private JPanel createResultsCard() {
        ModernUIComponents.ModernCard resultsCard = new ModernUIComponents.ModernCard("Articles");
        resultsCard.setLayout(new BorderLayout());
        
        // Create table
        String[] columnNames = {"Title", "Source", "Published At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        articlesTable = new JTable(tableModel);
        articlesTable.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        
        // Responsive row height based on screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int rowHeight = screenSize.width <= 1366 ? 50 : 60; // Smaller height for small screens
        articlesTable.setRowHeight(rowHeight);
        articlesTable.getTableHeader().setFont(EnhancedModernTheme.Fonts.LABEL);
        articlesTable.getTableHeader().setPreferredSize(new Dimension(0, 45)); // Larger header
        articlesTable.setSelectionBackground(EnhancedModernTheme.Colors.PRIMARY_LIGHT);
        articlesTable.setGridColor(EnhancedModernTheme.Colors.BORDER_LIGHT);
        articlesTable.setShowGrid(true);
        articlesTable.setShowHorizontalLines(true);
        articlesTable.setShowVerticalLines(true);
        
        // Responsive column widths based on screen size
        int titleWidth, sourceWidth, dateWidth;
        int titleMinWidth, sourceMinWidth, dateMinWidth;
        
        if (screenSize.width <= 1366) {  // Small laptops
            titleWidth = 350; sourceWidth = 140; dateWidth = 120;
            titleMinWidth = 200; sourceMinWidth = 100; dateMinWidth = 80;
        } else if (screenSize.width <= 1920) {  // Standard screens
            titleWidth = 500; sourceWidth = 200; dateWidth = 160;
            titleMinWidth = 300; sourceMinWidth = 120; dateMinWidth = 100;
        } else {  // Large screens
            titleWidth = 600; sourceWidth = 250; dateWidth = 180;
            titleMinWidth = 350; sourceMinWidth = 150; dateMinWidth = 120;
        }
        
        articlesTable.getColumnModel().getColumn(0).setPreferredWidth(titleWidth);
        articlesTable.getColumnModel().getColumn(1).setPreferredWidth(sourceWidth);
        articlesTable.getColumnModel().getColumn(2).setPreferredWidth(dateWidth);
        
        articlesTable.getColumnModel().getColumn(0).setMinWidth(titleMinWidth);
        articlesTable.getColumnModel().getColumn(1).setMinWidth(sourceMinWidth);
        articlesTable.getColumnModel().getColumn(2).setMinWidth(dateMinWidth);
        
        JScrollPane scrollPane = new JScrollPane(articlesTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(EnhancedModernTheme.Colors.WHITE);
        
        // Status bar
        statusLabel = new JLabel("✨ Welcome! Select your preferences and click 'Fetch News' to begin");
        statusLabel.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        statusLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        statusLabel.setBorder(new EmptyBorder(EnhancedModernTheme.Spacing.MEDIUM, 0, 0, 0));
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(statusLabel, BorderLayout.SOUTH);
        
        resultsCard.add(contentPanel, BorderLayout.CENTER);
        
        return resultsCard;
    }
    
    private void createAnalyticsPanel() {
        analyticsPanel = createContentPanel("Analytics", "Analyze sentiment and patterns in news data");
        
        // Analytics will be populated when data is analyzed
        JPanel placeholder = createPlaceholderPanel(
            EnhancedModernTheme.Icons.ANALYTICS + " Analytics",
            "Fetch news articles first, then analytics will appear here",
            EnhancedModernTheme.Colors.SECONDARY
        );
        
        analyticsPanel.add(placeholder, BorderLayout.CENTER);
        contentArea.add(analyticsPanel, "analytics");
    }
    
    private void createChartsPanel() {
        chartsPanel = createContentPanel("Sentiment Charts", "Visual representation of sentiment analysis");
        
        JPanel placeholder = createPlaceholderPanel(
            EnhancedModernTheme.Icons.CHARTS + " Charts",
            "Charts will appear here after analyzing news data",
            EnhancedModernTheme.Colors.PRIMARY
        );
        
        chartsPanel.add(placeholder, BorderLayout.CENTER);
        contentArea.add(chartsPanel, "charts");
    }
    
    private void createKeywordsPanel() {
        keywordsPanel = createContentPanel("Keywords Analysis", "Most frequent keywords and topics");
        
        JPanel placeholder = createPlaceholderPanel(
            EnhancedModernTheme.Icons.FILTER + " Keywords",
            "Keyword analysis will appear here after processing articles",
            EnhancedModernTheme.Colors.WARNING
        );
        
        keywordsPanel.add(placeholder, BorderLayout.CENTER);
        contentArea.add(keywordsPanel, "keywords");
    }
    
    private void createAISummaryPanel() {
        aiSummaryPanel = createContentPanel("AI Summary", "Generate AI-powered article summaries");
        
        // Create AI summary controls
        JPanel controlsPanel = createAISummaryControls();
        aiSummaryPanel.add(controlsPanel, BorderLayout.NORTH);
        
        // Create summary display area
        JPanel summaryDisplay = createSummaryDisplayCard();
        aiSummaryPanel.add(summaryDisplay, BorderLayout.CENTER);
        
        contentArea.add(aiSummaryPanel, "ai_summary");
    }
    
    private JPanel createAISummaryControls() {
        ModernUIComponents.ModernCard controlsCard = new ModernUIComponents.ModernCard("Article URL");
        controlsCard.setLayout(new BorderLayout());
        controlsCard.setPreferredSize(new Dimension(0, 140));
        
        JPanel controlsContent = new JPanel(new BorderLayout());
        controlsContent.setOpaque(false);
        controlsContent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        urlField = ModernUIComponents.createTextField(50);
        urlField.setToolTipText("Enter article URL for AI-powered summary");
        
        summarizeButton = ModernUIComponents.createAccentButton(
            EnhancedModernTheme.Icons.AI + " Generate Summary"
        );
        summarizeButton.addActionListener(e -> summarizeArticle());
        
        JPanel urlPanel = createFormGroup("Enter article URL:", urlField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(summarizeButton);
        
        controlsContent.add(urlPanel, BorderLayout.CENTER);
        controlsContent.add(buttonPanel, BorderLayout.SOUTH);
        
        controlsCard.add(controlsContent, BorderLayout.CENTER);
        
        return controlsCard;
    }
    
    private JPanel createSummaryDisplayCard() {
        ModernUIComponents.ModernCard displayCard = new ModernUIComponents.ModernCard("Summary Results");
        displayCard.setLayout(new BorderLayout());
        
        JLabel placeholderLabel = new JLabel(
            "<html><div style='text-align: center; padding: 40px;'>" +
            "<h2>" + EnhancedModernTheme.Icons.AI + " AI Article Summarizer</h2>" +
            "<p style='color: #666; font-size: 14px;'>Enter an article URL above and click 'Generate Summary' to see AI-powered analysis</p>" +
            "</div></html>"
        );
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderLabel.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        contentPanel.add(placeholderLabel, BorderLayout.CENTER);
        
        displayCard.add(contentPanel, BorderLayout.CENTER);
        
        return displayCard;
    }
    
    private void createTranslationPanel() {
        translationPanel = createContentPanel("Translation & Dictionary", "Translate text and understand news terms");
        
        // Create the actual translation panel using the existing TranslationPanel class
        TranslationPanel translationComponent = new TranslationPanel();
        translationComponent.setOpaque(false);
        translationComponent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        translationPanel.add(translationComponent, BorderLayout.CENTER);
        contentArea.add(translationPanel, "translation");
    }
    
    private void createNewsAppPanel() {
        newsAppPanel = createContentPanel("NewsApp Integration", "Browse and search news articles");
        
        // Create the actual NewsApp panel using the existing NewsAppPanel class
        NewsAppPanel newsAppComponent = new NewsAppPanel();
        newsAppComponent.setOpaque(false);
        newsAppComponent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        newsAppPanel.add(newsAppComponent, BorderLayout.CENTER);
        contentArea.add(newsAppPanel, "newsapp");
    }
    
    private void createHistoryPanel() {
        historyPanel = createContentPanel("Search History", "View your previous searches and user profile");
        
        // Create history and profile controls
        JPanel historyContent = createHistoryContent();
        historyPanel.add(historyContent, BorderLayout.CENTER);
        
        contentArea.add(historyPanel, "history");
    }
    
    private JPanel createHistoryContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        
        // User profile card
        ModernUIComponents.ModernCard profileCard = new ModernUIComponents.ModernCard("User Profile");
        profileCard.setLayout(new BorderLayout());
        profileCard.setPreferredSize(new Dimension(0, 200));
        
        JPanel profileContent = new JPanel(new BorderLayout());
        profileContent.setOpaque(false);
        profileContent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        // User info display
        if (sessionManager.isLoggedIn()) {
            User currentUser = sessionManager.getCurrentUser();
            String userInfo = String.format(
                "<html><div style='text-align: center;'>" +
                "<h3>%s Welcome, %s!</h3>" +
                "<p>Email: %s</p>" +
                "<p>Member since: %s</p>" +
                "</div></html>",
                EnhancedModernTheme.Icons.USER, 
                currentUser.getFirstName() + " " + currentUser.getLastName(),
                currentUser.getEmail(),
                "Registration date"
            );
            
            JLabel userLabel = new JLabel(userInfo);
            userLabel.setHorizontalAlignment(SwingConstants.CENTER);
            userLabel.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
            profileContent.add(userLabel, BorderLayout.CENTER);
            
            // Profile button
            ModernUIComponents.ModernButton profileButton = ModernUIComponents.createSecondaryButton(
                EnhancedModernTheme.Icons.PROFILE + " View Full Profile"
            );
            profileButton.addActionListener(e -> openUserProfile());
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setOpaque(false);
            buttonPanel.add(profileButton);
            profileContent.add(buttonPanel, BorderLayout.SOUTH);
            
        } else {
            JLabel loginPrompt = new JLabel(
                "<html><div style='text-align: center;'>" +
                "<h3>" + EnhancedModernTheme.Icons.LOGIN + " Please Login</h3>" +
                "<p>Login to view your profile and search history</p>" +
                "</div></html>"
            );
            loginPrompt.setHorizontalAlignment(SwingConstants.CENTER);
            profileContent.add(loginPrompt, BorderLayout.CENTER);
        }
        
        profileCard.add(profileContent, BorderLayout.CENTER);
        
        // Search history placeholder
        ModernUIComponents.ModernCard historyCard = new ModernUIComponents.ModernCard("Recent Searches");
        historyCard.setLayout(new BorderLayout());
        
        JLabel historyLabel = new JLabel(
            "<html><div style='text-align: center; padding: 40px;'>" +
            "<h3>" + EnhancedModernTheme.Icons.HISTORY + " Search History</h3>" +
            "<p style='color: #666;'>Your recent news searches will appear here</p>" +
            "</div></html>"
        );
        historyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel historyContent = new JPanel(new BorderLayout());
        historyContent.setOpaque(false);
        historyContent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        historyContent.add(historyLabel, BorderLayout.CENTER);
        
        historyCard.add(historyContent, BorderLayout.CENTER);
        
        content.add(profileCard, BorderLayout.NORTH);
        content.add(historyCard, BorderLayout.CENTER);
        
        return content;
    }
    
    private void openUserProfile() {
        if (sessionManager.isLoggedIn()) {
            UserProfileWindow profileWindow = new UserProfileWindow(this);
            profileWindow.setVisible(true);
        }
    }
    
    private void createSettingsPanel() {
        settingsPanel = createContentPanel("Settings", "Application preferences and configuration");
        
        // Create settings content
        JPanel settingsContent = createSettingsContent();
        settingsPanel.add(settingsContent, BorderLayout.CENTER);
        
        contentArea.add(settingsPanel, "settings");
    }
    
    private JPanel createSettingsContent() {
        // Responsive layout for settings
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int rows, cols;
        if (screenSize.width <= 1366) {  // Small screens - single column
            rows = 4; cols = 1;
        } else {  // Larger screens - 2x2 grid
            rows = 2; cols = 2;
        }
        
        JPanel content = new JPanel(new GridLayout(rows, cols, EnhancedModernTheme.Spacing.LARGE, EnhancedModernTheme.Spacing.LARGE));
        content.setOpaque(false);
        
        // Theme Settings Card
        ModernUIComponents.ModernCard themeCard = new ModernUIComponents.ModernCard("Theme Settings");
        themeCard.add(createPlaceholderPanel(
            EnhancedModernTheme.Icons.SETTINGS + " Theme",
            "Theme customization options",
            EnhancedModernTheme.Colors.PRIMARY
        ));
        
        // Data Settings Card
        ModernUIComponents.ModernCard dataCard = new ModernUIComponents.ModernCard("Data Settings");
        dataCard.add(createPlaceholderPanel(
            EnhancedModernTheme.Icons.DATABASE + " Data",
            "Data management and export options",
            EnhancedModernTheme.Colors.SECONDARY
        ));
        
        // API Settings Card
        ModernUIComponents.ModernCard apiCard = new ModernUIComponents.ModernCard("API Settings");
        apiCard.add(createPlaceholderPanel(
            EnhancedModernTheme.Icons.API + " API",
            "News API and service configurations",
            EnhancedModernTheme.Colors.WARNING
        ));
        
        // About Card
        ModernUIComponents.ModernCard aboutCard = new ModernUIComponents.ModernCard("About");
        aboutCard.add(createPlaceholderPanel(
            EnhancedModernTheme.Icons.INFO + " About",
            "NewsVisualizer v1.0 - Professional News Analysis",
            EnhancedModernTheme.Colors.GRAY_500
        ));
        
        content.add(themeCard);
        content.add(dataCard);
        content.add(apiCard);
        content.add(aboutCard);
        
        return content;
    }
    
    private JPanel createContentPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, EnhancedModernTheme.Spacing.LARGE, 0));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(EnhancedModernTheme.Fonts.SECTION_TITLE);
        titleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        subtitleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(4));
        titleContainer.add(subtitleLabel);
        
        header.add(titleContainer, BorderLayout.WEST);
        
        panel.add(header, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createStatsCard(String icon, String title, String value, Color accentColor) {
        ModernUIComponents.ModernCard card = new ModernUIComponents.ModernCard("");
        card.setLayout(new BorderLayout());
        
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 
            EnhancedModernTheme.Spacing.LARGE,
            EnhancedModernTheme.Spacing.LARGE, 
            EnhancedModernTheme.Spacing.LARGE
        ));
        
        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 32));
        iconLabel.setForeground(accentColor);
        
        // Title and value
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        titleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(EnhancedModernTheme.Fonts.CARD_TITLE);
        valueLabel.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        
        content.add(iconLabel, BorderLayout.WEST);
        content.add(textPanel, BorderLayout.CENTER);
        
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createFormGroup(String labelText, JComponent component) {
        JPanel group = new JPanel(new BorderLayout());
        group.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(EnhancedModernTheme.Fonts.LABEL);
        label.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        label.setBorder(new EmptyBorder(0, 0, EnhancedModernTheme.Spacing.SMALL, 0));
        
        group.add(label, BorderLayout.NORTH);
        group.add(component, BorderLayout.CENTER);
        
        return group;
    }
    
    private JPanel createPlaceholderPanel(String title, String description, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'><h2>" + title + "</h2></div></html>");
        titleLabel.setFont(EnhancedModernTheme.Fonts.CARD_TITLE);
        titleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel descLabel = new JLabel("<html><div style='text-align: center; color: #666;'>" + description + "</div></html>");
        descLabel.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(Box.createVerticalGlue());
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(EnhancedModernTheme.Spacing.MEDIUM));
        content.add(descLabel);
        content.add(Box.createVerticalGlue());
        
        panel.add(content, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Navigation handling
    @Override
    public void onNavigationItemSelected(ModernSidebar.NavigationItem item) {
        switch (item) {
            case DASHBOARD:
                showContent("dashboard");
                break;
            case NEWS_FETCH:
                showContent("news_fetch");
                break;
            case ANALYTICS:
                showContent("analytics");
                break;
            case CHARTS:
                showContent("charts");
                break;
            case KEYWORDS:
                showContent("keywords");
                break;
        case AI_SUMMARY:
                showContent("ai_summary");
                break;
            case TRANSLATION:
                showContent("translation");
                break;
            case NEWSAPP:
                showContent("newsapp");
                break;
            case HISTORY:
                showContent("history");
                break;
            case SETTINGS:
                showContent("settings");
                break;
        }
    }
    
    private void showContent(String contentName) {
        contentCardLayout.show(contentArea, contentName);
    }
    
    // News fetching functionality
    private void fetchNews() {
        SwingWorker<NewsResponse, Void> worker = new SwingWorker<NewsResponse, Void>() {
            @Override
            protected NewsResponse doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    fetchButton.setEnabled(false);
                    progressBar.setIndeterminate(true);
                    progressBar.setString("Fetching news...");
                    statusLabel.setText("📡 Fetching news articles...");
                });
                
                String countrySelection = (String) countryCombo.getSelectedItem();
                String categorySelection = (String) categoryCombo.getSelectedItem();
                
                // Extract country code
                String country = null;
                if (countrySelection != null && countrySelection.contains("(")) {
                    String extracted = countrySelection.substring(
                        countrySelection.lastIndexOf("(") + 1, 
                        countrySelection.lastIndexOf(")")
                    );
                    if (!extracted.isEmpty()) {
                        country = extracted;
                    }
                }
                
                // Convert category
                String category = null;
                if (categorySelection != null && !categorySelection.equals("General")) {
                    category = categorySelection.toLowerCase();
                }
                
                return newsService.getTopHeadlines(country, category);
            }
            
            @Override
            protected void done() {
                try {
                    NewsResponse response = get();
                    processNewsResponse(response);
                } catch (Exception e) {
                    showError("Error fetching news: " + e.getMessage());
                } finally {
                    fetchButton.setEnabled(true);
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
            
            String message = String.format(
                "📰 Successfully fetched %d articles. Navigate to Analytics or Charts to analyze the data!",
                currentArticles.size()
            );
            statusLabel.setText(message);
            
            // Auto-analyze data
            analyzeData();
            
        } else {
            String message = response.getMessage() != null ? response.getMessage() : "No articles found";
            statusLabel.setText("⚠️ " + message);
        }
    }
    
    private void updateArticlesTable() {
        tableModel.setRowCount(0);
        
        for (NewsArticle article : currentArticles) {
            Object[] row = {
                article.getTitle(),
                article.getSource() != null ? article.getSource().getName() : "Unknown",
                article.getPublishedAt() != null ?
                    article.getPublishedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Unknown"
            };
            tableModel.addRow(row);
        }
    }
    
    private void analyzeData() {
        if (currentArticles == null || currentArticles.isEmpty()) {
            return;
        }
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("🔄 Analyzing sentiment and generating charts...");
                });
                
                // Perform sentiment analysis
                NewsAnalyzer.analyzeSentiment(currentArticles);
                
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    generateVisualizations();
                    statusLabel.setText(
                        "✅ Analysis complete! Check Analytics and Charts sections for detailed insights."
                    );
                } catch (Exception e) {
                    showError("Error analyzing data: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void generateVisualizations() {
        // Generate analysis data
        Map<String, Integer> sentimentDist = NewsAnalyzer.getSentimentDistribution(currentArticles);
        Map<String, Integer> sourceDist = NewsAnalyzer.getSourceDistribution(currentArticles);
        Map<String, Integer> keywords = NewsAnalyzer.extractKeywords(currentArticles, 20);
        
        // Update analytics panel
        updateAnalyticsPanel(sentimentDist, sourceDist);
        
        // Update charts panel
        updateChartsPanel(sentimentDist, sourceDist);
        
        // Update keywords panel
        updateKeywordsPanel(keywords);
    }
    
    private void updateAnalyticsPanel(Map<String, Integer> sentimentDist, Map<String, Integer> sourceDist) {
        analyticsPanel.removeAll();
        
        // Recreate header
        JPanel header = createContentPanelHeader("Analytics", "Sentiment and source analysis results");
        analyticsPanel.add(header, BorderLayout.NORTH);
        
        // Create analytics content with responsive layout
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int rows, cols;
        if (screenSize.width <= 1366) {  // Small screens - vertical stack
            rows = 2; cols = 1;
        } else {  // Larger screens - side by side
            rows = 1; cols = 2;
        }
        
        JPanel content = new JPanel(new GridLayout(rows, cols, EnhancedModernTheme.Spacing.LARGE, EnhancedModernTheme.Spacing.LARGE));
        content.setOpaque(false);
        
        // Sentiment stats
        ModernUIComponents.ModernCard sentimentCard = new ModernUIComponents.ModernCard("Sentiment Distribution");
        sentimentCard.setLayout(new BorderLayout());
        
        JPanel sentimentContent = new JPanel();
        sentimentContent.setLayout(new BoxLayout(sentimentContent, BoxLayout.Y_AXIS));
        sentimentContent.setOpaque(false);
        sentimentContent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        for (Map.Entry<String, Integer> entry : sentimentDist.entrySet()) {
            JLabel statLabel = new JLabel(
                String.format("%s: %d articles", 
                    entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1), 
                    entry.getValue()
                )
            );
            statLabel.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
            statLabel.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
            sentimentContent.add(statLabel);
            sentimentContent.add(Box.createVerticalStrut(8));
        }
        
        sentimentCard.add(sentimentContent, BorderLayout.CENTER);
        
        // Source stats
        ModernUIComponents.ModernCard sourceCard = new ModernUIComponents.ModernCard("Top Sources");
        sourceCard.setLayout(new BorderLayout());
        
        JPanel sourceContent = new JPanel();
        sourceContent.setLayout(new BoxLayout(sourceContent, BoxLayout.Y_AXIS));
        sourceContent.setOpaque(false);
        sourceContent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        int count = 0;
        for (Map.Entry<String, Integer> entry : sourceDist.entrySet()) {
            if (count >= 5) break; // Show top 5 sources
            
            JLabel sourceLabel = new JLabel(
                String.format("%s: %d articles", entry.getKey(), entry.getValue())
            );
            sourceLabel.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
            sourceLabel.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
            sourceContent.add(sourceLabel);
            sourceContent.add(Box.createVerticalStrut(8));
            count++;
        }
        
        sourceCard.add(sourceContent, BorderLayout.CENTER);
        
        content.add(sentimentCard);
        content.add(sourceCard);
        
        analyticsPanel.add(content, BorderLayout.CENTER);
        analyticsPanel.revalidate();
        analyticsPanel.repaint();
    }
    
    private void updateChartsPanel(Map<String, Integer> sentimentDist, Map<String, Integer> sourceDist) {
        chartsPanel.removeAll();
        
        // Recreate header
        JPanel header = createContentPanelHeader("Sentiment Charts", "Visual analysis of news sentiment");
        chartsPanel.add(header, BorderLayout.NORTH);
        
        // Create charts with responsive layout
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int chartRows, chartCols;
        if (screenSize.width <= 1366) {  // Small screens - vertical stack
            chartRows = 2; chartCols = 1;
        } else {  // Larger screens - side by side
            chartRows = 1; chartCols = 2;
        }
        
        JPanel chartContent = new JPanel(new GridLayout(chartRows, chartCols, EnhancedModernTheme.Spacing.LARGE, EnhancedModernTheme.Spacing.LARGE));
        chartContent.setOpaque(false);
        
        if (!sentimentDist.isEmpty()) {
            JPanel sentimentChart = ChartGenerator.createSentimentChart("Sentiment Distribution", sentimentDist);
            chartContent.add(sentimentChart);
        }
        
        if (!sourceDist.isEmpty()) {
            JPanel sourceChart = ChartGenerator.createHorizontalBarChart(
                "Top Sources", "Sources", "Number of Articles", sourceDist
            );
            chartContent.add(sourceChart);
        }
        
        chartsPanel.add(chartContent, BorderLayout.CENTER);
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
    
    private void updateKeywordsPanel(Map<String, Integer> keywords) {
        keywordsPanel.removeAll();
        
        // Recreate header
        JPanel header = createContentPanelHeader("Keywords Analysis", "Most frequently mentioned terms");
        keywordsPanel.add(header, BorderLayout.NORTH);
        
        if (!keywords.isEmpty()) {
            JPanel keywordChart = ChartGenerator.createWordFrequencyChart("Top Keywords", keywords);
            keywordsPanel.add(keywordChart, BorderLayout.CENTER);
        } else {
            JPanel placeholder = createPlaceholderPanel(
                EnhancedModernTheme.Icons.FILTER + " No Keywords",
                "No keywords found in the current data",
                EnhancedModernTheme.Colors.GRAY_400
            );
            keywordsPanel.add(placeholder, BorderLayout.CENTER);
        }
        
        keywordsPanel.revalidate();
        keywordsPanel.repaint();
    }
    
    private JPanel createContentPanelHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, EnhancedModernTheme.Spacing.LARGE, 0));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(EnhancedModernTheme.Fonts.SECTION_TITLE);
        titleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        subtitleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(4));
        titleContainer.add(subtitleLabel);
        
        header.add(titleContainer, BorderLayout.WEST);
        
        return header;
    }
    
    // AI Summary functionality
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
                    summarizeButton.setText("🔄 Summarizing...");
                });
                
                return ArticleSummarizer.summarizeFromUrl(url);
            }
            
            @Override
            protected void done() {
                try {
                    ArticleSummarizer.ArticleSummary summary = get();
                    displayArticleSummary(summary);
                } catch (Exception e) {
                    showError("Error summarizing article: " + e.getMessage());
                } finally {
                    summarizeButton.setEnabled(true);
                    summarizeButton.setText(EnhancedModernTheme.Icons.AI + " Generate Summary");
                }
            }
        };
        
        worker.execute();
    }
    
    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
    
    private void displayArticleSummary(ArticleSummarizer.ArticleSummary summary) {
        // Find and update the summary display card in AI Summary panel
        Component[] components = aiSummaryPanel.getComponents();
        for (Component component : components) {
            if (component instanceof ModernUIComponents.ModernCard) {
                ModernUIComponents.ModernCard card = (ModernUIComponents.ModernCard) component;
                // Update the summary display card (not the controls card)
                if (component == aiSummaryPanel.getComponent(1)) { // Summary display card
                    updateSummaryDisplay(card, summary);
                    break;
                }
            }
        }
    }
    
    private void updateSummaryDisplay(ModernUIComponents.ModernCard card, ArticleSummarizer.ArticleSummary summary) {
        card.removeAll();
        
        JPanel summaryContent = new JPanel(new BorderLayout());
        summaryContent.setOpaque(false);
        summaryContent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        // Title
        JLabel titleLabel = new JLabel("<html><h3>📄 " + summary.getTitle() + "</h3></html>");
        titleLabel.setFont(EnhancedModernTheme.Fonts.CARD_TITLE);
        titleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
        
        // Summary text in a larger, scrollable area
        JTextArea summaryText = new JTextArea(summary.getSummary());
        summaryText.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        summaryText.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
        summaryText.setBackground(EnhancedModernTheme.Colors.WHITE);
        summaryText.setLineWrap(true);
        summaryText.setWrapStyleWord(true);
        summaryText.setEditable(false);
        summaryText.setBorder(new EmptyBorder(12, 12, 12, 12));
        
        JScrollPane scrollPane = new JScrollPane(summaryText);
        scrollPane.setBorder(BorderFactory.createLineBorder(EnhancedModernTheme.Colors.BORDER_LIGHT));
        scrollPane.setPreferredSize(new Dimension(0, 300)); // Larger display area
        
        // Key points if available
        if (summary.getKeyPoints() != null && !summary.getKeyPoints().isEmpty()) {
            JPanel keyPointsPanel = new JPanel(new BorderLayout());
            keyPointsPanel.setOpaque(false);
            keyPointsPanel.setBorder(new EmptyBorder(EnhancedModernTheme.Spacing.MEDIUM, 0, 0, 0));
            
            JLabel keyPointsTitle = new JLabel("🎯 Key Points:");
            keyPointsTitle.setFont(EnhancedModernTheme.Fonts.LABEL);
            keyPointsTitle.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
            
            StringBuilder keyPointsText = new StringBuilder("<html><ul>");
            for (String point : summary.getKeyPoints()) {
                keyPointsText.append("<li>").append(point).append("</li>");
            }
            keyPointsText.append("</ul></html>");
            
            JLabel keyPointsLabel = new JLabel(keyPointsText.toString());
            keyPointsLabel.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
            keyPointsLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
            
            keyPointsPanel.add(keyPointsTitle, BorderLayout.NORTH);
            keyPointsPanel.add(keyPointsLabel, BorderLayout.CENTER);
            
            summaryContent.add(keyPointsPanel, BorderLayout.SOUTH);
        }
        
        summaryContent.add(titleLabel, BorderLayout.NORTH);
        summaryContent.add(Box.createVerticalStrut(EnhancedModernTheme.Spacing.MEDIUM), BorderLayout.CENTER);
        summaryContent.add(scrollPane, BorderLayout.CENTER);
        
        card.add(summaryContent, BorderLayout.CENTER);
        card.revalidate();
        card.repaint();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    @Override
    public void dispose() {
        if (newsService != null) {
            newsService.close();
        }
        super.dispose();
    }
}