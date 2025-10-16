package com.newsvisualizer.gui.components;

import com.newsvisualizer.gui.theme.KeepTooTheme;
import com.newsvisualizer.service.SessionManager;
import com.newsvisualizer.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * KeepToo-inspired sidebar navigation component
 */
public class KeepTooSidebar extends JPanel {
    
    public enum NavigationItem {
        DASHBOARD("Dashboard", KeepTooTheme.Icons.DASHBOARD),
        NEWS_FETCH("News Fetch", KeepTooTheme.Icons.NEWS_FETCH),
        ANALYTICS("Analytics", KeepTooTheme.Icons.ANALYTICS),
        CHARTS("Sentiment Charts", KeepTooTheme.Icons.CHARTS),
        KEYWORDS("Keywords", KeepTooTheme.Icons.KEYWORDS),
        AI_SUMMARY("AI Summary", KeepTooTheme.Icons.AI_SUMMARY),
        TRANSLATION("Translation", KeepTooTheme.Icons.TRANSLATION),
        NEWS_APP("NewsApp", KeepTooTheme.Icons.NEWS_APP),
        HISTORY("Search History", KeepTooTheme.Icons.HISTORY),
        SETTINGS("Settings", KeepTooTheme.Icons.SETTINGS);
        
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
    private List<SidebarItem> sidebarItems = new ArrayList<>();
    
    public KeepTooSidebar() {
        initializeSidebar();
        createSidebarHeader();
        createNavigationItems();
    }
    
    private void initializeSidebar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(KeepTooTheme.Layout.getSidebarWidth(), 800));
        setMinimumSize(new Dimension(KeepTooTheme.Layout.getSidebarWidth(), 600));
        setBackground(KeepTooTheme.Colors.SIDEBAR_PRIMARY);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the purple gradient background like KeepToo
        GradientPaint gradient = KeepTooTheme.Effects.createSidebarGradient(getHeight());
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.dispose();
    }
    
    private void createSidebarHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(
            KeepTooTheme.Spacing.XLARGE, 
            KeepTooTheme.Spacing.LARGE,
            KeepTooTheme.Spacing.XLARGE, 
            KeepTooTheme.Spacing.LARGE
        ));
        
        // App title matching KeepToo style
        JLabel titleLabel = new JLabel("NewsVisualizer");
        titleLabel.setFont(KeepTooTheme.Fonts.SIDEBAR_TITLE);
        titleLabel.setForeground(KeepTooTheme.Colors.TEXT_WHITE);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Professional News Analytics");
        subtitleLabel.setFont(KeepTooTheme.Fonts.BODY_SMALL);
        subtitleLabel.setForeground(KeepTooTheme.Colors.TEXT_SIDEBAR);
        
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(4));
        titleContainer.add(subtitleLabel);
        
        headerPanel.add(titleContainer, BorderLayout.CENTER);
        
        // Add subtle divider line
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(KeepTooTheme.Colors.BORDER_SIDEBAR);
                g.fillRect(KeepTooTheme.Spacing.LARGE, 0, 
                          getWidth() - 2 * KeepTooTheme.Spacing.LARGE, 1);
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(0, 1));
        
        JPanel headerWithDivider = new JPanel(new BorderLayout());
        headerWithDivider.setOpaque(false);
        headerWithDivider.add(headerPanel, BorderLayout.CENTER);
        headerWithDivider.add(divider, BorderLayout.SOUTH);
        
        add(headerWithDivider, BorderLayout.NORTH);
    }
    
    private void createNavigationItems() {
        JPanel itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setOpaque(false);
        itemsContainer.setBorder(new EmptyBorder(
            KeepTooTheme.Spacing.LARGE, 0, 
            KeepTooTheme.Spacing.LARGE, 0
        ));
        
        // Create navigation items
        for (NavigationItem item : NavigationItem.values()) {
            SidebarItem sidebarItem = new SidebarItem(item);
            sidebarItems.add(sidebarItem);
            itemsContainer.add(sidebarItem);
            itemsContainer.add(Box.createVerticalStrut(KeepTooTheme.Spacing.TINY));
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private class SidebarItem extends JPanel {
        private final NavigationItem item;
        private boolean isHovered = false;
        
        public SidebarItem(NavigationItem item) {
            this.item = item;
            initializeItem();
        }
        
        private void initializeItem() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(0, KeepTooTheme.Layout.SIDEBAR_ITEM_HEIGHT));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, KeepTooTheme.Layout.SIDEBAR_ITEM_HEIGHT));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            setBorder(new EmptyBorder(0, KeepTooTheme.Spacing.LARGE, 0, KeepTooTheme.Spacing.LARGE));
            
            // Create item content
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setOpaque(false);
            contentPanel.setBorder(new EmptyBorder(
                KeepTooTheme.Spacing.MEDIUM, 
                KeepTooTheme.Spacing.MEDIUM, 
                KeepTooTheme.Spacing.MEDIUM, 
                KeepTooTheme.Spacing.MEDIUM
            ));
            
            // Icon
            JLabel iconLabel = new JLabel(item.getIcon());
            iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            iconLabel.setForeground(getTextColor());
            iconLabel.setPreferredSize(new Dimension(24, 24));
            
            // Text
            JLabel textLabel = new JLabel(item.getDisplayName());
            textLabel.setFont(KeepTooTheme.Fonts.SIDEBAR_ITEM);
            textLabel.setForeground(getTextColor());
            textLabel.setBorder(new EmptyBorder(0, KeepTooTheme.Spacing.MEDIUM, 0, 0));
            
            contentPanel.add(iconLabel, BorderLayout.WEST);
            contentPanel.add(textLabel, BorderLayout.CENTER);
            
            add(contentPanel, BorderLayout.CENTER);
            
            // Mouse events
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectItem();
                }
            });
        }
        
        private Color getTextColor() {
            if (item == selectedItem) {
                return KeepTooTheme.Colors.TEXT_SIDEBAR_SELECTED;
            } else {
                return KeepTooTheme.Colors.TEXT_SIDEBAR;
            }
        }
        
        private void selectItem() {
            selectedItem = item;
            // Update all items
            for (SidebarItem sidebarItem : sidebarItems) {
                sidebarItem.updateAppearance();
            }
            // Notify listener
            if (navigationListener != null) {
                navigationListener.onNavigationItemSelected(item);
            }
        }
        
        private void updateAppearance() {
            // Update text colors
            Component[] components = ((JPanel) getComponent(0)).getComponents();
            if (components.length >= 2) {
                ((JLabel) components[0]).setForeground(getTextColor()); // Icon
                ((JLabel) components[1]).setForeground(getTextColor()); // Text
            }
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int margin = KeepTooTheme.Spacing.SMALL;
            int width = getWidth() - 2 * margin;
            int height = getHeight();
            
            if (item == selectedItem) {
                // Selected state - purple highlight
                g2d.setColor(KeepTooTheme.Colors.PURPLE_SELECTED);
                g2d.fillRoundRect(margin, 0, width, height, 
                                KeepTooTheme.Radius.MEDIUM, KeepTooTheme.Radius.MEDIUM);
            } else if (isHovered) {
                // Hover state - light purple
                g2d.setColor(KeepTooTheme.Colors.PURPLE_HOVER);
                g2d.fillRoundRect(margin, 0, width, height, 
                                KeepTooTheme.Radius.MEDIUM, KeepTooTheme.Radius.MEDIUM);
            }
            
            g2d.dispose();
        }
    }
    
    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }
    
    public void selectNavigationItem(NavigationItem item) {
        if (selectedItem == item) {
            return; // Prevent recursive calls
        }
        selectedItem = item;
        for (SidebarItem sidebarItem : sidebarItems) {
            sidebarItem.updateAppearance();
        }
        if (navigationListener != null) {
            navigationListener.onNavigationItemSelected(item);
        }
    }
}
