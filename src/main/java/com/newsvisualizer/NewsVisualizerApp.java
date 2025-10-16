package com.newsvisualizer;

import com.newsvisualizer.gui.MainWindow;
import com.newsvisualizer.gui.LoginWindowNew;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the News Visualizer application
 */
public class NewsVisualizerApp {
    private static final Logger logger = LoggerFactory.getLogger(NewsVisualizerApp.class);
    
    public static void main(String[] args) {
        logger.info("Starting News Visualizer Application...");
        
        // Set system look and feel
        setLookAndFeel();
        
        // Create and show the login window
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginWindowNew();
                logger.info("News Visualizer Application started successfully");
            } catch (Exception e) {
                logger.error("Failed to start News Visualizer Application", e);
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
            "News Visualizer - Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
}