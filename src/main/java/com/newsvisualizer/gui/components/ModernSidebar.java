package com.newsvisualizer.gui.components;

import com.newsvisualizer.gui.theme.EnhancedModernTheme;
import com.newsvisualizer.model.User;
import com.newsvisualizer.service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Modern sidebar navigation component styled like KeepToo
 */
public class ModernSidebar extends JPanel {
    
    public enum NavigationItem {
        DASHBOARD("Dashboard", EnhancedModernTheme.Icons.DASHBOARD),
        NEWS_FETCH("News Fetch", EnhancedModernTheme.Icons.NEWS),
        ANALYTICS("Analytics", EnhancedModernTheme.Icons.ANALYTICS),
        CHARTS("Sentiment Charts", EnhancedModernTheme.Icons.CHARTS),
        KEYWORDS("Keywords", EnhancedModernTheme.Icons.FILTER),
        AI_SUMMARY("AI Summary", EnhancedModernTheme.Icons.AI),
        TRANSLATION("Translation", EnhancedModernTheme.Icons.TRANSLATE),
        NEWSAPP("NewsApp", EnhancedModernTheme.Icons.NEWSAPP),
        HISTORY("Search History", EnhancedModernTheme.Icons.HISTORY),
        SETTINGS("Settings", EnhancedModernTheme.Icons.SETTINGS);
        
        private final String displayName;
        private final String icon;
        
        NavigationItem(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }
    
    public interface NavigationListener {
        void onNavigationItemSelected(NavigationItem item);
    }
    
    private NavigationListener navigationListener;
    private NavigationItem selectedItem = NavigationItem.DASHBOARD;
    private Map<NavigationItem, JPanel> navigationItems = new HashMap<>();
    private JPanel itemsContainer;
    
    public ModernSidebar() {
        initializeSidebar();
        createSidebarHeader();
        createNavigationItems();
        createUserSection();
    }
    
    private void initializeSidebar() {
        setLayout(new BorderLayout());
        
        // Get responsive sidebar width
        int sidebarWidth = EnhancedModernTheme.Layout.getSidebarWidth();
        
        setPreferredSize(new Dimension(sidebarWidth, 800));
        setMinimumSize(new Dimension(sidebarWidth, 600));
        setMaximumSize(new Dimension(sidebarWidth, Integer.MAX_VALUE));
        
        setBackground(EnhancedModernTheme.Colors.SIDEBAR_PRIMARY);
        setOpaque(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Sidebar gradient background
        GradientPaint gradient = EnhancedModernTheme.Gradients.createSidebarGradient(height);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Right shadow
        EnhancedModernTheme.Shadows.applySidebarShadow(g2d, width - 5, height);
        
        g2d.dispose();
    }
    
    private void createSidebarHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.LARGE, 
            EnhancedModernTheme.Spacing.LARGE, 
            EnhancedModernTheme.Spacing.LARGE, 
            EnhancedModernTheme.Spacing.LARGE
        ));
        
        // App title
        JLabel titleLabel = new JLabel("NewsVisualizer");
        titleLabel.setFont(EnhancedModernTheme.Fonts.APP_TITLE);
        titleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_WHITE);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Professional Analytics");
        subtitleLabel.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        subtitleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_SIDEBAR);
        
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(4));
        titleContainer.add(subtitleLabel);
        
        headerPanel.add(titleContainer, BorderLayout.CENTER);
        
        // Add separator line
        JPanel separator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(EnhancedModernTheme.Colors.BORDER_SIDEBAR);
                g2d.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        separator.setOpaque(false);
        separator.setPreferredSize(new Dimension(0, 1));
        
        JPanel headerWithSeparator = new JPanel(new BorderLayout());
        headerWithSeparator.setOpaque(false);
        headerWithSeparator.add(headerPanel, BorderLayout.CENTER);
        headerWithSeparator.add(separator, BorderLayout.SOUTH);
        
        add(headerWithSeparator, BorderLayout.NORTH);
    }
    
    private void createNavigationItems() {
        itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setOpaque(false);
        itemsContainer.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.MEDIUM, 0, 
            EnhancedModernTheme.Spacing.MEDIUM, 0
        ));
        
        // Create navigation items
        for (NavigationItem item : NavigationItem.values()) {
            if (item != NavigationItem.SETTINGS) { // Settings will be in user section
                JPanel itemPanel = createNavigationItem(item);
                navigationItems.put(item, itemPanel);
                itemsContainer.add(itemPanel);
                itemsContainer.add(Box.createVerticalStrut(2));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createNavigationItem(NavigationItem item) {
        JPanel itemPanel = new JPanel(new BorderLayout()) {
            private boolean isHovered = false;
            private boolean isSelected = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (item == selectedItem) {
                    // Selected state
                    g2d.setColor(EnhancedModernTheme.Colors.SIDEBAR_SELECTED);
                    g2d.fillRoundRect(8, 0, getWidth() - 16, getHeight(), 
                                    EnhancedModernTheme.Radius.MEDIUM, 
                                    EnhancedModernTheme.Radius.MEDIUM);
                } else if (isHovered) {
                    // Hover state
                    g2d.setColor(EnhancedModernTheme.Colors.SIDEBAR_HOVER);
                    g2d.fillRoundRect(8, 0, getWidth() - 16, getHeight(), 
                                    EnhancedModernTheme.Radius.MEDIUM, 
                                    EnhancedModernTheme.Radius.MEDIUM);
                }
                
                g2d.dispose();
            }
        };
        
        itemPanel.setOpaque(false);
        itemPanel.setPreferredSize(new Dimension(0, EnhancedModernTheme.Layout.SIDEBAR_ITEM_HEIGHT));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, EnhancedModernTheme.Layout.SIDEBAR_ITEM_HEIGHT));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Icon and text
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, EnhancedModernTheme.Spacing.LARGE, 0, EnhancedModernTheme.Spacing.LARGE));
        
        JLabel iconLabel = new JLabel(item.getIcon());
        iconLabel.setFont(EnhancedModernTheme.Fonts.BODY_LARGE);
        iconLabel.setPreferredSize(new Dimension(24, 24));
        
        JLabel textLabel = new JLabel(item.getDisplayName());
        textLabel.setFont(EnhancedModernTheme.Fonts.SIDEBAR_ITEM);
        textLabel.setForeground(item == selectedItem ? 
            EnhancedModernTheme.Colors.TEXT_SIDEBAR_SELECTED : 
            EnhancedModernTheme.Colors.TEXT_SIDEBAR);
        textLabel.setBorder(new EmptyBorder(0, EnhancedModernTheme.Spacing.MEDIUM, 0, 0));
        
        contentPanel.add(iconLabel, BorderLayout.WEST);
        contentPanel.add(textLabel, BorderLayout.CENTER);
        
        itemPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Mouse listeners
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                selectNavigationItem(item);
                if (navigationListener != null) {
                    navigationListener.onNavigationItemSelected(item);
                }
            }
        };
        
        itemPanel.addMouseListener(mouseAdapter);
        
        return itemPanel;
    }
    
    private void createUserSection() {
        JPanel userSection = new JPanel(new BorderLayout());
        userSection.setOpaque(false);
        userSection.setBorder(new EmptyBorder(
            EnhancedModernTheme.Spacing.MEDIUM, 
            EnhancedModernTheme.Spacing.LARGE, 
            EnhancedModernTheme.Spacing.LARGE, 
            EnhancedModernTheme.Spacing.LARGE
        ));
        
        // User info
        SessionManager sessionManager = SessionManager.getInstance();
        if (sessionManager.isLoggedIn()) {
            User currentUser = sessionManager.getCurrentUser();
            
            JPanel userInfo = new JPanel();
            userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
            userInfo.setOpaque(false);
            
            JLabel userIcon = new JLabel(EnhancedModernTheme.Icons.USER);
            userIcon.setFont(EnhancedModernTheme.Fonts.BODY_LARGE);
            
            JLabel userName = new JLabel("Welcome, " + currentUser.getFirstName());
            userName.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
            userName.setForeground(EnhancedModernTheme.Colors.TEXT_SIDEBAR);
            
            JLabel userEmail = new JLabel(currentUser.getEmail());
            userEmail.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
            userEmail.setForeground(EnhancedModernTheme.Colors.TEXT_SIDEBAR);
            
            JPanel userRow = new JPanel(new BorderLayout());
            userRow.setOpaque(false);
            userRow.add(userIcon, BorderLayout.WEST);
            
            JPanel userDetails = new JPanel();
            userDetails.setLayout(new BoxLayout(userDetails, BoxLayout.Y_AXIS));
            userDetails.setOpaque(false);
            userDetails.setBorder(new EmptyBorder(0, EnhancedModernTheme.Spacing.MEDIUM, 0, 0));
            userDetails.add(userName);
            userDetails.add(userEmail);
            
            userRow.add(userDetails, BorderLayout.CENTER);
            userInfo.add(userRow);
            
            userSection.add(userInfo, BorderLayout.NORTH);
        }
        
        // Settings item
        JPanel settingsItem = createNavigationItem(NavigationItem.SETTINGS);
        navigationItems.put(NavigationItem.SETTINGS, settingsItem);
        userSection.add(settingsItem, BorderLayout.SOUTH);
        
        add(userSection, BorderLayout.SOUTH);
    }
    
    public void selectNavigationItem(NavigationItem item) {
        NavigationItem previousItem = selectedItem;
        selectedItem = item;
        
        // Update visual state
        if (navigationItems.containsKey(previousItem)) {
            navigationItems.get(previousItem).repaint();
        }
        if (navigationItems.containsKey(item)) {
            navigationItems.get(item).repaint();
        }
    }
    
    public NavigationItem getSelectedItem() {
        return selectedItem;
    }
    
    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }
}
