package com.newsvisualizer;

import com.newsvisualizer.gui.EnhancedMainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Enhanced entry point for the News Visualizer application with modern UI
 */
public class EnhancedApp {
    private static final Logger logger = LoggerFactory.getLogger(EnhancedApp.class);
    
    public static void main(String[] args) {
        logger.info("Starting Enhanced News Visualizer Application...");
        
        // Set system look and feel
        setLookAndFeel();
        
        // Create and show the enhanced main window
        SwingUtilities.invokeLater(() -> {
            try {
                new EnhancedMainWindow().setVisible(true);
                logger.info("Enhanced News Visualizer Application started successfully");
            } catch (Exception e) {
                logger.error("Failed to start Enhanced News Visualizer Application", e);
                showErrorDialog("Failed to start application: " + e.getMessage());
                System.exit(1);
            }
        });
    }
    
    /**
     * Set the system look and feel
     */
    private static void setLookAndFeel() {
        try {
            // Try to use system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            logger.info("Using system look and feel: {}", UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Could not set system look and feel, using default", e);
            try {
                // Fallback to cross-platform look and feel
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e2) {
                logger.warn("Could not set cross-platform look and feel either", e2);
            }
        }
        
        // Set some UI properties for better appearance
        UIManager.put("Table.gridColor", Color.LIGHT_GRAY);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", true);
        UIManager.put("TabbedPane.tabInsets", new Insets(4, 8, 4, 8));
    }
    
    /**
     * Show error dialog
     */
    private static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
            null,
            message,
            "Enhanced News Visualizer - Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}