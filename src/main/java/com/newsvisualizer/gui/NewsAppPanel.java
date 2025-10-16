package com.newsvisualizer.gui;

import com.newsvisualizer.model.NewsArticle;
import com.newsvisualizer.service.NewsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;

/**
 * NewsApp-style GUI panel that can be integrated into NewsVisualizer
 * This recreates the GUI functionality from the legacy newsApp
 */
public class NewsAppPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(NewsAppPanel.class);
    
    private final NewsAppService newsAppService;
    private JList<NewsArticle> newsList;
    private DefaultListModel<NewsArticle> listModel;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JButton refreshButton;
    private JButton searchButton;
    private JLabel statusLabel;
    
    public NewsAppPanel() {
        this.newsAppService = new NewsAppService();
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadNewsByCategory();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("NewsApp Integration"));
        
        // Search panel components
        searchField = new JTextField(20);
        searchField.setToolTipText("Enter search keywords...");
        
        // Category selection
        String[] categories = {
            "All Categories", "General", "Business", "Technology", 
            "Sports", "Health", "Entertainment", "Science"
        };
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setToolTipText("Select news category");
        
        searchButton = new JButton("Search");
        searchButton.setToolTipText("Search news articles");
        
        refreshButton = new JButton("Refresh");
        refreshButton.setToolTipText("Refresh news feed");
        
        // News list
        listModel = new DefaultListModel<>();
        newsList = new JList<>(listModel);
        newsList.setCellRenderer(new NewsCardRenderer());
        newsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Status label
        statusLabel = new JLabel("Loading news...");
        statusLabel.setForeground(Color.GRAY);
    }
    
    private void layoutComponents() {
        // Top panel with search controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Category:"));
        topPanel.add(categoryCombo);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(refreshButton);
        
        // Enhanced center panel with improved news list layout
        JScrollPane scrollPane = new JScrollPane(newsList);
        scrollPane.setPreferredSize(new Dimension(900, 600)); // Increased size for better visibility
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1)
        )); // Modern border instead of bevel
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        
        // Bottom panel with status
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(statusLabel);
        
        // Add all panels
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Category selection action
        categoryCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadNewsByCategory();
            }
        });
        
        // Search button action
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Search field enter key
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Refresh button action
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadNewsByCategory();
            }
        });
        
        // Enhanced click handling - single click to select, double-click to open, right-click for summary
        newsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NewsArticle selected = newsList.getSelectedValue();
                if (selected == null) return;
                
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    // Double-click to open article in browser
                    if (selected.getUrl() != null && !selected.getUrl().isEmpty()) {
                        openArticleInBrowser(selected.getUrl());
                    }
                } else if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
                    // Right-click to show article summary popup
                    showArticleSummaryDialog(selected);
                }
            }
        });
    }
    
    private void performSearch() {
        String keyword = searchField.getText().trim();
        
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Searching for: " + keyword);
            statusLabel.setForeground(Color.BLUE);
        });
        
        // Perform search in background thread
        SwingWorker<List<NewsArticle>, Void> worker = new SwingWorker<List<NewsArticle>, Void>() {
            @Override
            protected List<NewsArticle> doInBackground() throws Exception {
                return newsAppService.searchNews(keyword);
            }
            
            @Override
            protected void done() {
                try {
                    List<NewsArticle> articles = get();
                    updateNewsList(articles);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Found " + articles.size() + " articles matching: " + keyword);
                        statusLabel.setForeground(Color.BLACK);
                    });
                } catch (Exception e) {
                    logger.error("Error performing search", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Search failed: " + e.getMessage());
                        statusLabel.setForeground(Color.RED);
                    });
                }
            }
        };
        worker.execute();
    }
    
    private void loadNewsByCategory() {
        String selectedCategory = (String) categoryCombo.getSelectedItem();
        String category = "general"; // default
        
        if (!"All Categories".equals(selectedCategory) && !"General".equals(selectedCategory)) {
            category = selectedCategory.toLowerCase();
        }
        
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Loading " + selectedCategory + " news...");
            statusLabel.setForeground(Color.BLUE);
            refreshButton.setEnabled(false);
            categoryCombo.setEnabled(false);
        });
        
        final String finalCategory = category;
        
        // Load news in background thread
        SwingWorker<List<NewsArticle>, Void> worker = new SwingWorker<List<NewsArticle>, Void>() {
            @Override
            protected List<NewsArticle> doInBackground() throws Exception {
                return newsAppService.fetchNewsAppStyleByCategory(finalCategory);
            }
            
            @Override
            protected void done() {
                try {
                    List<NewsArticle> articles = get();
                    updateNewsList(articles);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Loaded " + articles.size() + " " + selectedCategory + " articles");
                        statusLabel.setForeground(Color.BLACK);
                        refreshButton.setEnabled(true);
                        categoryCombo.setEnabled(true);
                    });
                } catch (Exception e) {
                    logger.error("Error loading news", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Failed to load news: " + e.getMessage());
                        statusLabel.setForeground(Color.RED);
                        refreshButton.setEnabled(true);
                        categoryCombo.setEnabled(true);
                    });
                }
            }
        };
        worker.execute();
    }
    
    private void updateNewsList(List<NewsArticle> articles) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (NewsArticle article : articles) {
                listModel.addElement(article);
            }
        });
    }
    
    private void openArticleInBrowser(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(url));
            } else {
                // Fallback: show URL in dialog
                JOptionPane.showMessageDialog(this, 
                    "Please open this URL in your browser:\\n" + url,
                    "Open Article", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            logger.error("Error opening URL in browser: " + url, e);
            JOptionPane.showMessageDialog(this, 
                "Could not open article in browser.\\nURL: " + url,
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showArticleSummaryDialog(NewsArticle article) {
        JDialog summaryDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Article Summary", true);
        summaryDialog.setSize(800, 600);
        summaryDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("<html><h2>" + article.getTitle() + "</h2></html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Summary content
        JTextArea summaryArea = new JTextArea(article.getSummary() != null ? article.getSummary() : "Summary not available.");
        summaryArea.setWrapStyleWord(true);
        summaryArea.setLineWrap(true);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Georgia", Font.PLAIN, 14));
        summaryArea.setBackground(new Color(248, 249, 250));
        summaryArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton openButton = new JButton("Open in Browser");
        openButton.addActionListener(e -> {
            if (article.getUrl() != null && !article.getUrl().isEmpty()) {
                openArticleInBrowser(article.getUrl());
            }
            summaryDialog.dispose();
        });
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> summaryDialog.dispose());
        
        buttonPanel.add(openButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        summaryDialog.add(mainPanel);
        summaryDialog.setVisible(true);
    }
    
    /**
     * Enhanced custom cell renderer for news articles with modern card design
     */
    private static class NewsCardRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                    int index, boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof NewsArticle) {
                NewsArticle article = (NewsArticle) value;
                
                // Create modern card panel with enhanced styling
                JPanel panel = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        // Modern card background with subtle shadow
                        if (isSelected) {
                            g2d.setColor(new Color(59, 130, 246, 20));
                        } else {
                            g2d.setColor(Color.WHITE);
                        }
                        g2d.fillRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 12, 12);
                        
                        // Subtle border
                        g2d.setColor(new Color(229, 231, 235));
                        g2d.setStroke(new BasicStroke(1.0f));
                        g2d.drawRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 12, 12);
                        
                        // Selection highlight
                        if (isSelected) {
                            g2d.setColor(new Color(59, 130, 246, 100));
                            g2d.fillRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 12, 12);
                        }
                    }
                };
                panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20)); // Increased padding
                panel.setOpaque(false);
                
                // Title with improved typography
                JLabel titleLabel = new JLabel("<html><b>" + article.getTitle() + "</b></html>");
                titleLabel.setFont(new Font("SF Pro Text", Font.BOLD, 15)); // Improved font
                titleLabel.setForeground(new Color(30, 41, 59));
                
                // Source with better styling
                String sourceText = article.getSource() != null ? article.getSource().getName() : "Unknown Source";
                JLabel sourceLabel = new JLabel("📰 " + sourceText);
                sourceLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 12));
                sourceLabel.setForeground(new Color(100, 116, 139));
                
                // Enhanced summary with better text handling
                String summaryText = article.getSummary() != null ? article.getSummary() : "No summary available.";
                if (summaryText.length() > 200) {
                    summaryText = summaryText.substring(0, 200) + "..."; // Truncate long summaries
                }
                JLabel summaryLabel = new JLabel("<html><div style='margin-top: 8px; line-height: 1.4;'>" + summaryText + "</div></html>");
                summaryLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
                summaryLabel.setForeground(new Color(75, 85, 99));
                
                // Layout with better spacing
                JPanel contentPanel = new JPanel();
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                contentPanel.setOpaque(false);
                contentPanel.add(titleLabel);
                contentPanel.add(Box.createVerticalStrut(8)); // Spacing
                contentPanel.add(summaryLabel);
                contentPanel.add(Box.createVerticalStrut(10)); // Spacing
                contentPanel.add(sourceLabel);
                
                panel.add(contentPanel, BorderLayout.CENTER);
                
                // Add subtle hover indicator
                JLabel hintLabel = new JLabel("<html><small style='color: #9CA3AF;'>Double-click to open • Right-click for full summary</small></html>");
                hintLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 10));
                hintLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                panel.add(hintLabel, BorderLayout.SOUTH);
                
                return panel;
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}