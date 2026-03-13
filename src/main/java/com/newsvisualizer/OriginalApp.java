package com.newsvisualizer;

import com.newsvisualizer.gui.MainWindow;

import javax.swing.*;
import java.awt.*;

/**
 * Simple launcher for the original MainWindow
 */
public class OriginalApp {
    
    public static void main(String[] args) {
        System.out.println("Starting Original NewsVisualizer Application...");
        
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        
        // Create and show the original main window
        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
                System.out.println("Original NewsVisualizer Application started successfully");
            } catch (Exception e) {
                System.err.println("Failed to start application: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}