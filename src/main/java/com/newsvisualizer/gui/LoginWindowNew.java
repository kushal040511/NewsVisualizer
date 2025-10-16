package com.newsvisualizer.gui;

import com.newsvisualizer.service.AuthenticationService;
import com.newsvisualizer.service.SessionManager;
import com.newsvisualizer.gui.MainWindow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;

public class LoginWindowNew extends JFrame {
    private static final Logger logger = Logger.getLogger(LoginWindowNew.class.getName());
    
    // Services
    private final AuthenticationService authService;
    private final SessionManager sessionManager;
    
    // UI Components
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JButton signUpButton;
    private JButton signInButton;
    private JPanel rightPanel;
    private boolean isSignUpMode = true;
    
    public LoginWindowNew() {
        // Initialize services
        this.authService = new AuthenticationService();
        this.sessionManager = SessionManager.getInstance();
        
        initializeWindow();
        createUI();
        setupEventHandlers();
        
        setVisible(true);
    }
    
    private void initializeWindow() {
        setTitle("News Visualizer - Welcome");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    // Animation variables for galaxy theme
    private Timer galaxyAnimationTimer;
    private float animationPhase = 0;
    private java.util.Random random = new java.util.Random();
    private Star[] stars;
    private int numStars = 150;
    
    // Star class for animated starfield
    private static class Star {
        float x, y;
        float size;
        float brightness;
        float twinklePhase;
        Color color;
        
        Star(float x, float y, float size, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.brightness = 0.3f + (float)Math.random() * 0.7f;
            this.twinklePhase = (float)(Math.random() * Math.PI * 2);
        }
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        // Initialize stars for animation
        initializeStars();
        
        // Create main panel with galaxy background
        JPanel galaxyPanel = new JPanel(new GridLayout(1, 2, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintGalaxyBackground(g);
            }
        };
        galaxyPanel.setOpaque(true);
        
        // Left panel - Welcome section with galaxy theme
        JPanel leftPanel = createLeftPanel();
        
        // Right panel - Form section
        rightPanel = createRightPanel();
        
        galaxyPanel.add(leftPanel);
        galaxyPanel.add(rightPanel);
        
        add(galaxyPanel, BorderLayout.CENTER);
        
        // Start galaxy animation
        startGalaxyAnimation();
    }
    
    // Initialize the animated starfield
    private void initializeStars() {
        stars = new Star[numStars];
        
        // Create stars with different colors and sizes
        Color[] starColors = {
            new Color(255, 255, 255),  // White
            new Color(255, 255, 200),  // Warm white
            new Color(200, 220, 255),  // Cool blue-white
            new Color(255, 200, 200),  // Warm red-white
            new Color(200, 255, 255),  // Cool cyan-white
            new Color(255, 220, 150)   // Warm yellow-white
        };
        
        for (int i = 0; i < numStars; i++) {
            float x = random.nextFloat();
            float y = random.nextFloat();
            float size = 0.5f + random.nextFloat() * 2.5f;
            Color color = starColors[random.nextInt(starColors.length)];
            stars[i] = new Star(x, y, size, color);
        }
    }
    
    // Paint the galaxy background
    private void paintGalaxyBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        // Deep space background - dark gradient
        GradientPaint spaceGradient = new GradientPaint(
            0, 0, new Color(5, 5, 15),      // Very dark blue-black
            width, height, new Color(15, 10, 25)  // Dark purple-black
        );
        g2d.setPaint(spaceGradient);
        g2d.fillRect(0, 0, width, height);
        
        // Add multiple nebula layers for depth
        paintNebulaLayer(g2d, width, height, 0.6f, new Color(60, 20, 100, 40));  // Purple nebula
        paintNebulaLayer(g2d, width, height, 0.8f, new Color(20, 60, 120, 30));  // Blue nebula
        paintNebulaLayer(g2d, width, height, 1.0f, new Color(80, 30, 60, 25));   // Pink nebula
        paintNebulaLayer(g2d, width, height, 0.4f, new Color(40, 80, 40, 20));   // Green nebula
        
        // Paint animated stars
        paintStars(g2d, width, height);
        
        // Add subtle scanning effect
        paintScanningEffect(g2d, width, height);
        
        g2d.dispose();
    }
    
    // Paint a nebula layer with flowing animation
    private void paintNebulaLayer(Graphics2D g2d, int width, int height, float phaseOffset, Color baseColor) {
        float phase = animationPhase * phaseOffset;
        
        // Create multiple nebula clouds
        for (int i = 0; i < 6; i++) {
            float cloudPhase = phase + i * 1.2f;
            float centerX = width * (0.2f + 0.6f * (float)Math.sin(cloudPhase * 0.3f));
            float centerY = height * (0.3f + 0.4f * (float)Math.cos(cloudPhase * 0.2f));
            float radius = Math.min(width, height) * (0.3f + 0.2f * (float)Math.sin(cloudPhase * 0.5f));
            
            // Create radial gradient for nebula cloud
            float[] fractions = {0.0f, 0.3f, 0.7f, 1.0f};
            Color[] colors = {
                new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), baseColor.getAlpha()),
                new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), baseColor.getAlpha() / 2),
                new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), baseColor.getAlpha() / 4),
                new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0)
            };
            
            RadialGradientPaint nebulaGradient = new RadialGradientPaint(
                centerX, centerY, radius, fractions, colors
            );
            
            g2d.setPaint(nebulaGradient);
            g2d.fillOval((int)(centerX - radius), (int)(centerY - radius), 
                        (int)(radius * 2), (int)(radius * 2));
        }
    }
    
    // Paint animated twinkling stars
    private void paintStars(Graphics2D g2d, int width, int height) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        
        for (Star star : stars) {
            // Calculate star position
            float x = star.x * width;
            float y = star.y * height;
            
            // Animate twinkling effect
            star.twinklePhase += 0.05f + random.nextFloat() * 0.02f;
            float twinkle = 0.5f + 0.5f * (float)Math.sin(star.twinklePhase);
            float alpha = star.brightness * twinkle;
            
            // Create star glow effect
            Color glowColor = new Color(
                star.color.getRed(),
                star.color.getGreen(), 
                star.color.getBlue(),
                (int)(alpha * 255)
            );
            
            g2d.setColor(glowColor);
            
            // Draw star with glow
            float starSize = star.size * (0.8f + 0.4f * twinkle);
            
            // Outer glow
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.3f));
            g2d.fillOval((int)(x - starSize), (int)(y - starSize), 
                        (int)(starSize * 2), (int)(starSize * 2));
            
            // Inner bright core
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.fillOval((int)(x - starSize/2), (int)(y - starSize/2), 
                        (int)starSize, (int)starSize);
        }
    }
    
    // Paint subtle scanning effect across the galaxy
    private void paintScanningEffect(Graphics2D g2d, int width, int height) {
        float scanY = height * (0.5f + 0.4f * (float)Math.sin(animationPhase * 0.3f));
        
        // Create a simple two-point gradient for the scanning effect
        GradientPaint scanGradient = new GradientPaint(
            0, scanY - 30, new Color(100, 150, 255, 0),
            0, scanY + 30, new Color(100, 150, 255, 15)
        );
        
        g2d.setPaint(scanGradient);
        g2d.fillRect(0, (int)(scanY - 30), width, 60);
        
        // Add a second gradient for the fade effect
        GradientPaint fadeGradient = new GradientPaint(
            0, scanY, new Color(100, 150, 255, 15),
            0, scanY + 30, new Color(100, 150, 255, 0)
        );
        
        g2d.setPaint(fadeGradient);
        g2d.fillRect(0, (int)scanY, width, 30);
    }
    
    // Start the galaxy animation timer
    private void startGalaxyAnimation() {
        galaxyAnimationTimer = new Timer(50, e -> {
            animationPhase += 0.02f;
            if (animationPhase >= Math.PI * 2) {
                animationPhase = 0;
            }
            repaint();
        });
        galaxyAnimationTimer.start();
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent(g) to make background transparent
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent dark overlay to ensure text readability over galaxy background
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                g2d.setColor(new Color(20, 10, 30));  // Dark purple overlay
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add a subtle cosmic glow border
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                GradientPaint glowGradient = new GradientPaint(
                    0, 0, new Color(80, 60, 120, 100),
                    getWidth(), getHeight(), new Color(60, 40, 100, 50)
                );
                g2d.setPaint(glowGradient);
                g2d.setStroke(new BasicStroke(3.0f));
                g2d.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
                
                // Add subtle inner glow
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                g2d.setColor(new Color(150, 100, 200));
                g2d.fillRect(0, 0, getWidth(), 30);  // Top glow
                g2d.fillRect(0, getHeight() - 30, getWidth(), 30);  // Bottom glow
                
                g2d.dispose();
            }
        };
        panel.setOpaque(false);  // Make panel transparent to show galaxy background
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(80, 40, 80, 40));
        
        // Add vertical glue to center content
        panel.add(Box.createVerticalGlue());
        
        // Welcome title with cosmic glow effect
        JLabel welcomeTitle = new JLabel("Welcome Back!") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                String text = getText();
                Font font = getFont();
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                
                // Outer cosmic glow
                for (int i = 8; i > 0; i--) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                    g2d.setColor(new Color(150, 100, 255)); // Purple glow
                    g2d.drawString(text, x - i/2, y - i/2);
                    g2d.drawString(text, x + i/2, y + i/2);
                }
                
                // Main text
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(220, 220, 255)); // Light blue-white
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        welcomeTitle.setFont(new Font("Arial", Font.BOLD, 36));
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(welcomeTitle);
        
        panel.add(Box.createVerticalStrut(20));
        
        // Subtitle with enhanced styling for galaxy theme
        JLabel subtitle = new JLabel("<html><div style='text-align: center; color: #E0E0FF;'>" +
                "To keep connected with us please<br>login with your personal info</div></html>");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitle);
        
        panel.add(Box.createVerticalStrut(40));
        
        // SIGN IN button with cosmic styling
        signInButton = new JButton("SIGN IN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Cosmic glow border effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.setColor(new Color(150, 100, 255)); // Purple glow
                g2d.setStroke(new BasicStroke(4.0f));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);
                
                // Main border
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(200, 180, 255)); // Light purple
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 25, 25);
                
                // Subtle inner glow
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                GradientPaint innerGlow = new GradientPaint(
                    0, 0, new Color(200, 150, 255),
                    0, getHeight(), new Color(100, 50, 200)
                );
                g2d.setPaint(innerGlow);
                g2d.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 20, 20);
                
                // Enhanced text with glow
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Text glow effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setColor(new Color(150, 100, 255));
                for (int i = 3; i > 0; i--) {
                    g2d.drawString(text, x - i, y - i);
                    g2d.drawString(text, x + i, y + i);
                }
                
                // Main text
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(220, 220, 255)); // Light blue-white
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        
        signInButton.setFont(new Font("Arial", Font.BOLD, 14));
        signInButton.setPreferredSize(new Dimension(200, 45));
        signInButton.setMaximumSize(new Dimension(200, 45));
        signInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInButton.setContentAreaFilled(false);
        signInButton.setBorderPainted(false);
        signInButton.setFocusPainted(false);
        signInButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(signInButton);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent(g) to make background transparent
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent overlay to ensure form readability over galaxy background
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                
                // Subtle gradient overlay
                GradientPaint overlayGradient = new GradientPaint(
                    0, 0, new Color(245, 248, 255, 240),
                    0, getHeight(), new Color(235, 240, 250, 220)
                );
                g2d.setPaint(overlayGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle cosmic border glow
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setColor(new Color(100, 150, 255));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                
                g2d.dispose();
            }
        };
        panel.setOpaque(false);  // Make panel transparent to show galaxy background
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(80, 60, 80, 60));
        
        // Set rightPanel reference first
        rightPanel = panel;
        
        // Now update it
        updateRightPanel();
        
        return panel;
    }
    
    private void updateRightPanel() {
        rightPanel.removeAll();
        
        if (isSignUpMode) {
            createSignUpForm();
        } else {
            createSignInForm();
        }
        
        rightPanel.revalidate();
        rightPanel.repaint();
    }
    
    private void createSignUpForm() {
        // Title with cosmic glow effect
        JLabel title = new JLabel("Create Account") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                String text = getText();
                Font font = getFont();
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                
                // Cosmic glow effect
                for (int i = 6; i > 0; i--) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                    g2d.setColor(new Color(100, 150, 255)); // Blue glow
                    g2d.drawString(text, x - i/2, y - i/2);
                    g2d.drawString(text, x + i/2, y + i/2);
                }
                
                // Main text
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(60, 80, 120)); // Dark blue-gray
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(title);
        
        rightPanel.add(Box.createVerticalStrut(40));
        
        // Name field
        nameField = createTextField("Name");
        rightPanel.add(nameField);
        rightPanel.add(Box.createVerticalStrut(20));
        
        // Email field
        emailField = createTextField("Email");
        rightPanel.add(emailField);
        rightPanel.add(Box.createVerticalStrut(20));
        
        // Password field
        passwordField = createPasswordField("Password");
        rightPanel.add(passwordField);
        rightPanel.add(Box.createVerticalStrut(30));
        
        // SIGN UP button
        signUpButton = createGreenButton("SIGN UP");
        signUpButton.addActionListener(e -> performSignUp());
        rightPanel.add(signUpButton);
    }
    
    private void createSignInForm() {
        // Title with cosmic glow effect
        JLabel title = new JLabel("Sign In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                String text = getText();
                Font font = getFont();
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                
                // Cosmic glow effect
                for (int i = 6; i > 0; i--) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                    g2d.setColor(new Color(100, 150, 255)); // Blue glow
                    g2d.drawString(text, x - i/2, y - i/2);
                    g2d.drawString(text, x + i/2, y + i/2);
                }
                
                // Main text
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(60, 80, 120)); // Dark blue-gray
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(title);
        
        rightPanel.add(Box.createVerticalStrut(40));
        
        // Email field
        loginEmailField = createTextField("Email");
        rightPanel.add(loginEmailField);
        rightPanel.add(Box.createVerticalStrut(20));
        
        // Password field
        loginPasswordField = createPasswordField("Password");
        rightPanel.add(loginPasswordField);
        rightPanel.add(Box.createVerticalStrut(30));
        
        // SIGN IN button (green version)
        JButton loginBtn = createGreenButton("SIGN IN");
        loginBtn.addActionListener(e -> performLogin());
        rightPanel.add(loginBtn);
    }
    
    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setBorder(new EmptyBorder(15, 20, 15, 20));
        field.setBackground(new Color(229, 234, 238));
        field.setPreferredSize(new Dimension(300, 50));
        field.setMaximumSize(new Dimension(300, 50));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add placeholder functionality
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        
        return field;
    }
    
    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setBorder(new EmptyBorder(15, 20, 15, 20));
        field.setBackground(new Color(229, 234, 238));
        field.setPreferredSize(new Dimension(300, 50));
        field.setMaximumSize(new Dimension(300, 50));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add placeholder functionality
        field.setEchoChar((char) 0); // Show text initially
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('●'); // Hide password
                    field.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0); // Show placeholder
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        
        return field;
    }
    
    private JButton createGreenButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Green background
                g2d.setColor(new Color(76, 175, 80));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // White text
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String buttonText = getText();
                int x = (getWidth() - fm.stringWidth(buttonText)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.setColor(Color.WHITE);
                g2d.drawString(buttonText, x, y);
                
                g2d.dispose();
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(250, 45));
        button.setMaximumSize(new Dimension(250, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void setupEventHandlers() {
        // Left panel SIGN IN button switches to login mode
        signInButton.addActionListener(e -> {
            isSignUpMode = false;
            updateRightPanel();
        });
    }
    
    private void performSignUp() {
        String name = getFieldValue(nameField, "Name");
        String email = getFieldValue(emailField, "Email");
        String password = getFieldValue(passwordField, "Password");
        
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        if (!email.contains("@")) {
            showError("Please enter a valid email address");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return;
        }
        
        try {
            // Create user account using register method
            var result = authService.register(name, email, password, password, name, "");
            
            if (result.isSuccess()) {
                showSuccess("Account created successfully!");
                // Switch to login mode
                isSignUpMode = false;
                updateRightPanel();
            } else {
                showError(result.getMessage());
            }
        } catch (Exception e) {
            showError("Error creating account: " + e.getMessage());
            logger.severe("Signup error: " + e.getMessage());
        }
    }
    
    private void performLogin() {
        String email = getFieldValue(loginEmailField, "Email");
        String password = getFieldValue(loginPasswordField, "Password");
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }
        
        try {
            var result = authService.login(email, password);
            
            if (result.isSuccess()) {
                showSuccess("Login successful!");
                
                // Set the user session
                sessionManager.startSession(result.getUser());
                
                // Close login window and open main application
                SwingUtilities.invokeLater(() -> {
                    setVisible(false);
                    dispose();
                    // Open the main News Visualizer application
                    try {
                        com.newsvisualizer.gui.MainWindow mainWindow = new com.newsvisualizer.gui.MainWindow();
                        mainWindow.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Failed to open main application: " + e.getMessage());
                    }
                });
            } else {
                showError("Invalid email or password");
            }
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
            logger.severe("Login error: " + e.getMessage());
        }
    }
    
    private String getFieldValue(JTextField field, String placeholder) {
        if (field == null) return "";
        String value = field instanceof JPasswordField ? 
            new String(((JPasswordField) field).getPassword()) : field.getText();
        return value.equals(placeholder) ? "" : value.trim();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Cleanup method for animations
    @Override
    public void dispose() {
        if (galaxyAnimationTimer != null) {
            galaxyAnimationTimer.stop();
            galaxyAnimationTimer = null;
        }
        super.dispose();
    }
    
    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginWindowNew();
        });
    }
}