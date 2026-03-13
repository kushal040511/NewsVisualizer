package com.newsvisualizer.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.newsvisualizer.gui.components.ModernSidebar;
import com.newsvisualizer.gui.components.ModernUIComponents;
import com.newsvisualizer.gui.theme.EnhancedModernTheme;
import com.newsvisualizer.model.NewsArticle;
import com.newsvisualizer.model.NewsResponse;
import com.newsvisualizer.service.NewsApiService;
import com.newsvisualizer.service.DatabaseService;
import com.newsvisualizer.utils.NewsAnalyzer;
import com.newsvisualizer.utils.ArticleSummarizer;
import com.newsvisualizer.visualization.ChartGenerator;
import com.newsvisualizer.gui.TranslationPanel;
import com.newsvisualizer.gui.NewsAppPanel;
import com.newsvisualizer.model.SearchHistory;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Enhanced main window with modern sidebar navigation and clean layout
 */
public class EnhancedMainWindow extends JFrame implements ModernSidebar.NavigationListener {
    private static final Path HISTORY_FILE = Path.of("data", "activity-history.json");
    private static final long LOCAL_HISTORY_USER_ID = 1L;
    private static final DateTimeFormatter HISTORY_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private ModernSidebar sidebar;
    private JPanel contentArea;
    private CardLayout contentCardLayout;
    
    // Services
    private NewsApiService newsService;
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
    private TranslationPanel translationComponent;
    private JPanel newsAppPanel;
    private JPanel historyPanel;
    private JPanel settingsPanel;
    
    // UI Components for News Fetch
    private ModernUIComponents.ModernComboBox countryCombo;
    private ModernUIComponents.ModernComboBox categoryCombo;
    private ModernUIComponents.ModernButton fetchButton;
    private ModernUIComponents.ModernProgressBar progressBar;
    private JLabel fetchProgressLabel;
    private JLabel statusLabel;
    
    // UI Components for AI Summary
    private JTextField urlField;
    private ModernUIComponents.ModernButton summarizeButton;
    private ModernUIComponents.ModernButton summarizeSelectedButton;
    private JTextArea summaryTextArea;
    private JLabel summaryTitleLabel;
    private JLabel summaryMetaLabel;
    private JLabel summaryKeywordsLabel;

    // Dashboard and history state
    private JLabel totalArticlesValueLabel;
    private JLabel analysesDoneValueLabel;
    private JLabel sentimentScoreValueLabel;
    private JLabel searchHistoryValueLabel;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private ModernUIComponents.ModernButton clearHistoryButton;
    private final List<SearchHistory> recentActivity = new ArrayList<>();
    private int analysesRun = 0;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    // Results display
    private JTable articlesTable;
    private DefaultTableModel tableModel;
    
    public EnhancedMainWindow() {
        initializeServices();
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
        databaseService = DatabaseService.getInstance();
        loadHistoryFromDisk();
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
        totalArticlesValueLabel = new JLabel("0");
        analysesDoneValueLabel = new JLabel("0");
        sentimentScoreValueLabel = new JLabel("0.00");
        searchHistoryValueLabel = new JLabel("0");

        content.add(createStatsCard(EnhancedModernTheme.Icons.NEWS, "Total Articles", totalArticlesValueLabel, EnhancedModernTheme.Colors.PRIMARY));
        content.add(createStatsCard(EnhancedModernTheme.Icons.ANALYTICS, "Analyses Done", analysesDoneValueLabel, EnhancedModernTheme.Colors.SECONDARY));
        content.add(createStatsCard(EnhancedModernTheme.Icons.CHARTS, "Sentiment Score", sentimentScoreValueLabel, EnhancedModernTheme.Colors.WARNING));
        content.add(createStatsCard(EnhancedModernTheme.Icons.HISTORY, "Recent Actions", searchHistoryValueLabel, EnhancedModernTheme.Colors.ACCENT));
        
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
        controlsCard.setPreferredSize(new Dimension(0, 206));
        
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
            "Select Country",
            "United States",
            "India",
            "United Kingdom",
            "Japan",
            "Australia",
            "Canada",
            "Germany"
        });
        countryCombo.setSelectedIndex(0);
        countryCombo.setToolTipText("Choose the country for top headlines");
        countryCombo.setPrototypeDisplayValue("United Kingdom");
        
        categoryCombo = ModernUIComponents.createComboBox(new String[]{
            "general", "business", "technology", "health", "science", "sports", "entertainment"
        });
        categoryCombo.setToolTipText("Choose the category to filter headlines");
        categoryCombo.setPrototypeDisplayValue("entertainment");
        
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
        progressBar.setVisible(false);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(260, 10));

        fetchProgressLabel = new JLabel("Choose a country and category to fetch the latest headlines.");
        fetchProgressLabel.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        fetchProgressLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);

        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtonPanel.setOpaque(false);
        leftButtonPanel.add(fetchButton);

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(new EmptyBorder(0, EnhancedModernTheme.Spacing.LARGE, 0, 0));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        fetchProgressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressPanel.add(progressBar);
        progressPanel.add(Box.createVerticalStrut(8));
        progressPanel.add(fetchProgressLabel);
        
        buttonRow.add(leftButtonPanel, BorderLayout.WEST);
        buttonRow.add(progressPanel, BorderLayout.CENTER);
        
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
        articlesTable.setFillsViewportHeight(true);
        articlesTable.setIntercellSpacing(new Dimension(0, 0));
        articlesTable.setRowMargin(0);
        articlesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        articlesTable.setSelectionForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
        articlesTable.setBackground(EnhancedModernTheme.Colors.WHITE);
        
        // Responsive row height based on screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int rowHeight = screenSize.width <= 1366 ? 52 : 58;
        articlesTable.setRowHeight(rowHeight);
        articlesTable.setSelectionBackground(new Color(79, 70, 229, 24));
        articlesTable.setGridColor(EnhancedModernTheme.Colors.BORDER_LIGHT);
        articlesTable.setShowGrid(false);
        articlesTable.setShowHorizontalLines(false);
        articlesTable.setShowVerticalLines(false);
        articlesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        installTableStyle();
        
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
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnhancedModernTheme.Colors.BORDER_LIGHT, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        scrollPane.getViewport().setBackground(EnhancedModernTheme.Colors.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(true);
        
        // Status bar
        statusLabel = new JLabel("Select your preferences and fetch articles to begin analysis.");
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
        controlsCard.setPreferredSize(new Dimension(0, 190));
        
        JPanel controlsContent = new JPanel();
        controlsContent.setLayout(new BoxLayout(controlsContent, BoxLayout.Y_AXIS));
        controlsContent.setOpaque(false);
        controlsContent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        urlField = new JTextField(50);
        urlField.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        urlField.setToolTipText("Enter article URL for AI-powered summary");
        urlField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnhancedModernTheme.Colors.BORDER_LIGHT, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        urlField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        urlField.setPreferredSize(new Dimension(700, 44));
        urlField.setMinimumSize(new Dimension(300, 44));
        installTextFieldEditing(urlField);
        
        summarizeButton = ModernUIComponents.createAccentButton(
            EnhancedModernTheme.Icons.AI + " Generate Summary"
        );
        summarizeButton.addActionListener(e -> summarizeArticle());

        summarizeSelectedButton = ModernUIComponents.createSecondaryButton(
            EnhancedModernTheme.Icons.NEWS + " Summarize Selected Article"
        );
        summarizeSelectedButton.addActionListener(e -> summarizeSelectedArticle());
        
        JLabel fieldLabel = new JLabel("Enter article URL:");
        fieldLabel.setFont(EnhancedModernTheme.Fonts.LABEL);
        fieldLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        urlField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.add(summarizeButton);
        buttonPanel.add(Box.createHorizontalStrut(12));
        buttonPanel.add(summarizeSelectedButton);
        
        controlsContent.add(fieldLabel);
        controlsContent.add(Box.createVerticalStrut(8));
        controlsContent.add(urlField);
        controlsContent.add(Box.createVerticalStrut(14));
        controlsContent.add(buttonPanel);
        
        controlsCard.add(controlsContent, BorderLayout.CENTER);
        
        return controlsCard;
    }
    
    private JPanel createSummaryDisplayCard() {
        ModernUIComponents.ModernCard displayCard = new ModernUIComponents.ModernCard("Summary Results");
        displayCard.setLayout(new BorderLayout());
        
        summaryTitleLabel = new JLabel("AI Article Summarizer");
        summaryTitleLabel.setFont(EnhancedModernTheme.Fonts.CARD_TITLE);
        summaryTitleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);

        summaryMetaLabel = new JLabel("Paste a URL or select an article from the fetched news table.");
        summaryMetaLabel.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        summaryMetaLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);

        summaryTextArea = new JTextArea();
        summaryTextArea.setEditable(false);
        summaryTextArea.setLineWrap(true);
        summaryTextArea.setWrapStyleWord(true);
        summaryTextArea.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        summaryTextArea.setText(
            "Enter an article URL above and click 'Generate Summary', or fetch news and use " +
            "'Summarize Selected Article' for an article from the list."
        );

        summaryKeywordsLabel = new JLabel("No summary generated yet.");
        summaryKeywordsLabel.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        summaryKeywordsLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);

        JScrollPane scrollPane = new JScrollPane(summaryTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(EnhancedModernTheme.Colors.BORDER_LIGHT));
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.add(summaryTitleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(summaryMetaLabel);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(summaryKeywordsLabel, BorderLayout.SOUTH);
        
        displayCard.add(contentPanel, BorderLayout.CENTER);
        
        return displayCard;
    }
    
    private void createTranslationPanel() {
        translationPanel = createContentPanel("Translation & Dictionary", "Translate text and understand news terms");
        
        // Create the actual translation panel using the existing TranslationPanel class
        translationComponent = new TranslationPanel();
        translationComponent.setActivityListener((actionType, input, resultSummary) ->
            recordActivity(createTranslationHistoryItem(actionType, input, resultSummary))
        );
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
        
        ModernUIComponents.ModernCard profileCard = new ModernUIComponents.ModernCard("Workspace Overview");
        profileCard.setLayout(new BorderLayout());
        profileCard.setPreferredSize(new Dimension(0, 200));
        
        JPanel profileContent = new JPanel(new BorderLayout());
        profileContent.setOpaque(false);
        profileContent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        JLabel overviewLabel = new JLabel(
            "<html><div style='text-align: center;'>" +
            "<h3>" + EnhancedModernTheme.Icons.INFO + " Local Workspace</h3>" +
            "<p>The application now opens directly without authentication.</p>" +
            "<p>Use this section to review local search activity and app state.</p>" +
            "</div></html>"
        );
        overviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        overviewLabel.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        profileContent.add(overviewLabel, BorderLayout.CENTER);
        
        profileCard.add(profileContent, BorderLayout.CENTER);
        
        ModernUIComponents.ModernCard historyCard = new ModernUIComponents.ModernCard("Recent Searches");
        historyCard.setLayout(new BorderLayout());
        
        JPanel historyContent = new JPanel(new BorderLayout());
        historyContent.setOpaque(false);
        historyContent.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));

        JPanel historyActionsPanel = new JPanel(new BorderLayout());
        historyActionsPanel.setOpaque(false);
        JLabel tableHint = new JLabel("Recent first. Activity is stored locally for this workspace.");
        tableHint.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        tableHint.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        clearHistoryButton = ModernUIComponents.createSecondaryButton("Clear History");
        clearHistoryButton.addActionListener(e -> clearHistory());
        historyActionsPanel.add(tableHint, BorderLayout.WEST);
        historyActionsPanel.add(clearHistoryButton, BorderLayout.EAST);

        String[] historyColumns = {"Action", "Input", "Date", "Result Summary"};
        historyTableModel = new DefaultTableModel(historyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        historyTable.setRowHeight(46);
        historyTable.setShowVerticalLines(false);
        historyTable.setShowHorizontalLines(false);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setFillsViewportHeight(true);
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        historyTable.setBackground(EnhancedModernTheme.Colors.WHITE);
        installHistoryTableStyle();

        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnhancedModernTheme.Colors.BORDER_LIGHT, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        historyScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        historyScrollPane.getViewport().setBackground(EnhancedModernTheme.Colors.WHITE);

        historyContent.add(historyActionsPanel, BorderLayout.NORTH);
        historyContent.add(historyScrollPane, BorderLayout.CENTER);
        
        historyCard.add(historyContent, BorderLayout.CENTER);
        
        content.add(profileCard, BorderLayout.NORTH);
        content.add(historyCard, BorderLayout.CENTER);

        refreshHistoryPanel();
        
        return content;
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
    
    private JPanel createStatsCard(String icon, String title, JLabel valueLabel, Color accentColor) {
        ModernUIComponents.ModernCard card = new ModernUIComponents.ModernCard("");
        card.setLayout(new BorderLayout());
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 
            EnhancedModernTheme.Spacing.LARGE,
            EnhancedModernTheme.Spacing.LARGE, 
            EnhancedModernTheme.Spacing.LARGE
        ));
        
        // Icon
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(EnhancedModernTheme.Fonts.LABEL.deriveFont(Font.BOLD, 12f));
        iconLabel.setForeground(accentColor);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 26));
        iconLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        iconLabel.setPreferredSize(new Dimension(42, 42));
        
        // Title and value
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        titleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        
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

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(new EmptyBorder(
                    EnhancedModernTheme.Spacing.LARGE - 2,
                    EnhancedModernTheme.Spacing.LARGE,
                    EnhancedModernTheme.Spacing.LARGE + 2,
                    EnhancedModernTheme.Spacing.LARGE
                ));
                card.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(new EmptyBorder(
                    EnhancedModernTheme.Spacing.LARGE,
                    EnhancedModernTheme.Spacing.LARGE,
                    EnhancedModernTheme.Spacing.LARGE,
                    EnhancedModernTheme.Spacing.LARGE
                ));
                card.repaint();
            }
        });
        
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
        
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'><h2 style='margin-bottom:8px;'>" + title + "</h2></div></html>");
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

    private void installTableStyle() {
        JTableHeader header = articlesTable.getTableHeader();
        header.setFont(EnhancedModernTheme.Fonts.LABEL);
        header.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        header.setBackground(EnhancedModernTheme.Colors.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EnhancedModernTheme.Colors.BORDER_LIGHT));
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(new EmptyBorder(0, 16, 0, 16));
                label.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
                label.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
                if (isSelected) {
                    label.setBackground(new Color(79, 70, 229, 24));
                } else {
                    label.setBackground(row % 2 == 0 ? EnhancedModernTheme.Colors.WHITE : EnhancedModernTheme.Colors.GRAY_50);
                }
                return label;
            }
        };

        for (int i = 0; i < articlesTable.getColumnModel().getColumnCount(); i++) {
            articlesTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void installHistoryTableStyle() {
        JTableHeader header = historyTable.getTableHeader();
        header.setFont(EnhancedModernTheme.Fonts.LABEL);
        header.setForeground(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        header.setBackground(EnhancedModernTheme.Colors.WHITE);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(new EmptyBorder(0, 12, 0, 12));
                label.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
                label.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
                label.setBackground(isSelected
                    ? new Color(79, 70, 229, 24)
                    : (row % 2 == 0 ? EnhancedModernTheme.Colors.WHITE : EnhancedModernTheme.Colors.GRAY_50));
                return label;
            }
        };

        TableColumnModel columns = historyTable.getColumnModel();
        columns.getColumn(0).setPreferredWidth(160);
        columns.getColumn(1).setPreferredWidth(260);
        columns.getColumn(2).setPreferredWidth(140);
        columns.getColumn(3).setPreferredWidth(360);
        for (int i = 0; i < columns.getColumnCount(); i++) {
            columns.getColumn(i).setCellRenderer(renderer);
        }
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
        String country = extractCountryCode((String) countryCombo.getSelectedItem());
        if (country == null || country.isBlank()) {
            showError("Please select a country before fetching news.");
            return;
        }

        String category = normalizeCategory((String) categoryCombo.getSelectedItem());

        SwingWorker<NewsResponse, Void> worker = new SwingWorker<NewsResponse, Void>() {
            @Override
            protected NewsResponse doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    setFetchLoadingState(true, "Fetching latest news...");
                });

                return newsService.getTopHeadlines(country, category);
            }
            
            @Override
            protected void done() {
                try {
                    NewsResponse response = get();
                    processNewsResponse(response);
                } catch (Exception e) {
                    showError("Unable to fetch news at the moment. Please try again.");
                    statusLabel.setText("Unable to fetch news at the moment. Please try again.");
                } finally {
                    setFetchLoadingState(false, "Fetch complete");
                }
            }
        };
        
        worker.execute();
    }
    
    private void processNewsResponse(NewsResponse response) {
        if (response != null && response.isSuccess() && response.hasArticles()) {
            currentArticles = response.getArticles();
            updateArticlesTable();
            recordActivity(createFetchHistoryItem());
            updateDashboardStats();
            
            String message = String.format(
                "📰 Successfully fetched %d articles. Navigate to Analytics or Charts to analyze the data!",
                currentArticles.size()
            );
            statusLabel.setText(message);
            
            // Auto-analyze data
            analyzeData();
            
        } else {
            String message = response != null && response.getMessage() != null
                ? response.getMessage()
                : "Unable to fetch news at the moment. Please try again.";
            statusLabel.setText(message);
            showError("Unable to fetch news at the moment. Please try again.");
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
                    analysesRun++;
                    generateVisualizations();
                    updateDashboardStats();
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
        recordActivity(createKeywordHistoryItem(keywords));
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
                    recordActivity(createSummaryHistoryItem(url, summary.getTitle()));
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

    private void summarizeSelectedArticle() {
        if (currentArticles == null || currentArticles.isEmpty()) {
            showError("Fetch news first, then select an article from the table.");
            return;
        }

        int selectedRow = articlesTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentArticles.size()) {
            showError("Select an article from the News Fetch table first.");
            return;
        }

        NewsArticle article = currentArticles.get(selectedRow);
        String articleUrl = article.getUrl();
        if (articleUrl != null && !articleUrl.isBlank()) {
            urlField.setText(articleUrl);
            summarizeArticle();
            return;
        }

        displayArticleSummary(buildLocalSummary(article));
        recordActivity(createSummaryHistoryItem(article.getTitle(), article.getTitle()));
    }
    
    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
    
    private void displayArticleSummary(ArticleSummarizer.ArticleSummary summary) {
        summaryTitleLabel.setText(summary.getTitle());
        summaryMetaLabel.setText(String.format("Words: %d | Source: %s",
            summary.getWordCount(),
            summary.getUrl() == null || summary.getUrl().isBlank() ? "Local article data" : summary.getUrl()));
        summaryTextArea.setText(summary.getSummary());
        summaryTextArea.setCaretPosition(0);

        if (summary.getKeyPoints() != null && !summary.getKeyPoints().isEmpty()) {
            summaryKeywordsLabel.setText("Key points: " + String.join(" | ", summary.getKeyPoints()));
        } else {
            summaryKeywordsLabel.setText("No key points extracted.");
        }

        updateDashboardStats();
    }

    private ArticleSummarizer.ArticleSummary buildLocalSummary(NewsArticle article) {
        StringBuilder body = new StringBuilder();
        if (article.getDescription() != null && !article.getDescription().isBlank()) {
            body.append(article.getDescription()).append("\n\n");
        }
        if (article.getContent() != null && !article.getContent().isBlank()) {
            body.append(article.getContent());
        }
        if (body.length() == 0) {
            body.append("No article body was available from the feed. Title: ").append(article.getTitle());
        }

        List<String> points = new ArrayList<>();
        points.add(article.getSource() != null ? "Source: " + article.getSource().getName() : "Source unavailable");
        if (article.getPublishedAt() != null) {
            points.add("Published: " + article.getPublishedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        points.add("Generated from fetched article metadata");

        return new ArticleSummarizer.ArticleSummary(
            article.getTitle() != null ? article.getTitle() : "Selected Article",
            body.toString(),
            points,
            Map.of(),
            body.toString().split("\\s+").length,
            article.getUrl() != null ? article.getUrl() : ""
        );
    }

    private void installTextFieldEditing(JTextField field) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem cut = new JMenuItem("Cut");
        JMenuItem copy = new JMenuItem("Copy");
        JMenuItem paste = new JMenuItem("Paste");
        cut.addActionListener(e -> field.cut());
        copy.addActionListener(e -> field.copy());
        paste.addActionListener(e -> field.paste());
        menu.add(cut);
        menu.add(copy);
        menu.add(paste);
        field.setComponentPopupMenu(menu);
        field.setDragEnabled(true);
    }

    private void updateDashboardStats() {
        int articleCount = currentArticles == null ? 0 : currentArticles.size();
        totalArticlesValueLabel.setText(String.valueOf(articleCount));
        analysesDoneValueLabel.setText(String.valueOf(analysesRun));
        searchHistoryValueLabel.setText(String.valueOf(recentActivity.size()));

        double avgSentiment = 0.0;
        if (currentArticles != null && !currentArticles.isEmpty()) {
            double totalSentiment = 0.0;
            for (NewsArticle article : currentArticles) {
                totalSentiment += article.getSentimentScore();
            }
            avgSentiment = totalSentiment / currentArticles.size();
        }
        sentimentScoreValueLabel.setText(String.format("%.2f", avgSentiment));
    }

    private SearchHistory createFetchHistoryItem() {
        SearchHistory item = new SearchHistory();
        item.setUserId(LOCAL_HISTORY_USER_ID);
        item.setSearchType("headlines");
        item.setCountry(extractCountryCode((String) countryCombo.getSelectedItem()));
        item.setCategory(normalizeCategory((String) categoryCombo.getSelectedItem()));
        item.setArticlesFound(currentArticles == null ? 0 : currentArticles.size());
        item.setSearchQuery(String.format(
            "Headlines for %s / %s",
            nonBlank((String) countryCombo.getSelectedItem(), "Unknown"),
            nonBlank((String) categoryCombo.getSelectedItem(), "general")
        ));
        item.setAnalysisResults(item.getArticlesFound() + " articles fetched");
        item.setSearchedAt(LocalDateTime.now());
        return item;
    }

    private SearchHistory createSummaryHistoryItem(String query, String resultTitle) {
        SearchHistory item = new SearchHistory();
        item.setUserId(LOCAL_HISTORY_USER_ID);
        item.setSearchType("url_summary");
        item.setSearchQuery(query);
        item.setAnalysisResults(resultTitle);
        item.setArticlesFound(1);
        item.setSearchedAt(LocalDateTime.now());
        return item;
    }

    private SearchHistory createTranslationHistoryItem(String actionType, String input, String resultSummary) {
        SearchHistory item = new SearchHistory();
        item.setUserId(LOCAL_HISTORY_USER_ID);
        item.setSearchType("translation");
        item.setSearchQuery(input);
        item.setAnalysisResults(actionType + ": " + resultSummary);
        item.setArticlesFound(1);
        item.setSearchedAt(LocalDateTime.now());
        return item;
    }

    private SearchHistory createKeywordHistoryItem(Map<String, Integer> keywords) {
        SearchHistory item = new SearchHistory();
        item.setUserId(LOCAL_HISTORY_USER_ID);
        item.setSearchType("keywords");
        item.setSearchQuery("Keyword analysis");
        item.setArticlesFound(currentArticles == null ? 0 : currentArticles.size());
        item.setAnalysisResults("Top terms: " + String.join(", ", keywords.keySet().stream().limit(5).toList()));
        item.setSearchedAt(LocalDateTime.now());
        return item;
    }

    private void recordActivity(SearchHistory item) {
        if (item == null) {
            return;
        }
        recentActivity.add(0, item);
        while (recentActivity.size() > 10) {
            recentActivity.remove(recentActivity.size() - 1);
        }
        saveHistoryToDisk();
        refreshHistoryPanel();
        updateDashboardStats();
    }

    private void refreshHistoryPanel() {
        if (historyTableModel == null) {
            return;
        }
        historyTableModel.setRowCount(0);

        for (SearchHistory item : recentActivity) {
            historyTableModel.addRow(new Object[]{
                item.getDisplayTitle(),
                nonBlank(item.getSearchQuery(), "-"),
                item.getSearchedAt() != null ? item.getSearchedAt().format(HISTORY_TIMESTAMP) : "-",
                nonBlank(item.getAnalysisResults(), item.getSearchDescription())
            });
        }
        if (clearHistoryButton != null) {
            clearHistoryButton.setEnabled(!recentActivity.isEmpty());
        }
    }

    private String extractCountryCode(String countrySelection) {
        if (countrySelection == null) {
            return null;
        }
        return switch (countrySelection) {
            case "United States" -> "us";
            case "India" -> "in";
            case "United Kingdom" -> "gb";
            case "Japan" -> "jp";
            case "Australia" -> "au";
            case "Canada" -> "ca";
            case "Germany" -> "de";
            default -> null;
        };
    }

    private String normalizeCategory(String categorySelection) {
        if (categorySelection == null || categorySelection.isBlank()) {
            return "general";
        }
        return categorySelection.trim().toLowerCase();
    }

    private void setFetchLoadingState(boolean loading, String progressText) {
        fetchButton.setEnabled(!loading);
        countryCombo.setEnabled(!loading);
        categoryCombo.setEnabled(!loading);
        progressBar.setVisible(loading);
        progressBar.setIndeterminate(loading);
        fetchProgressLabel.setText(progressText);
        fetchProgressLabel.setForeground(loading ? EnhancedModernTheme.Colors.PRIMARY : EnhancedModernTheme.Colors.TEXT_SECONDARY);
        if (loading) {
            statusLabel.setText("Fetching latest news...");
        }
    }

    private void clearHistory() {
        recentActivity.clear();
        saveHistoryToDisk();
        refreshHistoryPanel();
        updateDashboardStats();
    }

    private void saveHistoryToDisk() {
        try {
            Files.createDirectories(HISTORY_FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(HISTORY_FILE.toFile(), recentActivity);
        } catch (Exception e) {
            statusLabel.setText("Unable to save local history.");
        }
    }

    private void loadHistoryFromDisk() {
        try {
            if (!Files.exists(HISTORY_FILE)) {
                return;
            }
            List<SearchHistory> stored = objectMapper.readValue(HISTORY_FILE.toFile(), new TypeReference<List<SearchHistory>>() {});
            recentActivity.clear();
            recentActivity.addAll(stored);
        } catch (Exception e) {
            recentActivity.clear();
        }
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
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
