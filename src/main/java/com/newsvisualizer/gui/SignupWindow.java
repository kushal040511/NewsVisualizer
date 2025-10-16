package com.newsvisualizer.gui;

import com.newsvisualizer.model.User;
import com.newsvisualizer.service.AuthenticationService;
import com.newsvisualizer.service.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern signup window with password strength indicator
 */
public class SignupWindow extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(SignupWindow.class);
    
    private final AuthenticationService authService;
    private final SessionManager sessionManager;
    
    // UI Components
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JButton signupButton;
    private JButton loginButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JProgressBar passwordStrengthBar;
    private JLabel passwordStrengthLabel;
    
    // Result
    private boolean registrationSuccessful = false;
    private User registeredUser = null;
    
    public SignupWindow(Frame parent) {
        super(parent, "News Visualizer - Create Account", true);
        
        this.authService = new AuthenticationService();
        this.sessionManager = SessionManager.getInstance();
        
        initializeGUI();
        setupEventHandlers();
        
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initializeGUI() {
        setSize(500, 750);
        setResizable(false);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(106, 90, 205), 0, h, new Color(72, 61, 139));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Content panel
        JScrollPane scrollPane = new JScrollPane(createContentPanel());
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));
        
        // Header
        panel.add(createHeaderPanel());
        panel.add(Box.createVerticalStrut(30));
        
        // Registration form
        panel.add(createRegistrationFormPanel());
        panel.add(Box.createVerticalStrut(25));
        
        // Buttons
        panel.add(createButtonPanel());
        panel.add(Box.createVerticalStrut(20));
        
        // Status
        panel.add(createStatusPanel());
        
        return panel;
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        // App icon
        JLabel iconLabel = new JLabel("📰");
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(iconLabel);
        
        headerPanel.add(Box.createVerticalStrut(10));
        
        // Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Join News Visualizer today");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(220, 220, 220));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(subtitleLabel);
        
        return headerPanel;
    }
    
    private JPanel createRegistrationFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        
        // Name fields (side by side)
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        namePanel.setOpaque(false);
        
        // First name
        JPanel firstNamePanel = createFieldPanel("First Name", firstNameField = new JTextField(), 170);
        namePanel.add(firstNamePanel);
        namePanel.add(Box.createHorizontalStrut(10));
        
        // Last name
        JPanel lastNamePanel = createFieldPanel("Last Name", lastNameField = new JTextField(), 170);
        namePanel.add(lastNamePanel);
        
        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Username field
        formPanel.add(createFieldPanel("Username", usernameField = new JTextField(), 350));
        formPanel.add(Box.createVerticalStrut(15));
        
        // Email field
        formPanel.add(createFieldPanel("Email Address", emailField = new JTextField(), 350));
        formPanel.add(Box.createVerticalStrut(15));
        
        // Password field with strength indicator
        formPanel.add(createPasswordFieldPanel());
        formPanel.add(Box.createVerticalStrut(15));
        
        // Confirm password field
        formPanel.add(createFieldPanel("Confirm Password", confirmPasswordField = new JPasswordField(), 350));
        
        return formPanel;
    }
    
    private JPanel createFieldPanel(String labelText, JTextField field, int width) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setOpaque(false);
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Label
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldPanel.add(label);
        
        fieldPanel.add(Box.createVerticalStrut(6));
        
        // Field
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(width, 40));
        field.setMaximumSize(new Dimension(width, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldPanel.add(field);
        
        return fieldPanel;
    }
    
    private JPanel createPasswordFieldPanel() {
        JPanel passwordPanel = new JPanel();
        passwordPanel.setOpaque(false);
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        passwordPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Label
        JLabel label = new JLabel("Password");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordPanel.add(label);
        
        passwordPanel.add(Box.createVerticalStrut(6));
        
        // Password field
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(350, 40));
        passwordField.setMaximumSize(new Dimension(350, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordPanel.add(passwordField);
        
        passwordPanel.add(Box.createVerticalStrut(8));
        
        // Password strength indicator
        passwordStrengthBar = new JProgressBar(0, 5);
        passwordStrengthBar.setPreferredSize(new Dimension(350, 8));
        passwordStrengthBar.setMaximumSize(new Dimension(350, 8));
        passwordStrengthBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordStrengthBar.setValue(0);
        passwordPanel.add(passwordStrengthBar);
        
        passwordPanel.add(Box.createVerticalStrut(4));
        
        // Password strength label
        passwordStrengthLabel = new JLabel("Enter a password");
        passwordStrengthLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passwordStrengthLabel.setForeground(new Color(220, 220, 220));
        passwordStrengthLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordPanel.add(passwordStrengthLabel);
        
        return passwordPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        // Signup button
        signupButton = createStyledButton("Create Account", new Color(52, 168, 83), Color.WHITE);
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(signupButton);
        
        buttonPanel.add(Box.createVerticalStrut(15));
        
        // Login button
        loginButton = createStyledButton("Already have an account? Sign In", 
                                       new Color(108, 117, 125), Color.WHITE);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(loginButton);
        
        return buttonPanel;
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(textColor);
        button.setPreferredSize(new Dimension(320, 45));
        button.setMaximumSize(new Dimension(320, 45));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel();
        statusPanel.setOpaque(false);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(320, 6));
        progressBar.setMaximumSize(new Dimension(320, 6));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusPanel.add(progressBar);
        
        statusPanel.add(Box.createVerticalStrut(10));
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusPanel.add(statusLabel);
        
        return statusPanel;
    }
    
    private void setupEventHandlers() {
        // Signup button
        signupButton.addActionListener(e -> performRegistration());
        
        // Login button
        loginButton.addActionListener(e -> {
            dispose();
        });
        
        // Password strength checker
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePasswordStrength();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePasswordStrength();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePasswordStrength();
            }
        });
    }
    
    private void updatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        AuthenticationService.PasswordStrength strength = authService.checkPasswordStrength(password);
        
        passwordStrengthBar.setValue(strength.getScore());
        passwordStrengthLabel.setText(strength.getLevel() + " - " + strength.getFeedback());
        
        // Update color based on strength
        Color strengthColor;
        switch (strength.getScore()) {
            case 0:
            case 1:
                strengthColor = new Color(220, 53, 69);
                break;
            case 2:
                strengthColor = new Color(255, 193, 7);
                break;
            case 3:
                strengthColor = new Color(255, 193, 7);
                break;
            case 4:
                strengthColor = new Color(40, 167, 69);
                break;
            case 5:
                strengthColor = new Color(25, 135, 84);
                break;
            default:
                strengthColor = Color.GRAY;
        }
        
        passwordStrengthBar.setForeground(strengthColor);
        passwordStrengthLabel.setForeground(strengthColor);
    }
    
    private void performRegistration() {
        // Get form data
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        
        // Basic client-side validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showStatus("Please fill in all required fields", false);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showStatus("Passwords do not match", false);
            return;
        }
        
        // Disable form during registration
        setFormEnabled(false);
        showProgress(true);
        showStatus("Creating your account...", true);
        
        // Perform registration in background thread
        SwingWorker<AuthenticationService.AuthenticationResult, Void> worker = 
            new SwingWorker<AuthenticationService.AuthenticationResult, Void>() {
            @Override
            protected AuthenticationService.AuthenticationResult doInBackground() {
                return authService.register(username, email, password, confirmPassword, firstName, lastName);
            }
            
            @Override
            protected void done() {
                try {
                    AuthenticationService.AuthenticationResult result = get();
                    handleRegistrationResult(result);
                } catch (Exception e) {
                    logger.error("Error during registration", e);
                    showStatus("Registration failed due to unexpected error", false);
                } finally {
                    setFormEnabled(true);
                    showProgress(false);
                }
            }
        };
        
        worker.execute();
    }
    
    private void handleRegistrationResult(AuthenticationService.AuthenticationResult result) {
        if (result.isSuccess()) {
            showStatus("Account created successfully! Welcome to News Visualizer.", true);
            
            // Start user session
            sessionManager.startSession(result.getUser());
            
            registrationSuccessful = true;
            registeredUser = result.getUser();
            
            // Close dialog after short delay
            Timer timer = new Timer(2000, e -> dispose());
            timer.setRepeats(false);
            timer.start();
            
        } else {
            showStatus(result.getMessage(), false);
        }
    }
    
    private void setFormEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        confirmPasswordField.setEnabled(enabled);
        firstNameField.setEnabled(enabled);
        lastNameField.setEnabled(enabled);
        signupButton.setEnabled(enabled);
        loginButton.setEnabled(enabled);
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisible(show);
        progressBar.setIndeterminate(show);
    }
    
    private void showStatus(String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.setForeground(isSuccess ? new Color(144, 238, 144) : new Color(255, 182, 193));
    }
    
    // Public methods
    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }
    
    public User getRegisteredUser() {
        return registeredUser;
    }
}