package com.newsvisualizer.gui.components;

import com.newsvisualizer.gui.theme.KeepTooTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * KeepToo-inspired main content area component
 */
public class KeepTooContentArea extends JPanel {
    
    private JPanel headerPanel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JPanel toolbarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    public KeepTooContentArea() {
        initializeContentArea();
        createHeader();
        createContent();
    }
    
    private void initializeContentArea() {
        setLayout(new BorderLayout());
        setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        setBorder(new EmptyBorder(0, 0, 0, 0));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Clean white background
        g2d.setColor(KeepTooTheme.Colors.BACKGROUND_WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.dispose();
    }
    
    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        headerPanel.setBorder(new EmptyBorder(
            KeepTooTheme.Spacing.XLARGE,
            KeepTooTheme.Spacing.XLARGE,
            KeepTooTheme.Spacing.LARGE,
            KeepTooTheme.Spacing.XLARGE
        ));
        
        // Title and subtitle area
        JPanel titleArea = new JPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        
        titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(KeepTooTheme.Fonts.PAGE_TITLE);
        titleLabel.setForeground(KeepTooTheme.Colors.TEXT_PRIMARY);
        
        subtitleLabel = new JLabel("Welcome to NewsVisualizer");
        subtitleLabel.setFont(KeepTooTheme.Fonts.BODY);
        subtitleLabel.setForeground(KeepTooTheme.Colors.TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        
        titleArea.add(titleLabel);
        titleArea.add(subtitleLabel);
        
        // Toolbar area for action buttons
        toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toolbarPanel.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        
        headerPanel.add(titleArea, BorderLayout.WEST);
        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        
        // Add subtle bottom border
        JPanel headerWithBorder = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(KeepTooTheme.Colors.BORDER_LIGHT);
                g2d.fillRect(KeepTooTheme.Spacing.XLARGE, getHeight() - 1,
                           getWidth() - 2 * KeepTooTheme.Spacing.XLARGE, 1);
                g2d.dispose();
            }
        };
        headerWithBorder.setOpaque(false);
        headerWithBorder.add(headerPanel, BorderLayout.CENTER);
        
        add(headerWithBorder, BorderLayout.NORTH);
    }
    
    private void createContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        contentPanel.setBorder(new EmptyBorder(
            KeepTooTheme.Spacing.LARGE,
            KeepTooTheme.Spacing.XLARGE,
            KeepTooTheme.Spacing.XLARGE,
            KeepTooTheme.Spacing.XLARGE
        ));
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Sets the page title and subtitle
     */
    public void setPageInfo(String title, String subtitle) {
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle);
        repaint();
    }
    
    /**
     * Adds a toolbar button with KeepToo styling
     */
    public JButton addToolbarButton(String text, String icon, String tooltip) {
        JButton button = createStyledButton(text, icon, false);
        if (tooltip != null) {
            button.setToolTipText(tooltip);
        }
        
        toolbarPanel.add(Box.createHorizontalStrut(KeepTooTheme.Spacing.MEDIUM));
        toolbarPanel.add(button);
        toolbarPanel.revalidate();
        
        return button;
    }
    
    /**
     * Adds a primary toolbar button with accent styling
     */
    public JButton addPrimaryToolbarButton(String text, String icon, String tooltip) {
        JButton button = createStyledButton(text, icon, true);
        if (tooltip != null) {
            button.setToolTipText(tooltip);
        }
        
        toolbarPanel.add(Box.createHorizontalStrut(KeepTooTheme.Spacing.MEDIUM));
        toolbarPanel.add(button);
        toolbarPanel.revalidate();
        
        return button;
    }
    
    /**
     * Clears all toolbar buttons
     */
    public void clearToolbar() {
        toolbarPanel.removeAll();
        toolbarPanel.revalidate();
        toolbarPanel.repaint();
    }
    
    /**
     * Adds a content panel with a given name
     */
    public void addContentPanel(String name, JPanel panel) {
        contentPanel.add(panel, name);
    }
    
    /**
     * Shows a specific content panel
     */
    public void showContentPanel(String name) {
        cardLayout.show(contentPanel, name);
    }
    
    /**
     * Gets the main content panel container
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }
    
    /**
     * Creates a KeepToo-styled button
     */
    private JButton createStyledButton(String text, String icon, boolean isPrimary) {
        JButton button = new JButton();
        
        if (icon != null && !icon.isEmpty()) {
            if (text != null && !text.isEmpty()) {
                button.setText(icon + "  " + text);
            } else {
                button.setText(icon);
            }
        } else {
            button.setText(text);
        }
        
        button.setFont(KeepTooTheme.Fonts.BUTTON);
        button.setBorder(new EmptyBorder(
            KeepTooTheme.Spacing.MEDIUM,
            KeepTooTheme.Spacing.LARGE,
            KeepTooTheme.Spacing.MEDIUM,
            KeepTooTheme.Spacing.LARGE
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isPrimary) {
            button.setBackground(KeepTooTheme.Colors.PRIMARY);
            button.setForeground(KeepTooTheme.Colors.TEXT_WHITE);
        } else {
            button.setBackground(KeepTooTheme.Colors.BACKGROUND_LIGHT);
            button.setForeground(KeepTooTheme.Colors.TEXT_PRIMARY);
        }
        
        // Custom button painting for rounded corners
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(KeepTooTheme.Colors.PRIMARY_HOVER);
                } else {
                    button.setBackground(KeepTooTheme.Colors.BORDER_LIGHT);
                }
                button.repaint();
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(KeepTooTheme.Colors.PRIMARY);
                } else {
                    button.setBackground(KeepTooTheme.Colors.BACKGROUND_LIGHT);
                }
                button.repaint();
            }
        });
        
        // Override paintComponent for rounded buttons
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JButton button = (JButton) c;
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded background
                g2d.setColor(button.getBackground());
                g2d.fillRoundRect(0, 0, button.getWidth(), button.getHeight(),
                                KeepTooTheme.Radius.MEDIUM, KeepTooTheme.Radius.MEDIUM);
                
                // Draw border for non-primary buttons
                if (!isPrimary) {
                    g2d.setColor(KeepTooTheme.Colors.BORDER_MEDIUM);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(0, 0, button.getWidth() - 1, button.getHeight() - 1,
                                    KeepTooTheme.Radius.MEDIUM, KeepTooTheme.Radius.MEDIUM);
                }
                
                g2d.dispose();
                super.paint(g, c);
            }
        });
        
        return button;
    }
    
    /**
     * Creates a KeepToo-styled card panel
     */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(KeepTooTheme.Colors.BORDER_LIGHT, 1),
            new EmptyBorder(
                KeepTooTheme.Spacing.LARGE,
                KeepTooTheme.Spacing.LARGE,
                KeepTooTheme.Spacing.LARGE,
                KeepTooTheme.Spacing.LARGE
            )
        ));
        return card;
    }
    
    /**
     * Creates a KeepToo-styled section header
     */
    public static JPanel createSectionHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(KeepTooTheme.Colors.BACKGROUND_WHITE);
        header.setBorder(new EmptyBorder(0, 0, KeepTooTheme.Spacing.LARGE, 0));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(KeepTooTheme.Fonts.SECTION_TITLE);
        titleLabel.setForeground(KeepTooTheme.Colors.TEXT_PRIMARY);
        
        header.add(titleLabel);
        
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(KeepTooTheme.Fonts.BODY_SMALL);
            subtitleLabel.setForeground(KeepTooTheme.Colors.TEXT_SECONDARY);
            subtitleLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
            header.add(subtitleLabel);
        }
        
        return header;
    }
}
