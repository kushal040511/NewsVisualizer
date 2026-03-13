package com.newsvisualizer.gui.theme;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * KeepToo-inspired theme matching the design from the reference image
 */
public class KeepTooTheme {
    
    // KeepToo Color Palette - Matching the reference image
    public static class Colors {
        // Primary Purple Colors (from the sidebar)
        public static final Color SIDEBAR_PRIMARY = new Color(103, 58, 183);      // Deep purple
        public static final Color SIDEBAR_SECONDARY = new Color(124, 77, 255);    // Lighter purple
        public static final Color SIDEBAR_DARK = new Color(81, 45, 168);          // Darker purple
        public static final Color SIDEBAR_LIGHT = new Color(159, 168, 218);       // Light purple for hover
        
        // Purple Gradient Colors
        public static final Color PURPLE_GRADIENT_START = new Color(103, 58, 183);
        public static final Color PURPLE_GRADIENT_END = new Color(124, 77, 255);
        public static final Color PURPLE_SELECTED = new Color(142, 101, 249);
        public static final Color PURPLE_HOVER = new Color(130, 87, 229);
        
        // Main Content Area Colors
        public static final Color CONTENT_BACKGROUND = Color.WHITE;
        public static final Color CONTENT_HEADER = new Color(248, 249, 250);
        public static final Color TABLE_HEADER = new Color(124, 77, 255);         // Purple header
        public static final Color TABLE_SELECTED = new Color(142, 101, 249, 50);  // Light purple selection
        public static final Color TABLE_HOVER = new Color(159, 168, 218, 30);     // Subtle hover
        
        // Text Colors
        public static final Color TEXT_WHITE = Color.WHITE;
        public static final Color TEXT_SIDEBAR = new Color(255, 255, 255, 230);
        public static final Color TEXT_SIDEBAR_SELECTED = Color.WHITE;
        public static final Color TEXT_PRIMARY = new Color(33, 37, 41);
        public static final Color TEXT_SECONDARY = new Color(108, 117, 125);
        public static final Color TEXT_MUTED = new Color(134, 142, 150);
        
        // Border and Divider Colors
        public static final Color BORDER_LIGHT = new Color(233, 236, 239);
        public static final Color BORDER_MEDIUM = new Color(206, 212, 218);
        public static final Color BORDER_SIDEBAR = new Color(255, 255, 255, 20);
        
        // Additional Colors needed by components
        public static final Color BACKGROUND_WHITE = Color.WHITE;
        public static final Color BACKGROUND_LIGHT = new Color(248, 249, 250);
        public static final Color PRIMARY = new Color(103, 58, 183);
        public static final Color PRIMARY_HOVER = new Color(124, 77, 255);
        
        // Status Colors
        public static final Color SUCCESS = new Color(40, 167, 69);
        public static final Color WARNING = new Color(255, 193, 7);
        public static final Color ERROR = new Color(220, 53, 69);
        public static final Color INFO = new Color(23, 162, 184);
    }
    
    // Typography - Clean and Professional
    public static class Fonts {
        // Get system appropriate fonts
        private static final String[] PREFERRED_FONTS = {
            "SF Pro Display", "Segoe UI", "Roboto", "Helvetica Neue", "Arial", "sans-serif"
        };
        
        private static String getSystemFont() {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] availableFonts = ge.getAvailableFontFamilyNames();
            
            for (String preferredFont : PREFERRED_FONTS) {
                for (String availableFont : availableFonts) {
                    if (availableFont.equals(preferredFont)) {
                        return preferredFont;
                    }
                }
            }
            return "SansSerif"; // fallback
        }
        
        private static final String FONT_FAMILY = getSystemFont();
        
        // Font definitions matching KeepToo style
        public static final Font APP_TITLE = new Font(FONT_FAMILY, Font.BOLD, 22);
        public static final Font SIDEBAR_TITLE = new Font(FONT_FAMILY, Font.BOLD, 20);
        public static final Font SECTION_TITLE = new Font(FONT_FAMILY, Font.BOLD, 18);
        public static final Font PAGE_TITLE = new Font(FONT_FAMILY, Font.BOLD, 24);
        public static final Font CONTENT_TITLE = new Font(FONT_FAMILY, Font.PLAIN, 16);
        public static final Font SIDEBAR_ITEM = new Font(FONT_FAMILY, Font.PLAIN, 15);
        public static final Font BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
        public static final Font BODY_REGULAR = new Font(FONT_FAMILY, Font.PLAIN, 14);
        public static final Font BODY_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
        public static final Font TABLE_HEADER = new Font(FONT_FAMILY, Font.BOLD, 13);
        public static final Font TABLE_CELL = new Font(FONT_FAMILY, Font.PLAIN, 13);
        public static final Font BUTTON = new Font(FONT_FAMILY, Font.PLAIN, 14);
        public static final Font LABEL = new Font(FONT_FAMILY, Font.BOLD, 12);
    }
    
    // Layout Constants
    public static class Layout {
        public static final int SIDEBAR_WIDTH = 270;
        public static final int SIDEBAR_ITEM_HEIGHT = 45;
        public static final int CONTENT_PADDING = 30;
        public static final int SECTION_PADDING = 20;
        public static final int TABLE_ROW_HEIGHT = 40;
        public static final int BUTTON_HEIGHT = 36;
        public static final int INPUT_HEIGHT = 36;
        
        // Responsive sidebar width
        public static int getSidebarWidth() {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            if (screenSize.width <= 1366) {
                return 250; // Narrower for small screens
            }
            return SIDEBAR_WIDTH;
        }
    }
    
    // Spacing Constants
    public static class Spacing {
        public static final int TINY = 4;
        public static final int SMALL = 8;
        public static final int MEDIUM = 12;
        public static final int LARGE = 16;
        public static final int XLARGE = 24;
        public static final int XXLARGE = 32;
    }
    
    // Border Radius
    public static class Radius {
        public static final int SMALL = 4;
        public static final int MEDIUM = 6;
        public static final int LARGE = 8;
        public static final int CARD = 8;
    }
    
    // Icons - Professional set for NewsVisualizer
    public static class Icons {
        // Navigation Icons
        public static final String DASHBOARD = "🏠";
        public static final String NEWS_FETCH = "📰";
        public static final String ANALYTICS = "📊";
        public static final String CHARTS = "📈";
        public static final String KEYWORDS = "🏷️";
        public static final String AI_SUMMARY = "🤖";
        public static final String TRANSLATION = "🌐";
        public static final String NEWS_APP = "📱";
        public static final String HISTORY = "📋";
        public static final String SETTINGS = "⚙️";
        
        // Action Icons
        public static final String SEARCH = "🔍";
        public static final String REFRESH = "🔄";
        public static final String EXPORT = "📤";
        public static final String FILTER = "🎯";
        public static final String DOWNLOAD = "📥";
        
        // Status Icons
        public static final String SUCCESS = "✅";
        public static final String ERROR = "❌";
        public static final String WARNING = "⚠️";
        public static final String INFO = "ℹ️";
        public static final String LOADING = "⏳";
    }
    
    // Gradients and Effects
    public static class Effects {
        public static GradientPaint createSidebarGradient(int height) {
            return new GradientPaint(
                0, 0, Colors.PURPLE_GRADIENT_START,
                0, height, Colors.PURPLE_GRADIENT_END
            );
        }
        
        public static GradientPaint createButtonGradient(int height) {
            return new GradientPaint(
                0, 0, Colors.PURPLE_GRADIENT_START.brighter(),
                0, height, Colors.PURPLE_GRADIENT_START
            );
        }
        
        public static void applyShadow(Graphics2D g2d, int x, int y, int width, int height, int radius) {
            // Subtle shadow effect
            g2d.setColor(new Color(0, 0, 0, 10));
            g2d.fillRoundRect(x + 2, y + 2, width, height, radius, radius);
            g2d.setColor(new Color(0, 0, 0, 5));
            g2d.fillRoundRect(x + 1, y + 1, width, height, radius, radius);
        }
        
        public static void applyTableRowHighlight(Graphics2D g2d, int x, int y, int width, int height, boolean isSelected) {
            if (isSelected) {
                g2d.setColor(Colors.TABLE_SELECTED);
                g2d.fillRect(x, y, width, height);
            }
        }
    }
}