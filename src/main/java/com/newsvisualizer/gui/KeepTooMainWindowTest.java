package com.newsvisualizer.gui;

import javax.swing.*;

/**
 * Test launcher for the new KeepToo-styled main window
 */
public class KeepTooMainWindowTest {
    
    public static void main(String[] args) {
        // Set system look and feel properties
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "NewsVisualizer");
        
        // Enable antialiasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Set look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Launch the new KeepToo main window
                new KeepTooMainWindow();
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error launching KeepToo Main Window: " + e.getMessage(),
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}