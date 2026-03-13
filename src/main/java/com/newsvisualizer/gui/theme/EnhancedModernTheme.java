package com.newsvisualizer.gui.theme;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

/**
 * Shared SaaS-style design system for NewsVisualizer.
 */
public class EnhancedModernTheme {
    
    // Enhanced Color Palette
    public static class Colors {
        public static final Color SIDEBAR_PRIMARY = new Color(15, 23, 42);
        public static final Color SIDEBAR_SECONDARY = new Color(30, 41, 59);
        public static final Color SIDEBAR_DARK = new Color(2, 6, 23);
        public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);
        public static final Color SIDEBAR_SELECTED = new Color(79, 70, 229);

        public static final Color PRIMARY = new Color(79, 70, 229);
        public static final Color PRIMARY_LIGHT = new Color(224, 231, 255);
        public static final Color PRIMARY_DARK = new Color(67, 56, 202);

        public static final Color SECONDARY = new Color(99, 102, 241);
        public static final Color SECONDARY_LIGHT = new Color(199, 210, 254);
        public static final Color SUCCESS = new Color(16, 185, 129);

        public static final Color ACCENT = new Color(14, 165, 233);
        public static final Color WARNING = new Color(245, 158, 11);
        public static final Color ERROR = new Color(239, 68, 68);
        public static final Color INFO = new Color(59, 130, 246);
        
        // Neutral Colors
        public static final Color WHITE = new Color(255, 255, 255);
        public static final Color GRAY_50 = new Color(248, 250, 252);
        public static final Color GRAY_100 = new Color(241, 245, 249);
        public static final Color GRAY_200 = new Color(229, 231, 235);
        public static final Color GRAY_300 = new Color(209, 213, 219);
        public static final Color GRAY_400 = new Color(156, 163, 175);
        public static final Color GRAY_500 = new Color(107, 114, 128);
        public static final Color GRAY_600 = new Color(75, 85, 99);
        public static final Color GRAY_700 = new Color(55, 65, 81);
        public static final Color GRAY_800 = new Color(31, 41, 55);
        public static final Color GRAY_900 = new Color(17, 24, 39);
        
        public static final Color BACKGROUND_PRIMARY = new Color(248, 250, 252);
        public static final Color BACKGROUND_SECONDARY = WHITE;
        public static final Color BACKGROUND_CARD = WHITE;
        public static final Color BACKGROUND_CONTENT = new Color(248, 250, 252);
        
        public static final Color TEXT_PRIMARY = GRAY_900;
        public static final Color TEXT_SECONDARY = GRAY_600;
        public static final Color TEXT_MUTED = GRAY_500;
        public static final Color TEXT_WHITE = WHITE;
        public static final Color TEXT_SIDEBAR = new Color(226, 232, 240);
        public static final Color TEXT_SIDEBAR_SELECTED = WHITE;
        
        public static final Color BORDER_LIGHT = GRAY_200;
        public static final Color BORDER_DEFAULT = GRAY_300;
        public static final Color BORDER_STRONG = GRAY_400;
        public static final Color BORDER_SIDEBAR = new Color(148, 163, 184, 48);
        
        public static final Color SHADOW_LIGHT = new Color(15, 23, 42, 10);
        public static final Color SHADOW_DEFAULT = new Color(15, 23, 42, 18);
        public static final Color SHADOW_STRONG = new Color(15, 23, 42, 28);
    }
    
    // Typography
    public static class Fonts {
        // System fonts with improved hierarchy
        private static final String[] PREFERRED_FONTS = {
            "Inter", "Poppins", "SF Pro Display", "Segoe UI", "Roboto", "Helvetica Neue", "Arial", "sans-serif"
        };
        private static final String[] PREFERRED_TEXT_FONTS = {
            "Inter", "Poppins", "SF Pro Text", "Segoe UI", "Roboto", "Helvetica Neue", "Arial", "sans-serif"
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
        public static final Font APP_TITLE = getDisplayFont(Font.BOLD, 28);
        public static final Font SECTION_TITLE = getDisplayFont(Font.BOLD, 24);
        public static final Font CARD_TITLE = getDisplayFont(Font.BOLD, 22);
        public static final Font HEADING_LARGE = getDisplayFont(Font.BOLD, 20);
        public static final Font HEADING_MEDIUM = getDisplayFont(Font.BOLD, 16);
        public static final Font HEADING_SMALL = getDisplayFont(Font.BOLD, 14);
        public static final Font SIDEBAR_TITLE = getDisplayFont(Font.BOLD, 16);
        public static final Font BODY_LARGE = getTextFont(Font.PLAIN, 16);
        public static final Font BODY_MEDIUM = getTextFont(Font.PLAIN, 14);
        public static final Font BODY_SMALL = getTextFont(Font.PLAIN, 12);
        public static final Font BUTTON = getTextFont(Font.BOLD, 13);
        public static final Font LABEL = getTextFont(Font.BOLD, 13);
        public static final Font SIDEBAR_ITEM = getTextFont(Font.BOLD, 14);
        public static final Font CAPTION = getTextFont(Font.PLAIN, 11);
    }
    
    // Layout Constants
    public static class Layout {
        // Responsive sidebar width based on screen size
        public static int getSidebarWidth() {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width;
            
            if (screenWidth <= 1366) {        // Small laptops
                return 220;
            } else if (screenWidth <= 1920) {  // Standard laptops/desktops
                return 264;
            } else {                          // Large screens
                return 288;
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
        public static final int CARD_PADDING = 22;
        public static final int SIDEBAR_ITEM_HEIGHT = 52;
        public static final int BUTTON_HEIGHT = 42;
        public static final int INPUT_HEIGHT = 44;
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
        public static final int SMALL = 8;
        public static final int MEDIUM = 12;
        public static final int LARGE = 16;
        public static final int XLARGE = 20;
        public static final int CARD = 18;
        public static final int BUTTON = 12;
    }
    
    // Professional Icons - Comprehensive Set
    public static class Icons {
        public static final String DASHBOARD = "DA";
        public static final String NEWS = "NW";
        public static final String ANALYTICS = "AN";
        public static final String CHARTS = "PI";
        public static final String FILTER = "KW";
        public static final String AI = "AI";
        public static final String TRANSLATE = "TR";
        public static final String HISTORY = "HI";
        public static final String SETTINGS = "ST";
        public static final String NEWSAPP = "GL";

        public static final String USER = "US";
        public static final String PROFILE = "PF";
        public static final String LOGIN = "LG";
        public static final String LOGOUT = "LO";

        public static final String SEARCH = "SR";
        public static final String REFRESH = "RF";
        public static final String DOWNLOAD = "DL";
        public static final String EXPORT = "EX";
        public static final String DELETE = "RM";
        public static final String EDIT = "ED";
        public static final String CLEAR = "CL";
        public static final String COPY = "CP";
        public static final String SAVE = "SV";
        public static final String PRINT = "PR";
        public static final String SHARE = "SH";
        public static final String VIEW = "VW";
        public static final String CLOSE = "X";
        public static final String MENU = "≡";

        public static final String ARTICLE = "AR";
        public static final String IMAGE = "IM";
        public static final String VIDEO = "VD";
        public static final String LINK = "LN";
        public static final String BOOKMARK = "BM";
        public static final String TAG = "TG";
        public static final String CATEGORY = "CT";
        public static final String SOURCE = "SC";

        public static final String SUCCESS = "OK";
        public static final String ERROR = "ER";
        public static final String WARNING = "!";
        public static final String INFO = "i";
        public static final String LOADING = "...";
        public static final String PROCESSING = "PR";
        public static final String COMPLETED = "OK";

        public static final String ARROW_RIGHT = "›";
        public static final String ARROW_LEFT = "‹";
        public static final String ARROW_DOWN = "⌄";
        public static final String ARROW_UP = "⌃";

        public static final String DATABASE = "DB";
        public static final String CLOUD = "CL";
        public static final String SERVER = "SV";
        public static final String API = "AP";

        public static final String INDIA = "IN";
        public static final String USA = "US";
        public static final String UK = "UK";
        public static final String GLOBAL = "GL";
        public static final String CANADA = "CA";
        public static final String AUSTRALIA = "AU";
        public static final String GERMANY = "DE";
        public static final String FRANCE = "FR";
        public static final String JAPAN = "JP";
        public static final String CHINA = "CN";
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
            g2d.setColor(new Color(15, 23, 42, 8));
            g2d.fillRoundRect(0, 8, width, height - 2, radius, radius);
            g2d.setColor(new Color(15, 23, 42, 12));
            g2d.fillRoundRect(0, 4, width, height - 1, radius, radius);
        }
        
        public static void applySidebarShadow(Graphics2D g2d, int width, int height) {
            g2d.setColor(new Color(15, 23, 42, 18));
            g2d.fillRect(width, 0, 3, height);
            g2d.setColor(new Color(15, 23, 42, 10));
            g2d.fillRect(width + 3, 0, 2, height);
        }
    }
    
    // Gradients
    public static class Gradients {
        public static GradientPaint createSidebarGradient(int height) {
            return new GradientPaint(0, 0, Colors.SIDEBAR_PRIMARY, 0, height, Colors.SIDEBAR_SECONDARY);
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
