package com.newsvisualizer.gui.components;

import com.newsvisualizer.gui.theme.ModernTheme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Factory class for creating modern UI components with consistent styling
 */
public class ModernUIComponents {
    
    // Modern Button with hover effects and gradients
    public static class ModernButton extends JButton {
        private final Color baseColor;
        private final Color hoverColor;
        private final Color textColor;
        private boolean isHovered = false;
        private boolean isPressed = false;
        
        public ModernButton(String text, Color baseColor, Color textColor) {
            super(text);
            this.baseColor = baseColor;
            this.textColor = textColor;
            this.hoverColor = baseColor.darker();
            
            setupButton();
        }
        
        private void setupButton() {
            setFont(ModernTheme.Fonts.BUTTON);
            setForeground(textColor);
            setOpaque(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 24, 12, 24));
            
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
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Shadow
            if (isEnabled()) {
                ModernTheme.Shadows.applyCardShadow(g2d, width, height, ModernTheme.Radius.MEDIUM);
            }
            
            // Button background with gradient
            Color buttonColor = isPressed ? baseColor.darker().darker() : 
                               isHovered ? hoverColor : baseColor;
            
            if (!isEnabled()) {
                buttonColor = ModernTheme.Colors.GRAY_300;
            }
            
            GradientPaint gradient = new GradientPaint(
                0, 0, buttonColor.brighter(),
                0, height, buttonColor
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(0, 0, width, height, ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
            
            // Border
            g2d.setColor(buttonColor.darker());
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawRoundRect(0, 0, width - 1, height - 1, ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
            
            g2d.dispose();
            
            // Paint text
            super.paintComponent(g);
        }
    }
    
    // Modern ComboBox
    public static class ModernComboBox extends JComboBox<String> {
        public ModernComboBox(String[] items) {
            super(items);
            setupComboBox();
        }
        
        private void setupComboBox() {
            setFont(ModernTheme.Fonts.BODY_MEDIUM);
            setBackground(ModernTheme.Colors.WHITE);
            setForeground(ModernTheme.Colors.TEXT_PRIMARY);
            setBorder(ModernTheme.Borders.createDefaultBorder());
            setPreferredSize(new Dimension(200, 42));
        }
    }
    
    // Modern TextField
    public static class ModernTextField extends JTextField {
        public ModernTextField(int columns) {
            super(columns);
            setupTextField();
        }
        
        public ModernTextField(String text) {
            super(text);
            setupTextField();
        }
        
        private void setupTextField() {
            setFont(ModernTheme.Fonts.BODY_MEDIUM);
            setBackground(ModernTheme.Colors.WHITE);
            setForeground(ModernTheme.Colors.TEXT_PRIMARY);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setPreferredSize(new Dimension(getPreferredSize().width, 42));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Background
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
            
            // Border
            g2d.setColor(hasFocus() ? ModernTheme.Colors.PRIMARY : ModernTheme.Colors.BORDER_DEFAULT);
            g2d.setStroke(new BasicStroke(hasFocus() ? 2.0f : 1.0f));
            g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
            
            g2d.dispose();
            super.paintComponent(g);
        }
    }
    
    // Modern PasswordField
    public static class ModernPasswordField extends JPasswordField {
        public ModernPasswordField(int columns) {
            super(columns);
            setupPasswordField();
        }
        
        private void setupPasswordField() {
            setFont(ModernTheme.Fonts.BODY_MEDIUM);
            setBackground(ModernTheme.Colors.WHITE);
            setForeground(ModernTheme.Colors.TEXT_PRIMARY);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setPreferredSize(new Dimension(getPreferredSize().width, 42));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Background
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
            
            // Border
            g2d.setColor(hasFocus() ? ModernTheme.Colors.PRIMARY : ModernTheme.Colors.BORDER_DEFAULT);
            g2d.setStroke(new BasicStroke(hasFocus() ? 2.0f : 1.0f));
            g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
            
            g2d.dispose();
            super.paintComponent(g);
        }
    }
    
    // Modern Progress Bar
    public static class ModernProgressBar extends JProgressBar {
        public ModernProgressBar() {
            super();
            setupProgressBar();
        }
        
        private void setupProgressBar() {
            setStringPainted(true);
            setFont(ModernTheme.Fonts.BODY_SMALL);
            setForeground(ModernTheme.Colors.PRIMARY);
            setBackground(ModernTheme.Colors.GRAY_200);
            setBorderPainted(false);
            setPreferredSize(new Dimension(getPreferredSize().width, 8));
            
            setUI(new BasicProgressBarUI() {
                @Override
                protected void paintDeterminate(Graphics g, JComponent c) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int width = c.getWidth();
                    int height = c.getHeight();
                    
                    // Background
                    g2d.setColor(ModernTheme.Colors.GRAY_200);
                    g2d.fillRoundRect(0, 0, width, height, height/2, height/2);
                    
                    // Progress
                    int progressWidth = (int) (width * (progressBar.getPercentComplete()));
                    if (progressWidth > 0) {
                        GradientPaint gradient = new GradientPaint(
                            0, 0, ModernTheme.Colors.PRIMARY_LIGHT,
                            progressWidth, 0, ModernTheme.Colors.PRIMARY
                        );
                        g2d.setPaint(gradient);
                        g2d.fillRoundRect(0, 0, progressWidth, height, height/2, height/2);
                    }
                    
                    g2d.dispose();
                }
                
                @Override
                protected void paintIndeterminate(Graphics g, JComponent c) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int width = c.getWidth();
                    int height = c.getHeight();
                    
                    // Background
                    g2d.setColor(ModernTheme.Colors.GRAY_200);
                    g2d.fillRoundRect(0, 0, width, height, height/2, height/2);
                    
                    // Indeterminate animation
                    long time = System.currentTimeMillis();
                    int animationWidth = width / 3;
                    int x = (int) ((time / 10) % (width + animationWidth)) - animationWidth;
                    
                    if (x < width && x + animationWidth > 0) {
                        GradientPaint gradient = new GradientPaint(
                            x, 0, new Color(ModernTheme.Colors.PRIMARY.getRed(), ModernTheme.Colors.PRIMARY.getGreen(), ModernTheme.Colors.PRIMARY.getBlue(), 100),
                            x + animationWidth, 0, new Color(ModernTheme.Colors.PRIMARY.getRed(), ModernTheme.Colors.PRIMARY.getGreen(), ModernTheme.Colors.PRIMARY.getBlue(), 0)
                        );
                        g2d.setPaint(gradient);
                        g2d.fillRoundRect(Math.max(0, x), 0, Math.min(animationWidth, width - Math.max(0, x)), height, height/2, height/2);
                    }
                    
                    g2d.dispose();
                }
            });
        }
    }
    
    // Modern Card Panel
    public static class ModernCard extends JPanel {
        private final String title;
        private final boolean showShadow;
        
        public ModernCard(String title, boolean showShadow) {
            this.title = title;
            this.showShadow = showShadow;
            setupCard();
        }
        
        public ModernCard(String title) {
            this(title, true);
        }
        
        private void setupCard() {
            setOpaque(false);
            setBorder(new EmptyBorder(ModernTheme.Spacing.MEDIUM, ModernTheme.Spacing.MEDIUM, 
                                    ModernTheme.Spacing.MEDIUM, ModernTheme.Spacing.MEDIUM));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Shadow
            if (showShadow) {
                ModernTheme.Shadows.applyCardShadow(g2d, width, height, ModernTheme.Radius.LARGE);
            }
            
            // Background
            g2d.setColor(ModernTheme.Colors.BACKGROUND_CARD);
            g2d.fillRoundRect(0, 0, width, height, ModernTheme.Radius.LARGE, ModernTheme.Radius.LARGE);
            
            // Border
            g2d.setColor(ModernTheme.Colors.BORDER_LIGHT);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawRoundRect(0, 0, width - 1, height - 1, ModernTheme.Radius.LARGE, ModernTheme.Radius.LARGE);
            
            // Title
            if (title != null && !title.isEmpty()) {
                g2d.setColor(ModernTheme.Colors.TEXT_PRIMARY);
                g2d.setFont(ModernTheme.Fonts.HEADING_MEDIUM);
                FontMetrics fm = g2d.getFontMetrics();
                int titleY = ModernTheme.Spacing.MEDIUM + fm.getAscent();
                g2d.drawString(title, ModernTheme.Spacing.MEDIUM, titleY);
            }
            
            g2d.dispose();
            super.paintComponent(g);
        }
    }
    
    // Modern TabbedPane
    public static class ModernTabbedPane extends JTabbedPane {
        public ModernTabbedPane() {
            super();
            setupTabbedPane();
        }
        
        private void setupTabbedPane() {
            setFont(ModernTheme.Fonts.BODY_MEDIUM);
            setBackground(ModernTheme.Colors.BACKGROUND_PRIMARY);
            setForeground(ModernTheme.Colors.TEXT_PRIMARY);
            
            setUI(new BasicTabbedPaneUI() {
                @Override
                protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                                       Rectangle iconRect, Rectangle textRect) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    Rectangle tabRect = rects[tabIndex];
                    boolean isSelected = tabIndex == getSelectedIndex();
                    
                    // Tab background
                    if (isSelected) {
                        g2d.setColor(ModernTheme.Colors.WHITE);
                        g2d.fillRoundRect(tabRect.x, tabRect.y, tabRect.width, tabRect.height, 
                                        ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
                        
                        // Selected tab border
                        g2d.setColor(ModernTheme.Colors.PRIMARY);
                        g2d.setStroke(new BasicStroke(2.0f));
                        g2d.drawRoundRect(tabRect.x + 1, tabRect.y + 1, tabRect.width - 2, tabRect.height - 2, 
                                        ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
                    } else {
                        g2d.setColor(ModernTheme.Colors.GRAY_50);
                        g2d.fillRoundRect(tabRect.x, tabRect.y, tabRect.width, tabRect.height, 
                                        ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
                    }
                    
                    g2d.dispose();
                    super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
                }
                
                @Override
                protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int width = tabPane.getWidth();
                    int height = tabPane.getHeight();
                    int tabHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                    
                    // Content area background
                    g2d.setColor(ModernTheme.Colors.WHITE);
                    g2d.fillRoundRect(0, tabHeight, width, height - tabHeight, 
                                    ModernTheme.Radius.LARGE, ModernTheme.Radius.LARGE);
                    
                    // Content area border
                    g2d.setColor(ModernTheme.Colors.BORDER_LIGHT);
                    g2d.setStroke(new BasicStroke(1.0f));
                    g2d.drawRoundRect(0, tabHeight, width - 1, height - tabHeight - 1, 
                                    ModernTheme.Radius.LARGE, ModernTheme.Radius.LARGE);
                    
                    g2d.dispose();
                }
            });
        }
    }
    
    // Factory methods
    public static ModernButton createPrimaryButton(String text) {
        return new ModernButton(text, ModernTheme.Colors.PRIMARY, ModernTheme.Colors.WHITE);
    }
    
    public static ModernButton createSecondaryButton(String text) {
        return new ModernButton(text, ModernTheme.Colors.SECONDARY, ModernTheme.Colors.WHITE);
    }
    
    public static ModernButton createAccentButton(String text) {
        return new ModernButton(text, ModernTheme.Colors.ACCENT, ModernTheme.Colors.WHITE);
    }
    
    public static ModernButton createOutlineButton(String text) {
        return new ModernButton(text, ModernTheme.Colors.WHITE, ModernTheme.Colors.PRIMARY);
    }
    
    public static ModernComboBox createComboBox(String[] items) {
        return new ModernComboBox(items);
    }
    
    public static ModernTextField createTextField(int columns) {
        return new ModernTextField(columns);
    }
    
    public static ModernTextField createTextField(String text) {
        return new ModernTextField(text);
    }
    
    public static ModernPasswordField createPasswordField(int columns) {
        return new ModernPasswordField(columns);
    }
    
    public static ModernProgressBar createProgressBar() {
        return new ModernProgressBar();
    }
    
    public static ModernCard createCard(String title) {
        return new ModernCard(title);
    }
    
    public static ModernCard createCard(String title, boolean showShadow) {
        return new ModernCard(title, showShadow);
    }
    
    public static ModernTabbedPane createTabbedPane() {
        return new ModernTabbedPane();
    }
}