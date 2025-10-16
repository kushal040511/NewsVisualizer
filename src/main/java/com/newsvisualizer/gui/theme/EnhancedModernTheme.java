package com.newsvisualizer.gui.theme;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

/**
 * Enhanced modern theme for NewsVisualizer with sidebar navigation styling
 */
public class EnhancedModernTheme {
    
    // Enhanced Color Palette
    public static class Colors {
        // Sidebar Colors (Purple theme like KeepToo)
        public static final Color SIDEBAR_PRIMARY = new Color(88, 86, 214);     // Deep purple
        public static final Color SIDEBAR_SECONDARY = new Color(106, 104, 225); // Lighter purple
        public static final Color SIDEBAR_DARK = new Color(70, 68, 170);        // Darker purple
        public static final Color SIDEBAR_HOVER = new Color(120, 118, 240);     // Hover purple
        public static final Color SIDEBAR_SELECTED = new Color(140, 138, 255);  // Selected purple
        
        // Primary Colors
        public static final Color PRIMARY = new Color(59, 130, 246);        // Blue 500
        public static final Color PRIMARY_LIGHT = new Color(147, 197, 253); // Blue 300
        public static final Color PRIMARY_DARK = new Color(29, 78, 216);    // Blue 700
        
        // Secondary Colors  
        public static final Color SECONDARY = new Color(16, 185, 129);      // Emerald 500
        public static final Color SECONDARY_LIGHT = new Color(110, 231, 183); // Emerald 300
        public static final Color SUCCESS = new Color(34, 197, 94);         // Green 500
        
        // Accent Colors
        public static final Color ACCENT = new Color(147, 51, 234);         // Purple 600
        public static final Color WARNING = new Color(245, 158, 11);        // Amber 500
        public static final Color ERROR = new Color(239, 68, 68);           // Red 500
        public static final Color INFO = new Color(59, 130, 246);           // Blue 500
        
        // Neutral Colors
        public static final Color WHITE = new Color(255, 255, 255);
        public static final Color GRAY_50 = new Color(249, 250, 251);
        public static final Color GRAY_100 = new Color(243, 244, 246);
        public static final Color GRAY_200 = new Color(229, 231, 235);
        public static final Color GRAY_300 = new Color(209, 213, 219);
        public static final Color GRAY_400 = new Color(156, 163, 175);
        public static final Color GRAY_500 = new Color(107, 114, 128);
        public static final Color GRAY_600 = new Color(75, 85, 99);
        public static final Color GRAY_700 = new Color(55, 65, 81);
        public static final Color GRAY_800 = new Color(31, 41, 55);
        public static final Color GRAY_900 = new Color(17, 24, 39);
        
        // Background Colors
        public static final Color BACKGROUND_PRIMARY = new Color(248, 249, 250);
        public static final Color BACKGROUND_SECONDARY = WHITE;
        public static final Color BACKGROUND_CARD = WHITE;
        public static final Color BACKGROUND_CONTENT = new Color(252, 252, 253);
        
        // Text Colors
        public static final Color TEXT_PRIMARY = GRAY_900;
        public static final Color TEXT_SECONDARY = GRAY_600;
        public static final Color TEXT_MUTED = GRAY_500;
        public static final Color TEXT_WHITE = WHITE;
        public static final Color TEXT_SIDEBAR = new Color(255, 255, 255, 230);
        public static final Color TEXT_SIDEBAR_SELECTED = WHITE;
        
        // Border Colors
        public static final Color BORDER_LIGHT = GRAY_200;
        public static final Color BORDER_DEFAULT = GRAY_300;
        public static final Color BORDER_STRONG = GRAY_400;
        public static final Color BORDER_SIDEBAR = new Color(255, 255, 255, 30);
        
        // Shadow Colors
        public static final Color SHADOW_LIGHT = new Color(0, 0, 0, 8);
        public static final Color SHADOW_DEFAULT = new Color(0, 0, 0, 12);
        public static final Color SHADOW_STRONG = new Color(0, 0, 0, 20);
    }
    
    // Typography
    public static class Fonts {
        // System fonts with improved hierarchy
        private static final String[] PREFERRED_FONTS = {
            "SF Pro Display", "Segoe UI", "Roboto", "Helvetica Neue", "Arial", "sans-serif"
        };
        private static final String[] PREFERRED_TEXT_FONTS = {
            "SF Pro Text", "Segoe UI", "Roboto", "Helvetica Neue", "Arial", "sans-serif"
        };
        
        public static Font getDisplayFont(int style, int size) {
            return new Font(getAvailableFont(PREFERRED_FONTS), style, size);
        }
        
        public static Font getTextFont(int style, int size) {
            return new Font(getAvailableFont(PREFERRED_TEXT_FONTS), style, size);
        }
        
        private static String getAvailableFont(String[] fonts) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] availableFonts = ge.getAvailableFontFamilyNames();
            
            for (String preferredFont : fonts) {
                for (String availableFont : availableFonts) {
                    if (availableFont.equals(preferredFont)) {
                        return preferredFont;
                    }
                }
            }
            return fonts[fonts.length - 1]; // fallback
        }
        
        // Font definitions
        public static final Font APP_TITLE = getDisplayFont(Font.BOLD, 24);
        public static final Font SECTION_TITLE = getDisplayFont(Font.BOLD, 20);
        public static final Font CARD_TITLE = getDisplayFont(Font.BOLD, 18);
        public static final Font SIDEBAR_TITLE = getDisplayFont(Font.BOLD, 16);
        public static final Font BODY_LARGE = getTextFont(Font.PLAIN, 16);
        public static final Font BODY_MEDIUM = getTextFont(Font.PLAIN, 14);
        public static final Font BODY_SMALL = getTextFont(Font.PLAIN, 12);
        public static final Font BUTTON = getTextFont(Font.BOLD, 14);
        public static final Font LABEL = getTextFont(Font.BOLD, 13);
        public static final Font SIDEBAR_ITEM = getTextFont(Font.PLAIN, 14);
        public static final Font CAPTION = getTextFont(Font.PLAIN, 11);
    }
    
    // Layout Constants
    public static class Layout {
        // Responsive sidebar width based on screen size
        public static int getSidebarWidth() {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width;
            
            if (screenWidth <= 1366) {        // Small laptops
                return 240;  // Narrower sidebar
            } else if (screenWidth <= 1920) {  // Standard laptops/desktops
                return 280;  // Default width
            } else {                          // Large screens
                return 320;  // Wider sidebar
            }
        }
        
        public static final int SIDEBAR_WIDTH = getSidebarWidth();
        public static final int HEADER_HEIGHT = 80;
        
        // Responsive content padding
        public static int getContentPadding() {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width;
            
            if (screenWidth <= 1366) {        // Small laptops
                return 16;  // Smaller padding
            } else if (screenWidth <= 1920) {  // Standard laptops/desktops
                return 24;  // Default padding
            } else {                          // Large screens
                return 32;  // Larger padding
            }
        }
        
        public static final int CONTENT_PADDING = getContentPadding();
        public static final int CARD_PADDING = 20;
        public static final int SIDEBAR_ITEM_HEIGHT = 48;
        public static final int BUTTON_HEIGHT = 40;
        public static final int INPUT_HEIGHT = 42;
    }
    
    // Spacing
    public static class Spacing {
        public static final int TINY = 4;
        public static final int SMALL = 8;
        public static final int MEDIUM = 16;
        public static final int LARGE = 24;
        public static final int XLARGE = 32;
        public static final int XXLARGE = 48;
    }
    
    // Border Radius
    public static class Radius {
        public static final int SMALL = 6;
        public static final int MEDIUM = 8;
        public static final int LARGE = 12;
        public static final int XLARGE = 16;
        public static final int CARD = 12;
        public static final int BUTTON = 8;
    }
    
    // Professional Icons - Comprehensive Set
    public static class Icons {
        // Navigation Icons - Professional and Clear
        public static final String DASHBOARD = "🏠";        // Home/Dashboard
        public static final String NEWS = "📰";             // News Fetch
        public static final String ANALYTICS = "📊";        // Analytics
        public static final String CHARTS = "📈";           // Charts
        public static final String FILTER = "🎯";           // Keywords/Filter
        public static final String AI = "🤖";               // AI Summary
        public static final String TRANSLATE = "🌐";        // Translation
        public static final String HISTORY = "📋";          // History
        public static final String SETTINGS = "⚙️";         // Settings
        public static final String NEWSAPP = "📱";          // NewsApp Integration
        
        // User Icons
        public static final String USER = "👤";
        public static final String PROFILE = "👨‍💼";
        public static final String LOGIN = "🔐";
        public static final String LOGOUT = "🚪";
        
        // Action Icons - Enhanced Set
        public static final String SEARCH = "🔍";
        public static final String REFRESH = "🔄";
        public static final String DOWNLOAD = "📥";
        public static final String EXPORT = "📤";
        public static final String DELETE = "🗑️";
        public static final String EDIT = "✏️";
        public static final String CLEAR = "🧹";
        public static final String COPY = "📋";
        public static final String SAVE = "💾";
        public static final String PRINT = "🖨️";
        public static final String SHARE = "📤";
        public static final String VIEW = "👁️";
        public static final String CLOSE = "✕";
        public static final String MENU = "☰";
        
        // Content Icons
        public static final String ARTICLE = "📄";
        public static final String IMAGE = "🖼️";
        public static final String VIDEO = "🎥";
        public static final String LINK = "🔗";
        public static final String BOOKMARK = "🔖";
        public static final String TAG = "🏷️";
        public static final String CATEGORY = "📂";
        public static final String SOURCE = "🏢";
        
        // Status Icons
        public static final String SUCCESS = "✅";
        public static final String ERROR = "❌";
        public static final String WARNING = "⚠️";
        public static final String INFO = "ℹ️";
        public static final String LOADING = "⏳";
        public static final String PROCESSING = "⚡";
        public static final String COMPLETED = "🎉";
        
        // Navigation Arrows
        public static final String ARROW_RIGHT = "→";
        public static final String ARROW_LEFT = "←";
        public static final String ARROW_DOWN = "↓";
        public static final String ARROW_UP = "↑";
        
        // Data Icons
        public static final String DATABASE = "🗄️";
        public static final String CLOUD = "☁️";
        public static final String SERVER = "🖥️";
        public static final String API = "🔌";
        
        // Countries/Regions for News Sources
        public static final String INDIA = "🇮🇳";
        public static final String USA = "🇺🇸";
        public static final String UK = "🇬🇧";
        public static final String GLOBAL = "🌍";
        public static final String CANADA = "🇨🇦";
        public static final String AUSTRALIA = "🇦🇺";
        public static final String GERMANY = "🇩🇪";
        public static final String FRANCE = "🇫🇷";
        public static final String JAPAN = "🇯🇵";
        public static final String CHINA = "🇨🇳";
    }
    
    // Shadows
    public static class Shadows {
        public static void applyShadow(Graphics2D g2d, int x, int y, int width, int height, int radius) {
            g2d.setColor(Colors.SHADOW_LIGHT);
            g2d.fillRoundRect(x + 2, y + 2, width, height, radius, radius);
            g2d.setColor(Colors.SHADOW_DEFAULT);
            g2d.fillRoundRect(x + 1, y + 1, width, height, radius, radius);
        }
        
        public static void applyCardShadow(Graphics2D g2d, int width, int height, int radius) {
            g2d.setColor(new Color(0, 0, 0, 4));
            g2d.fillRoundRect(0, 4, width, height, radius, radius);
            g2d.setColor(new Color(0, 0, 0, 8));
            g2d.fillRoundRect(0, 2, width, height, radius, radius);
            g2d.setColor(new Color(0, 0, 0, 12));
            g2d.fillRoundRect(0, 1, width, height, radius, radius);
        }
        
        public static void applySidebarShadow(Graphics2D g2d, int width, int height) {
            g2d.setColor(new Color(0, 0, 0, 15));
            g2d.fillRect(width, 0, 3, height);
            g2d.setColor(new Color(0, 0, 0, 8));
            g2d.fillRect(width + 3, 0, 2, height);
        }
    }
    
    // Gradients
    public static class Gradients {
        public static GradientPaint createSidebarGradient(int height) {
            return new GradientPaint(0, 0, Colors.SIDEBAR_PRIMARY, 0, height, Colors.SIDEBAR_DARK);
        }
        
        public static GradientPaint createContentGradient(int height) {
            return new GradientPaint(0, 0, Colors.WHITE, 0, height, Colors.BACKGROUND_CONTENT);
        }
        
        public static GradientPaint createButtonGradient(int height, Color startColor, Color endColor) {
            return new GradientPaint(0, 0, startColor.brighter(), 0, height, endColor);
        }
        
        public static RadialGradientPaint createHoverGradient(int centerX, int centerY, int radius) {
            return new RadialGradientPaint(centerX, centerY, radius,
                new float[]{0.0f, 1.0f}, 
                new Color[]{new Color(255, 255, 255, 30), new Color(255, 255, 255, 0)});
        }
    }
}