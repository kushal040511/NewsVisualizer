package com.newsvisualizer.gui;

import com.newsvisualizer.gui.components.KeepTooSidebar;
import com.newsvisualizer.gui.components.KeepTooContentArea;
import com.newsvisualizer.gui.theme.KeepTooTheme;
import com.newsvisualizer.service.SessionManager;
import com.newsvisualizer.model.User;

// Import existing GUI components
import com.newsvisualizer.gui.TranslationPanel;
import com.newsvisualizer.gui.NewsAppPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * KeepToo-styled main window for NewsVisualizer
 */
public class KeepTooMainWindow extends JFrame {
    
    private KeepTooSidebar sidebar;
    private KeepTooContentArea contentArea;
    private Map<KeepTooSidebar.NavigationItem, JPanel> contentPanels;
    private SessionManager sessionManager;
    
    // Content panels
    private TranslationPanel translationPanel;
    private NewsAppPanel newsAppPanel;
    private JPanel dashboardPanel;
    
    public KeepTooMainWindow() {
        this.sessionManager = SessionManager.getInstance();
        initializeWindow();
        createUI();
        setupNavigation();
        showDashboard();
        
        // Set window to maximized state
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }
    
    private void initializeWindow() {
        setTitle("NewsVisualizer - Professional News Analytics");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 800));
        
        // Set the application icon
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }
    
    private void createUI() {
        // Set up the main layout
        setLayout(new BorderLayout());
        getContentPane().setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        
        // Create sidebar
        sidebar = new KeepTooSidebar();
        
        // Create content area
        contentArea = new KeepTooContentArea();
        
        // Initialize content panels
        initializeContentPanels();
        
        // Add components to main window
        add(sidebar, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);
    }
    
    private void initializeContentPanels() {
        contentPanels = new HashMap<>();
        
        // Create dashboard panel
        dashboardPanel = createDashboardPanel();
        contentPanels.put(KeepTooSidebar.NavigationItem.DASHBOARD, dashboardPanel);
        
        // Initialize existing panels with error handling
        
        // Add placeholder panels for features that don't have dedicated panels yet
        contentPanels.put(KeepTooSidebar.NavigationItem.NEWS_FETCH, createFeaturePanel("News Fetch", "📰", "Fetch and display the latest news articles from various sources."));
        contentPanels.put(KeepTooSidebar.NavigationItem.ANALYTICS, createFeaturePanel("Analytics", "📊", "Analyze news trends, sentiment, and key insights."));
        contentPanels.put(KeepTooSidebar.NavigationItem.CHARTS, createFeaturePanel("Sentiment Charts", "📈", "Visual representation of sentiment analysis across articles."));
        contentPanels.put(KeepTooSidebar.NavigationItem.KEYWORDS, createFeaturePanel("Keywords", "🔑", "Extract and analyze key terms from news content."));
        contentPanels.put(KeepTooSidebar.NavigationItem.AI_SUMMARY, createFeaturePanel("AI Summary", "🤖", "Generate AI-powered summaries of news articles."));
        
        // Add existing panels
        try {
            translationPanel = new TranslationPanel();
            stylePanel(translationPanel);
            contentPanels.put(KeepTooSidebar.NavigationItem.TRANSLATION, translationPanel);
        } catch (Exception e) {
            contentPanels.put(KeepTooSidebar.NavigationItem.TRANSLATION, createErrorPanel("Translation"));
        }
        
        try {
            newsAppPanel = new NewsAppPanel();
            stylePanel(newsAppPanel);
            contentPanels.put(KeepTooSidebar.NavigationItem.NEWS_APP, newsAppPanel);
        } catch (Exception e) {
            contentPanels.put(KeepTooSidebar.NavigationItem.NEWS_APP, createErrorPanel("NewsApp"));
        }
        
        // Add placeholders for remaining features
        contentPanels.put(KeepTooSidebar.NavigationItem.HISTORY, createFeaturePanel("Search History", "📜", "View your search history and saved articles."));
        contentPanels.put(KeepTooSidebar.NavigationItem.SETTINGS, createFeaturePanel("Settings", "⚙️", "Configure application preferences and user settings."));
        
        // Add all panels to content area
        for (Map.Entry<KeepTooSidebar.NavigationItem, JPanel> entry : contentPanels.entrySet()) {
            contentArea.addContentPanel(entry.getKey().name(), entry.getValue());
        }
    }
    
    private JPanel createErrorPanel(String sectionName) {
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        
        JPanel card = KeepTooContentArea.createCard();
        card.setLayout(new BorderLayout());
        
        JLabel errorLabel = new JLabel("<html><div style='text-align: center;'>" +
            "<h2>🚧 " + sectionName + " Panel Loading...</h2>" +
            "<p>This panel will be available once all dependencies are loaded.</p>" +
            "</div></html>");
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setFont(KeepTooTheme.Fonts.BODY);
        errorLabel.setForeground(KeepTooTheme.Colors.TEXT_SECONDARY);
        
        card.add(errorLabel, BorderLayout.CENTER);
        errorPanel.add(card, BorderLayout.CENTER);
        
        return errorPanel;
    }
    
    private JPanel createFeaturePanel(String featureName, String icon, String description) {
        JPanel featurePanel = new JPanel(new BorderLayout());
        featurePanel.setBackground(Color.WHITE);
        
        JPanel card = KeepTooContentArea.createCard();
        card.setLayout(new BorderLayout());
        
        JPanel header = KeepTooContentArea.createSectionHeader(featureName, "Coming Soon");
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Description
        JLabel descLabel = new JLabel("<html><div style='text-align: center; line-height: 1.5;'>" + description + "</div></html>");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLabel.setForeground(new Color(102, 102, 102));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setBorder(new EmptyBorder(16, 0, 0, 0));
        
        // Status
        JLabel statusLabel = new JLabel("This feature is under development");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(153, 153, 153));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        
        content.add(iconLabel);
        content.add(descLabel);
        content.add(statusLabel);
        
        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        featurePanel.add(card, BorderLayout.CENTER);
        
        return featurePanel;
    }
    
    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        
        // Dashboard welcome section
        JPanel welcomeCard = KeepTooContentArea.createCard();
        welcomeCard.setLayout(new BorderLayout());
        
        JPanel welcomeHeader = KeepTooContentArea.createSectionHeader(
            "Welcome to NewsVisualizer", 
            "Your comprehensive news analysis platform"
        );
        
        JLabel welcomeText = new JLabel(
            "<html><div style='line-height: 1.6; color: #666666;'>" +
            "Get started by exploring the various features available in the sidebar. " +
            "Fetch the latest news, analyze sentiment, generate AI summaries, " +
            "and much more. Your journey to better news understanding begins here." +
            "</div></html>"
        );
        welcomeText.setFont(KeepTooTheme.Fonts.BODY);
        welcomeText.setBorder(new EmptyBorder(KeepTooTheme.Spacing.LARGE, 0, 0, 0));
        
        welcomeCard.add(welcomeHeader, BorderLayout.NORTH);
        welcomeCard.add(welcomeText, BorderLayout.CENTER);
        
        // Quick actions section
        JPanel quickActionsCard = KeepTooContentArea.createCard();
        quickActionsCard.setLayout(new BorderLayout());
        
        JPanel quickActionsHeader = KeepTooContentArea.createSectionHeader(
            "Quick Actions", 
            "Jump right into your favorite features"
        );
        
        JPanel actionsPanel = new JPanel(new GridLayout(2, 3, KeepTooTheme.Spacing.LARGE, KeepTooTheme.Spacing.LARGE));
        actionsPanel.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        actionsPanel.setBorder(new EmptyBorder(KeepTooTheme.Spacing.LARGE, 0, 0, 0));
        
        // Create quick action buttons
        JButton fetchNewsBtn = createQuickActionButton("📰 Fetch News", "Get the latest news articles");
        JButton analyticsBtn = createQuickActionButton("📊 Analytics", "View detailed analytics");
        JButton chartsBtn = createQuickActionButton("📈 Charts", "See sentiment charts");
        JButton aiSummaryBtn = createQuickActionButton("🤖 AI Summary", "Generate AI summaries");
        JButton translationBtn = createQuickActionButton("🌐 Translation", "Translate content");
        JButton historyBtn = createQuickActionButton("📜 History", "View search history");
        
        // Add action listeners
        fetchNewsBtn.addActionListener(e -> navigateToSection(KeepTooSidebar.NavigationItem.NEWS_FETCH));
        analyticsBtn.addActionListener(e -> navigateToSection(KeepTooSidebar.NavigationItem.ANALYTICS));
        chartsBtn.addActionListener(e -> navigateToSection(KeepTooSidebar.NavigationItem.CHARTS));
        aiSummaryBtn.addActionListener(e -> navigateToSection(KeepTooSidebar.NavigationItem.AI_SUMMARY));
        translationBtn.addActionListener(e -> navigateToSection(KeepTooSidebar.NavigationItem.TRANSLATION));
        historyBtn.addActionListener(e -> navigateToSection(KeepTooSidebar.NavigationItem.HISTORY));
        
        actionsPanel.add(fetchNewsBtn);
        actionsPanel.add(analyticsBtn);
        actionsPanel.add(chartsBtn);
        actionsPanel.add(aiSummaryBtn);
        actionsPanel.add(translationBtn);
        actionsPanel.add(historyBtn);
        
        quickActionsCard.add(quickActionsHeader, BorderLayout.NORTH);
        quickActionsCard.add(actionsPanel, BorderLayout.CENTER);
        
        // Main dashboard layout
        JPanel dashboardContent = new JPanel();
        dashboardContent.setLayout(new BoxLayout(dashboardContent, BoxLayout.Y_AXIS));
        dashboardContent.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        dashboardContent.setBorder(new EmptyBorder(
            KeepTooTheme.Spacing.LARGE,
            KeepTooTheme.Spacing.LARGE,
            KeepTooTheme.Spacing.LARGE,
            KeepTooTheme.Spacing.LARGE
        ));
        
        dashboardContent.add(welcomeCard);
        dashboardContent.add(Box.createVerticalStrut(KeepTooTheme.Spacing.XLARGE));
        dashboardContent.add(quickActionsCard);
        dashboardContent.add(Box.createVerticalGlue());
        
        JScrollPane scrollPane = new JScrollPane(dashboardContent);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        dashboard.add(scrollPane, BorderLayout.CENTER);
        return dashboard;
    }
    
    private JButton createQuickActionButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(KeepTooTheme.Fonts.BUTTON);
        button.setToolTipText(tooltip);
        button.setBackground(KeepTooTheme.Colors.BACKGROUND_LIGHT);
        button.setForeground(KeepTooTheme.Colors.TEXT_PRIMARY);
        button.setBorder(new EmptyBorder(
            KeepTooTheme.Spacing.LARGE,
            KeepTooTheme.Spacing.LARGE,
            KeepTooTheme.Spacing.LARGE,
            KeepTooTheme.Spacing.LARGE
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        
        // Custom painting for rounded corners
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JButton button = (JButton) c;
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded background
                g2d.setColor(button.getBackground());
                g2d.fillRoundRect(0, 0, button.getWidth(), button.getHeight(),
                                KeepTooTheme.Radius.LARGE, KeepTooTheme.Radius.LARGE);
                
                // Draw border
                g2d.setColor(KeepTooTheme.Colors.BORDER_LIGHT);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, button.getWidth() - 1, button.getHeight() - 1,
                                KeepTooTheme.Radius.LARGE, KeepTooTheme.Radius.LARGE);
                
                g2d.dispose();
                super.paint(g, c);
            }
        });
        
        // Hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(KeepTooTheme.Colors.PURPLE_HOVER);
                button.setForeground(KeepTooTheme.Colors.TEXT_WHITE);
                button.repaint();
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(KeepTooTheme.Colors.BACKGROUND_LIGHT);
                button.setForeground(KeepTooTheme.Colors.TEXT_PRIMARY);
                button.repaint();
            }
        });
        
        return button;
    }
    
    private void stylePanel(JPanel panel) {
        // Apply KeepToo styling to existing panels
        if (panel != null) {
            panel.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
            
            // Apply styling recursively to child components
            styleComponentsRecursively(panel);
        }
    }
    
    private void styleComponentsRecursively(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                if (panel.getBackground().equals(Color.WHITE) || 
                    panel.getBackground().equals(new Color(240, 240, 240))) {
                    panel.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
                }
                styleComponentsRecursively(panel);
            } else if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                if (label.getFont().getStyle() == Font.BOLD && label.getFont().getSize() >= 16) {
                    label.setFont(KeepTooTheme.Fonts.SECTION_TITLE);
                    label.setForeground(KeepTooTheme.Colors.TEXT_PRIMARY);
                } else {
                    label.setFont(KeepTooTheme.Fonts.BODY);
                    label.setForeground(KeepTooTheme.Colors.TEXT_SECONDARY);
                }
            } else if (component instanceof Container) {
                styleComponentsRecursively((Container) component);
            }
        }
    }
    
    private void setupNavigation() {
        sidebar.setNavigationListener(item -> {
            navigateToSection(item);
        });
    }
    
    private void navigateToSection(KeepTooSidebar.NavigationItem item) {
        // Update content area
        contentArea.showContentPanel(item.name());
        
        // Update page title and subtitle based on section
        switch (item) {
            case DASHBOARD:
                contentArea.setPageInfo("Dashboard", "Welcome to NewsVisualizer");
                break;
            case NEWS_FETCH:
                contentArea.setPageInfo("News Fetch", "Get the latest news articles");
                break;
            case ANALYTICS:
                contentArea.setPageInfo("Analytics", "Detailed news analytics and insights");
                break;
            case CHARTS:
                contentArea.setPageInfo("Sentiment Charts", "Visual sentiment analysis");
                break;
            case KEYWORDS:
                contentArea.setPageInfo("Keywords", "Keyword extraction and analysis");
                break;
            case AI_SUMMARY:
                contentArea.setPageInfo("AI Summary", "AI-powered content summarization");
                break;
            case TRANSLATION:
                contentArea.setPageInfo("Translation", "Multi-language content translation");
                break;
            case NEWS_APP:
                contentArea.setPageInfo("NewsApp", "Integrated news application");
                break;
            case HISTORY:
                contentArea.setPageInfo("Search History", "Your search and activity history");
                break;
            case SETTINGS:
                contentArea.setPageInfo("Settings", "Configure application preferences");
                break;
        }
        
        // Note: Don't call selectNavigationItem here to avoid circular calls
        // The sidebar selection is already updated when user clicks
        
        // Clear and setup toolbar for specific sections
        setupSectionToolbar(item);
    }
    
    private void setupSectionToolbar(KeepTooSidebar.NavigationItem item) {
        contentArea.clearToolbar();
        
        switch (item) {
            case NEWS_FETCH:
                contentArea.addPrimaryToolbarButton("Fetch News", KeepTooTheme.Icons.NEWS_FETCH, "Fetch latest news");
                contentArea.addToolbarButton("Clear", "🗑️", "Clear results");
                break;
            case ANALYTICS:
                contentArea.addToolbarButton("Export", "📊", "Export analytics");
                contentArea.addToolbarButton("Refresh", "🔄", "Refresh data");
                break;
            case CHARTS:
                contentArea.addToolbarButton("Export Chart", "📈", "Export chart");
                contentArea.addToolbarButton("Settings", "⚙️", "Chart settings");
                break;
            case AI_SUMMARY:
                contentArea.addPrimaryToolbarButton("Generate", "🤖", "Generate summary");
                contentArea.addToolbarButton("Save", "💾", "Save summary");
                break;
            case TRANSLATION:
                contentArea.addPrimaryToolbarButton("Translate", "🌐", "Start translation");
                break;
            case SETTINGS:
                contentArea.addPrimaryToolbarButton("Save Settings", "💾", "Save all settings");
                contentArea.addToolbarButton("Reset", "🔄", "Reset to defaults");
                break;
        }
    }
    
    private void showDashboard() {
        navigateToSection(KeepTooSidebar.NavigationItem.DASHBOARD);
    }
    
    private void handleWindowClosing() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit NewsVisualizer?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // Clean up and exit
            System.exit(0);
        }
    }
    
    // Public methods for external access
    public TranslationPanel getTranslationPanel() { return translationPanel; }
    public NewsAppPanel getNewsAppPanel() { return newsAppPanel; }
}