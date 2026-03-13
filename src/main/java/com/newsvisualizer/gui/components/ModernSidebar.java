package com.newsvisualizer.gui.components;

import com.newsvisualizer.gui.theme.EnhancedModernTheme;

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
    private Map<NavigationItem, JLabel> navigationText = new HashMap<>();
    private Map<NavigationItem, JLabel> navigationIcons = new HashMap<>();
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
            EnhancedModernTheme.Spacing.XLARGE,
            EnhancedModernTheme.Spacing.LARGE,
            EnhancedModernTheme.Spacing.LARGE,
            EnhancedModernTheme.Spacing.LARGE
        ));

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
        
        for (NavigationItem item : NavigationItem.values()) {
            if (item != NavigationItem.SETTINGS) {
                JPanel itemPanel = createNavigationItem(item);
                navigationItems.put(item, itemPanel);
                itemsContainer.add(itemPanel);
                itemsContainer.add(Box.createVerticalStrut(6));
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
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hovered = Boolean.TRUE.equals(getClientProperty("hover"));
                
                if (item == selectedItem) {
                    g2d.setColor(EnhancedModernTheme.Colors.SIDEBAR_SELECTED);
                    g2d.fillRoundRect(10, 0, getWidth() - 20, getHeight(),
                                    EnhancedModernTheme.Radius.LARGE,
                                    EnhancedModernTheme.Radius.LARGE);
                } else if (hovered) {
                    g2d.setColor(new Color(255, 255, 255, 18));
                    g2d.fillRoundRect(10, 0, getWidth() - 20, getHeight(),
                                    EnhancedModernTheme.Radius.LARGE,
                                    EnhancedModernTheme.Radius.LARGE);
                }
                
                g2d.dispose();
            }
        };
        
        itemPanel.setOpaque(false);
        itemPanel.setPreferredSize(new Dimension(0, EnhancedModernTheme.Layout.SIDEBAR_ITEM_HEIGHT));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, EnhancedModernTheme.Layout.SIDEBAR_ITEM_HEIGHT));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, EnhancedModernTheme.Spacing.MEDIUM, 0, EnhancedModernTheme.Spacing.LARGE));

        JLabel iconLabel = createIconBadge(item.getIcon(), item == selectedItem);
        navigationIcons.put(item, iconLabel);
        JLabel textLabel = new JLabel(item.getDisplayName());
        textLabel.setFont(EnhancedModernTheme.Fonts.SIDEBAR_ITEM);
        textLabel.setForeground(item == selectedItem ? 
            EnhancedModernTheme.Colors.TEXT_SIDEBAR_SELECTED : 
            EnhancedModernTheme.Colors.TEXT_SIDEBAR);
        textLabel.setBorder(new EmptyBorder(0, EnhancedModernTheme.Spacing.MEDIUM, 0, 0));
        navigationText.put(item, textLabel);

        contentPanel.add(iconLabel, BorderLayout.WEST);
        contentPanel.add(textLabel, BorderLayout.CENTER);
        
        itemPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Mouse listeners
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.putClientProperty("hover", Boolean.TRUE);
                itemPanel.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.putClientProperty("hover", Boolean.FALSE);
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
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel appIcon = new JLabel(EnhancedModernTheme.Icons.INFO);
        appIcon.setFont(EnhancedModernTheme.Fonts.BODY_LARGE);

        JLabel appTitle = new JLabel("Direct Access");
        appTitle.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        appTitle.setForeground(EnhancedModernTheme.Colors.TEXT_SIDEBAR);

        JLabel appSubtitle = new JLabel("Authentication removed");
        appSubtitle.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        appSubtitle.setForeground(EnhancedModernTheme.Colors.TEXT_SIDEBAR);

        JPanel infoRow = new JPanel(new BorderLayout());
        infoRow.setOpaque(false);
        infoRow.add(appIcon, BorderLayout.WEST);

        JPanel infoDetails = new JPanel();
        infoDetails.setLayout(new BoxLayout(infoDetails, BoxLayout.Y_AXIS));
        infoDetails.setOpaque(false);
        infoDetails.setBorder(new EmptyBorder(0, EnhancedModernTheme.Spacing.MEDIUM, 0, 0));
        infoDetails.add(appTitle);
        infoDetails.add(appSubtitle);

        infoRow.add(infoDetails, BorderLayout.CENTER);
        infoPanel.add(infoRow);
        userSection.add(infoPanel, BorderLayout.NORTH);
        
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
        for (Map.Entry<NavigationItem, JLabel> entry : navigationText.entrySet()) {
            entry.getValue().setForeground(entry.getKey() == selectedItem
                ? EnhancedModernTheme.Colors.TEXT_SIDEBAR_SELECTED
                : EnhancedModernTheme.Colors.TEXT_SIDEBAR);
        }
        for (Map.Entry<NavigationItem, JLabel> entry : navigationIcons.entrySet()) {
            boolean active = entry.getKey() == selectedItem;
            entry.getValue().setForeground(active ? EnhancedModernTheme.Colors.WHITE : new Color(226, 232, 240));
            entry.getValue().setBackground(active ? new Color(255, 255, 255, 50) : new Color(255, 255, 255, 12));
        }
    }
    
    public NavigationItem getSelectedItem() {
        return selectedItem;
    }
    
    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }

    private JLabel createIconBadge(String text, boolean selected) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(28, 28));
        badge.setMinimumSize(new Dimension(28, 28));
        badge.setFont(EnhancedModernTheme.Fonts.CAPTION.deriveFont(Font.BOLD, 11f));
        badge.setForeground(selected ? EnhancedModernTheme.Colors.WHITE : new Color(226, 232, 240));
        badge.setOpaque(true);
        badge.setBackground(selected ? new Color(255, 255, 255, 50) : new Color(255, 255, 255, 12));
        badge.setBorder(new EmptyBorder(4, 4, 4, 4));
        return badge;
    }
}
