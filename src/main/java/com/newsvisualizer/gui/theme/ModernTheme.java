package com.newsvisualizer.gui.theme;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

/**
 * Modern theme constants and utilities for the News Visualizer application
 */
public class ModernTheme {
    
    // Primary Color Palette - Professional and modern
    public static class Colors {
        // Primary Colors
        public static final Color PRIMARY = new Color(59, 130, 246);        // Blue 500
        public static final Color PRIMARY_LIGHT = new Color(147, 197, 253); // Blue 300
        public static final Color PRIMARY_DARK = new Color(29, 78, 216);    // Blue 700
        
        // Secondary Colors
        public static final Color SECONDARY = new Color(16, 185, 129);      // Emerald 500
        public static final Color SECONDARY_LIGHT = new Color(110, 231, 183); // Emerald 300
        public static final Color SECONDARY_DARK = new Color(5, 150, 105);  // Emerald 600
        
        // Accent Colors
        public static final Color ACCENT = new Color(147, 51, 234);         // Purple 600
        public static final Color ACCENT_LIGHT = new Color(196, 181, 253);  // Purple 300
        public static final Color WARNING = new Color(245, 158, 11);        // Amber 500
        public static final Color ERROR = new Color(239, 68, 68);           // Red 500
        
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
        
        // Text Colors
        public static final Color TEXT_PRIMARY = GRAY_900;
        public static final Color TEXT_SECONDARY = GRAY_600;
        public static final Color TEXT_MUTED = GRAY_500;
        
        // Border Colors
        public static final Color BORDER_LIGHT = GRAY_200;
        public static final Color BORDER_DEFAULT = GRAY_300;
        public static final Color BORDER_STRONG = GRAY_400;
        
        // Shadow Colors
        public static final Color SHADOW_LIGHT = new Color(0, 0, 0, 8);
        public static final Color SHADOW_DEFAULT = new Color(0, 0, 0, 12);
        public static final Color SHADOW_STRONG = new Color(0, 0, 0, 20);
    }
    
    // Typography
    public static class Fonts {
        // System fonts - fallback to system defaults if SF Pro not available
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
        
        // Common font definitions
        public static final Font TITLE_LARGE = getDisplayFont(Font.BOLD, 32);
        public static final Font TITLE_MEDIUM = getDisplayFont(Font.BOLD, 24);
        public static final Font TITLE_SMALL = getDisplayFont(Font.BOLD, 20);
        public static final Font HEADING_LARGE = getDisplayFont(Font.BOLD, 18);
        public static final Font HEADING_MEDIUM = getDisplayFont(Font.BOLD, 16);
        public static final Font HEADING_SMALL = getDisplayFont(Font.BOLD, 14);
        public static final Font BODY_LARGE = getTextFont(Font.PLAIN, 16);
        public static final Font BODY_MEDIUM = getTextFont(Font.PLAIN, 14);
        public static final Font BODY_SMALL = getTextFont(Font.PLAIN, 12);
        public static final Font CAPTION = getTextFont(Font.PLAIN, 11);
        public static final Font BUTTON = getTextFont(Font.BOLD, 14);
        public static final Font LABEL = getTextFont(Font.BOLD, 13);
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
    }
    
    // Shadows
    public static class Shadows {
        public static void applyShadow(Graphics2D g2d, int x, int y, int width, int height, int radius) {
            // Drop shadow effect
            g2d.setColor(Colors.SHADOW_LIGHT);
            g2d.fillRoundRect(x + 2, y + 2, width, height, radius, radius);
            g2d.setColor(Colors.SHADOW_DEFAULT);
            g2d.fillRoundRect(x + 1, y + 1, width, height, radius, radius);
        }
        
        public static void applyCardShadow(Graphics2D g2d, int width, int height, int radius) {
            // Subtle card shadow
            g2d.setColor(new Color(0, 0, 0, 4));
            g2d.fillRoundRect(0, 4, width, height, radius, radius);
            g2d.setColor(new Color(0, 0, 0, 8));
            g2d.fillRoundRect(0, 2, width, height, radius, radius);
            g2d.setColor(new Color(0, 0, 0, 12));
            g2d.fillRoundRect(0, 1, width, height, radius, radius);
        }
    }
    
    // Custom Border Utilities
    public static class Borders {
        
        public static class RoundedBorder extends AbstractBorder {
            private final Color color;
            private final int thickness;
            private final int radius;
            
            public RoundedBorder(Color color, int thickness, int radius) {
                this.color = color;
                this.thickness = thickness;
                this.radius = radius;
            }
            
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(thickness));
                g2d.drawRoundRect(x + thickness/2, y + thickness/2, 
                                width - thickness, height - thickness, radius, radius);
                g2d.dispose();
            }
            
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(thickness + 2, thickness + 2, thickness + 2, thickness + 2);
            }
        }
        
        public static RoundedBorder createRoundedBorder(Color color, int thickness, int radius) {
            return new RoundedBorder(color, thickness, radius);
        }
        
        public static RoundedBorder createDefaultBorder() {
            return new RoundedBorder(Colors.BORDER_LIGHT, 1, Radius.MEDIUM);
        }
    }
    
    // Gradient Utilities
    public static class Gradients {
        
        public static GradientPaint createVerticalGradient(int height, Color startColor, Color endColor) {
            return new GradientPaint(0, 0, startColor, 0, height, endColor);
        }
        
        public static GradientPaint createHorizontalGradient(int width, Color startColor, Color endColor) {
            return new GradientPaint(0, 0, startColor, width, 0, endColor);
        }
        
        public static RadialGradientPaint createRadialGradient(int centerX, int centerY, int radius, 
                                                              Color centerColor, Color edgeColor) {
            return new RadialGradientPaint(centerX, centerY, radius, 
                new float[]{0.0f, 1.0f}, new Color[]{centerColor, edgeColor});
        }
        
        // Predefined gradients
        public static GradientPaint PRIMARY_GRADIENT(int height) {
            return createVerticalGradient(height, Colors.PRIMARY_LIGHT, Colors.PRIMARY);
        }
        
        public static GradientPaint SECONDARY_GRADIENT(int height) {
            return createVerticalGradient(height, Colors.SECONDARY_LIGHT, Colors.SECONDARY);
        }
        
        public static GradientPaint BACKGROUND_GRADIENT(int height) {
            return createVerticalGradient(height, Colors.WHITE, Colors.GRAY_50);
        }
    }
    
    // Animation Utilities
    public static class Animation {
        public static Timer createFadeTimer(JComponent component, float startOpacity, float endOpacity, int duration) {
            return new Timer(16, new ActionListener() {
                private long startTime = System.currentTimeMillis();
                private float currentOpacity = startOpacity;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    float progress = Math.min(1.0f, (float) elapsed / duration);
                    
                    currentOpacity = startOpacity + (endOpacity - startOpacity) * progress;
                    
                    component.repaint();
                    
                    if (progress >= 1.0f) {
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
        }
    }
}