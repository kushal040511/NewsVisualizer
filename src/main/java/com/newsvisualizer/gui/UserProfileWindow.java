package com.newsvisualizer.gui;

import com.newsvisualizer.model.User;
import com.newsvisualizer.service.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Simple user profile window showing account information
 */
public class UserProfileWindow extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileWindow.class);
    
    private final SessionManager sessionManager;
    private final User currentUser;
    
    public UserProfileWindow(Frame parent) {
        super(parent, "User Profile", true);
        
        this.sessionManager = SessionManager.getInstance();
        this.currentUser = sessionManager.getCurrentUser();
        
        if (currentUser == null) {
            dispose();
            return;
        }
        
        initializeGUI();
        setLocationRelativeTo(parent);
    }
    
    private void initializeGUI() {
        setSize(500, 400);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel iconLabel = new JLabel("👤", JLabel.CENTER);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(iconLabel);
        
        JLabel nameLabel = new JLabel(currentUser.getFullName(), JLabel.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(nameLabel);
        
        JLabel emailLabel = new JLabel(currentUser.getEmail(), JLabel.CENTER);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        emailLabel.setForeground(new Color(220, 220, 220));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(emailLabel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        contentPanel.add(new JLabel("Username:"));
        contentPanel.add(new JLabel(currentUser.getUsername()));
        
        contentPanel.add(new JLabel("Email:"));
        contentPanel.add(new JLabel(currentUser.getEmail()));
        
        contentPanel.add(new JLabel("Member Since:"));
        String joinDate = currentUser.getCreatedAt() != null ?
            currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown";
        contentPanel.add(new JLabel(joinDate));
        
        contentPanel.add(new JLabel("Status:"));
        JLabel statusLabel = new JLabel(currentUser.isActive() ? "Active" : "Inactive");
        statusLabel.setForeground(currentUser.isActive() ? new Color(40, 167, 69) : Color.RED);
        contentPanel.add(statusLabel);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
}
