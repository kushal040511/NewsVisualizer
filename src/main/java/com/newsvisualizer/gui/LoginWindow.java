package com.newsvisualizer.gui;

import com.newsvisualizer.model.User;
import com.newsvisualizer.service.AuthenticationService;
import com.newsvisualizer.service.SessionManager;
import com.newsvisualizer.gui.theme.ModernTheme;
import com.newsvisualizer.gui.components.ModernUIComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * Enhanced combined login/signup window with modern design, gradients, and smooth animations
 */
public class LoginWindow extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(LoginWindow.class);
    
    private final AuthenticationService authService;
    private final SessionManager sessionManager;
    
    // UI Components - Login
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JButton loginButton;
    private JLabel loginStatusLabel;
    private JProgressBar loginProgressBar;
    
    // UI Components - Signup
    private JTextField signupUsernameField;
    private JTextField signupEmailField;
    private JPasswordField signupPasswordField;
    private JPasswordField signupConfirmPasswordField;
    private JTextField signupFirstNameField;
    private JTextField signupLastNameField;
    private JButton signupButton;
    private JLabel signupStatusLabel;
    private JProgressBar signupProgressBar;
    private JProgressBar passwordStrengthBar;
    private JLabel passwordStrengthLabel;
    
    // UI Components - Common
    private JTabbedPane tabbedPane;
    private Timer animationTimer;
    
    // Result
    private boolean loginSuccessful = false;
    private User loggedInUser = null;
    
    public LoginWindow(Frame parent) {
        super(parent, "News Visualizer - Welcome", true);
        
        this.authService = new AuthenticationService();
        this.sessionManager = SessionManager.getInstance();
        
        initializeGUI();
        setupEventHandlers();
        startWelcomeAnimation();
        
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initializeGUI() {
        setSize(600, 800);
        setResizable(false);
        
        // Main panel with stunning blue-purple gradient background
        JPanel mainPanel = new JPanel() {
            private float animationPhase = 0;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth(), h = getHeight();
                
                // Elegant pastel gradient background
                Color[] gradientColors = createBlueGradientColors();
                
                // Base layer - soft vertical gradient
                GradientPaint baseGradient = new GradientPaint(
                    0, 0, gradientColors[0],
                    0, h, gradientColors[1]
                );
                g2d.setPaint(baseGradient);
                g2d.fillRect(0, 0, w, h);
                
                // Middle layer - diagonal sophistication
                GradientPaint midGradient = new GradientPaint(
                    0, 0, new Color(245, 245, 255, 80),    // Very light lavender
                    w, h/2, new Color(250, 240, 230, 60)   // Warm cream
                );
                g2d.setPaint(midGradient);
                g2d.fillRect(0, 0, w, h);
                
                // Top layer - subtle radial glow from center
                RadialGradientPaint topGradient = new RadialGradientPaint(
                    w/2, h/3, Math.max(w, h)/2,
                    new float[]{0.0f, 0.7f, 1.0f},
                    new Color[]{
                        new Color(255, 255, 255, 30),      // Soft white glow
                        new Color(240, 248, 255, 20),      // Alice blue 
                        new Color(245, 245, 255, 10)       // Lavender mist
                    }
                );
                g2d.setPaint(topGradient);
                g2d.fillRect(0, 0, w, h);
                
                // Add elegant floating elements
                for (int i = 0; i < 12; i++) {
                    float x = (float)(Math.sin(animationPhase + i * 0.6) * 120 + w/2);
                    float y = (float)(Math.cos(animationPhase + i * 0.4) * 80 + h/2);
                    float size = 6 + (float)(Math.sin(animationPhase + i * 0.5) * 2);
                    
                    // Soft pastel particles with varying opacity
                    float opacity = 0.08f + (float)(Math.sin(animationPhase + i * 0.7) * 0.04f);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                    
                    // Soft professional colors
                    if (i % 3 == 0) {
                        g2d.setColor(new Color(245, 248, 255)); // Very light blue
                    } else if (i % 3 == 1) {
                        g2d.setColor(new Color(248, 250, 252)); // Light gray-blue
                    } else {
                        g2d.setColor(new Color(250, 250, 250)); // Very light gray
                    }
                    
                    g2d.fillOval((int)(x - size/2), (int)(y - size/2), (int)size, (int)size);
                }
                
                // Add elegant geometric pattern
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.04f));
                g2d.setStroke(new BasicStroke(1.0f));
                
                for (int i = 0; i < w; i += 80) {
                    for (int j = 0; j < h; j += 80) {
                        // Soft blue-gray geometric elements
                        g2d.setColor(new Color(220, 230, 240, 50)); // Light blue-gray
                        g2d.drawOval(i - 20, j - 20, 40, 40);
                        
                        // Inner smaller circles for elegance
                        g2d.setColor(new Color(235, 240, 245, 35)); // Lighter blue-gray
                        g2d.drawOval(i - 10, j - 10, 20, 20);
                    }
                }
                
                g2d.dispose();
            }
            
            private Color[] createBlueGradientColors() {
                // Clean and professional gradient with subtle animation
                float phase = (float)Math.sin(animationPhase * 0.3) * 0.02f;
                
                // Modern theme colors
                Color softBlue = ModernTheme.Colors.BACKGROUND_SECONDARY;
                Color lightGray = ModernTheme.Colors.GRAY_50;
                Color paleBlue = new Color(230, 240, 250);        // Light Blue
                Color mintCream = new Color(245, 255, 250);       // Mint Cream
                Color whiteSmoke = ModernTheme.Colors.GRAY_100;
                
                // Create elegant transitions with subtle animation
                return new Color[] {
                    new Color(
                        (int)(softBlue.getRed() + (5 * phase)),
                        (int)(softBlue.getGreen() + (2 * phase)),
                        (int)(softBlue.getBlue() + (1 * phase))
                    ),
                    new Color(
                        (int)(lightGray.getRed() - (2 * phase)),
                        (int)(lightGray.getGreen() + (3 * phase)),
                        (int)(lightGray.getBlue() - (1 * phase))
                    ),
                    new Color(
                        (int)(paleBlue.getRed() + (3 * phase)),
                        (int)(paleBlue.getGreen() - (1 * phase)),
                        (int)(paleBlue.getBlue() + (2 * phase))
                    )
                };
            }
            
            public void updateAnimation() {
                animationPhase += 0.02f;
                if (animationPhase >= 2 * Math.PI) animationPhase = 0;
                repaint();
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Create split-screen design like reference image
        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        splitPanel.setOpaque(false);
        
        // Left panel - Green welcome section
        JPanel leftPanel = createWelcomePanel();
        
        // Right panel - Form section  
        JPanel rightPanel = createFormPanel();
        
        splitPanel.add(leftPanel);
        splitPanel.add(rightPanel);
        
        mainPanel.add(splitPanel, BorderLayout.CENTER);
        
        // Store reference to mainPanel for animation
        final JPanel animatedPanel = mainPanel;
        
        // Start background animation
        animationTimer = new Timer(50, e -> {
            ((JPanel) animatedPanel).repaint();
        });
        animationTimer.start();
        
        add(mainPanel);
    }
    
    private JTabbedPane createStyledTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Transparent background
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        
        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabAreaInsets.top = 10;
                tabAreaInsets.left = 15;
                tabAreaInsets.right = 15;
                tabAreaInsets.bottom = 5;
            }
            
            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                // Paint transparent tab area
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
                super.paintTabArea(g2d, tabPlacement, selectedIndex);
                g2d.dispose();
            }
            
            @Override
            protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects,
                                    int tabIndex, Rectangle iconRect, Rectangle textRect) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Rectangle tabRect = rects[tabIndex];
                boolean isSelected = tabPane.getSelectedIndex() == tabIndex;
                
                // Professional gradient for tabs
                Color startColor, endColor;
                if (isSelected) {
                    startColor = new Color(248, 250, 255, 220);  // Very light blue
                    endColor = new Color(235, 242, 250, 200);    // Light blue-gray
                } else {
                    startColor = new Color(245, 248, 252, 140);  // Very light gray-blue
                    endColor = new Color(240, 248, 255, 120);    // Alice blue
                }
                
                GradientPaint gradient = new GradientPaint(
                    tabRect.x, tabRect.y, startColor,
                    tabRect.x, tabRect.y + tabRect.height, endColor
                );
                
                g2d.setPaint(gradient);
                g2d.fillRoundRect(tabRect.x + 2, tabRect.y + 2, 
                                  tabRect.width - 4, tabRect.height - 2, 15, 15);
                
                // Add elegant border
                if (isSelected) {
                    g2d.setColor(new Color(200, 210, 230, 160)); // Soft blue-gray
                    g2d.setStroke(new BasicStroke(1.5f));
                } else {
                    g2d.setColor(new Color(220, 228, 240, 100)); // Very light blue-gray
                    g2d.setStroke(new BasicStroke(1.0f));
                }
                g2d.drawRoundRect(tabRect.x + 2, tabRect.y + 2, 
                                  tabRect.width - 4, tabRect.height - 2, 15, 15);
                
                // Paint text with elegant contrast
                g2d.setColor(isSelected ? new Color(80, 80, 100) : new Color(120, 120, 140)); // Sophisticated dark gray
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                String title = tabPane.getTitleAt(tabIndex);
                int x = tabRect.x + (tabRect.width - fm.stringWidth(title)) / 2;
                int y = tabRect.y + (tabRect.height + fm.getAscent()) / 2 - 2;
                g2d.drawString(title, x, y);
                
                g2d.dispose();
            }
        });
        
        tabbedPane.setOpaque(false);
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBorder(new EmptyBorder(10, 15, 15, 15));
        
        return tabbedPane;
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(40, 40, 20, 40));
        
        // Create animated logo panel
        JPanel logoPanel = new JPanel() {
            private float bounce = 0;
            private Timer bounceTimer;
            
            {
                bounceTimer = new Timer(100, e -> {
                    bounce += 0.2f;
                    if (bounce >= 2 * Math.PI) bounce = 0;
                    repaint();
                });
                bounceTimer.start();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw animated news icon
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int bounceOffset = (int)(Math.sin(bounce) * 5);
                
                // Professional background circle
                RadialGradientPaint bgGradient = new RadialGradientPaint(
                    centerX, centerY - bounceOffset, 50,
                    new float[]{0.0f, 0.5f, 1.0f},
                    new Color[]{
                        new Color(255, 255, 255, 200),  // Pure white center
                        new Color(240, 248, 255, 160),  // Alice blue middle
                        new Color(230, 240, 250, 120)   // Light blue outer
                    }
                );
                g2d.setPaint(bgGradient);
                g2d.fillOval(centerX - 50, centerY - 50 - bounceOffset, 100, 100);
                
                // Add subtle elegant border
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                g2d.setColor(new Color(200, 210, 230));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawOval(centerX - 52, centerY - 52 - bounceOffset, 104, 104);
                
                // Reset composite for icon
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                
                // News icon with sophisticated styling
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(80, 80, 120)); // Sophisticated dark gray-blue
                g2d.setFont(new Font("Arial", Font.BOLD, 52));
                FontMetrics fm = g2d.getFontMetrics();
                String icon = "📰";
                int iconX = centerX - fm.stringWidth(icon) / 2;
                int iconY = centerY - bounceOffset + fm.getAscent() / 2 - 8;
                g2d.drawString(icon, iconX, iconY);
                
                g2d.dispose();
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(100, 100));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(logoPanel);
        
        headerPanel.add(Box.createVerticalStrut(15));
        
        // Enhanced title with better glow effect
        JLabel titleLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                String text = "News Visualizer";
                Font font = new Font("Arial", Font.BOLD, 36);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                
                // Draw glow effect
                for (int i = 8; i > 0; i--) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                    g2d.setColor(new Color(138, 43, 226));
                    g2d.drawString(text, x - i/2, y - i/2);
                    g2d.drawString(text, x + i/2, y + i/2);
                }
                
                // Draw main text
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        titleLabel.setPreferredSize(new Dimension(400, 50));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        
        headerPanel.add(Box.createVerticalStrut(10));
        
        // Enhanced subtitle with gradient text effect
        JLabel subtitleLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                String text = "Your Gateway to Intelligent News Analysis";
                Font font = new Font("Arial", Font.PLAIN, 18);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                
                // Subtle glow
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setColor(new Color(30, 144, 255));
                g2d.drawString(text, x + 1, y + 1);
                
                // Main text
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(220, 220, 255));
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        subtitleLabel.setPreferredSize(new Dimension(400, 30));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(subtitleLabel);
        
        return headerPanel;
    }
    
    private void startWelcomeAnimation() {
        // Simple entrance animation
        SwingUtilities.invokeLater(() -> {
            repaint();
        });
    }
    
    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Solid green background like reference image
                g2d.setColor(new Color(76, 175, 80)); // Material Green
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
        
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBorder(new EmptyBorder(60, 40, 60, 40));
        
        // Add vertical glue to center content
        welcomePanel.add(Box.createVerticalGlue());
        
        // Welcome Back title
        JLabel welcomeTitle = new JLabel("Welcome Back!");
        welcomeTitle.setFont(new Font("Arial", Font.BOLD, 36));
        welcomeTitle.setForeground(Color.WHITE);
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(welcomeTitle);
        
        welcomePanel.add(Box.createVerticalStrut(20));
        
        // Subtitle
        JLabel subtitle = new JLabel("<html><div style='text-align: center;'>To keep connected with us please<br>login with your personal info</div></html>");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setForeground(Color.WHITE);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(subtitle);
        
        welcomePanel.add(Box.createVerticalStrut(40));
        
        // SIGN IN button (outlined style)
        JButton signInBtn = new JButton("SIGN IN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Transparent background with white border
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 25, 25);
                
                // White text
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        
        signInBtn.setFont(new Font("Arial", Font.BOLD, 14));
        signInBtn.setPreferredSize(new Dimension(200, 45));
        signInBtn.setMaximumSize(new Dimension(200, 45));
        signInBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInBtn.setContentAreaFilled(false);
        signInBtn.setBorderPainted(false);
        signInBtn.setFocusPainted(false);
        signInBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add action to switch to login mode
        signInBtn.addActionListener(e -> switchToLoginMode());
        
        welcomePanel.add(signInBtn);
        welcomePanel.add(Box.createVerticalGlue());
        
        return welcomePanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Modern theme background
                g2d.setColor(ModernTheme.Colors.BACKGROUND_PRIMARY);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
        
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(80, 60, 80, 60));
        
        // Create Account title
        JLabel title = new JLabel("Create Account");
        title.setFont(ModernTheme.Fonts.TITLE_LARGE);
        title.setForeground(ModernTheme.Colors.SECONDARY); // Modern green
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(title);
        
        formPanel.add(Box.createVerticalStrut(40));
        
        // Name field
        JTextField nameField = createModernTextField("Name");
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(ModernTheme.Spacing.LARGE));
        
        // Email field  
        JTextField emailField = createModernTextField("Email");
        formPanel.add(emailField);
        formPanel.add(Box.createVerticalStrut(ModernTheme.Spacing.LARGE));
        
        // Password field
        JPasswordField passwordField = createModernPasswordField("Password");
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(ModernTheme.Spacing.XLARGE));
        
        // SIGN UP button (modern style)
        JButton signUpBtn = new JButton("SIGN UP") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern green background using theme
                g2d.setColor(ModernTheme.Colors.SECONDARY);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ModernTheme.Radius.LARGE, ModernTheme.Radius.LARGE);
                
                // White text
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.setColor(ModernTheme.Colors.WHITE);
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        
        signUpBtn.setFont(ModernTheme.Fonts.BUTTON);
        signUpBtn.setPreferredSize(new Dimension(250, 45));
        signUpBtn.setMaximumSize(new Dimension(250, 45));
        signUpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpBtn.setContentAreaFilled(false);
        signUpBtn.setBorderPainted(false);
        signUpBtn.setFocusPainted(false);
        signUpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Store references for functionality
        this.signupFirstNameField = nameField;
        this.signupEmailField = emailField;
        this.signupPasswordField = passwordField;
        this.signupButton = signUpBtn;
        
        // Add signup action
        signUpBtn.addActionListener(e -> handleSignup());
        
        formPanel.add(signUpBtn);
        
        return formPanel;
    }
    
    private JTextField createModernTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern light background
                g2d.setColor(ModernTheme.Colors.GRAY_100);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
                
                // Draw text
                super.paintComponent(g2d);
                
                g2d.dispose();
            }
        };
        
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(15, 20, 15, 20));
        field.setFont(ModernTheme.Fonts.BODY_MEDIUM);
        field.setForeground(ModernTheme.Colors.TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(300, 50));
        field.setMaximumSize(new Dimension(300, 50));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add placeholder functionality
        field.putClientProperty("placeholder", placeholder);
        
        // Add icon
        if (placeholder.equals("Name")) {
            field.setText("👤 " + placeholder);
        } else if (placeholder.equals("Email")) {
            field.setText("📧 " + placeholder);
        }
        
        field.setForeground(new Color(150, 150, 150));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getForeground().equals(new Color(150, 150, 150))) {
                    field.setText("");
                    field.setForeground(new Color(100, 100, 100));
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    if (placeholder.equals("Name")) {
                        field.setText("👤 " + placeholder);
                    } else if (placeholder.equals("Email")) {
                        field.setText("📧 " + placeholder);
                    }
                    field.setForeground(new Color(150, 150, 150));
                }
            }
        });
        
        return field;
    }
    
    private JPasswordField createModernPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern light background
                g2d.setColor(ModernTheme.Colors.GRAY_100);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ModernTheme.Radius.MEDIUM, ModernTheme.Radius.MEDIUM);
                
                // Draw text
                super.paintComponent(g2d);
                
                g2d.dispose();
            }
        };
        
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(15, 20, 15, 20));
        field.setFont(ModernTheme.Fonts.BODY_MEDIUM);
        field.setForeground(ModernTheme.Colors.TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(300, 50));
        field.setMaximumSize(new Dimension(300, 50));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add placeholder
        field.setEchoChar((char) 0);
        field.setText("🔒 " + placeholder);
        field.setForeground(new Color(150, 150, 150));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getForeground().equals(new Color(150, 150, 150))) {
                    field.setText("");
                    field.setEchoChar('●');
                    field.setForeground(new Color(100, 100, 100));
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText("🔒 " + placeholder);
                    field.setForeground(new Color(150, 150, 150));
                }
            }
        });
        
        return field;
    }
    
    private void switchToLoginMode() {
        // This could switch to a login form instead of signup
        // For now, we'll keep the signup form but could extend this
        logger.debug("Switching to login mode");
    }
    
    private void handleSignup() {
        // Get field values (handling placeholders)
        String name = signupFirstNameField.getText();
        String email = signupEmailField.getText();
        String password = new String(signupPasswordField.getPassword());
        
        // Remove placeholder text and icons
        if (name.startsWith("👤 ")) {
            name = name.substring(3).trim();
        }
        if (email.startsWith("📧 ")) {
            email = email.substring(3).trim();
        }
        
        // Basic validation
        if (name.equals("Name") || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (email.equals("Email") || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.equals("Password") || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a password", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // For now, just show success message
        // In a real app, this would create the user account
        JOptionPane.showMessageDialog(this, 
            "Account created successfully!\nName: " + name + "\nEmail: " + email,
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // You could add actual user creation logic here
        logger.info("Signup successful for user: " + email);
    }
    
    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent background
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 20, 20);
                
                g2d.dispose();
            }
        };
        loginPanel.setOpaque(false);
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        // Username field
        loginPanel.add(createStyledFieldPanel("👤 Username or Email", 
            loginUsernameField = createAnimatedTextField(), true));
        loginPanel.add(Box.createVerticalStrut(20));
        
        // Password field
        loginPanel.add(createStyledFieldPanel("🔒 Password", 
            loginPasswordField = createAnimatedPasswordField(), true));
        loginPanel.add(Box.createVerticalStrut(30));
        
        // Login button with beautiful blue gradient
        loginButton = createEnhancedButton("Sign In", 
            new Color[]{new Color(30, 144, 255), new Color(25, 118, 210)}, Color.WHITE);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(loginButton);
        
        loginPanel.add(Box.createVerticalStrut(20));
        
        // Sign Up link with modern styling
        JPanel signupLinkPanel = new JPanel();
        signupLinkPanel.setOpaque(false);
        signupLinkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        JLabel normalText = new JLabel("Don't have an account?");
        normalText.setFont(new Font("Arial", Font.PLAIN, 14));
        normalText.setForeground(new Color(120, 120, 120));
        
        JLabel linkText = new JLabel("Sign Up") {
            private boolean isHovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                String text = getText();
                Font font = new Font("Arial", Font.BOLD, 14);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                
                int x = 0;
                int y = fm.getAscent();
                
                // Main text with blue color
                g2d.setColor(isHovered ? new Color(25, 118, 210) : new Color(30, 144, 255));
                g2d.drawString(text, x, y);
                
                // Add underline for link effect
                if (isHovered) {
                    g2d.setStroke(new BasicStroke(1.0f));
                    g2d.drawLine(x, y + 2, x + fm.stringWidth(text), y + 2);
                }
                
                g2d.dispose();
            }
        };
        linkText.setFont(new Font("Arial", Font.BOLD, 14));
        linkText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabbedPane.setSelectedIndex(1);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                try {
                    java.lang.reflect.Field field = label.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.setBoolean(label, true);
                } catch (Exception ex) {}
                linkText.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                try {
                    java.lang.reflect.Field field = label.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.setBoolean(label, false);
                } catch (Exception ex) {}
                linkText.repaint();
            }
        });
        
        signupLinkPanel.add(normalText);
        signupLinkPanel.add(linkText);
        signupLinkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(signupLinkPanel);
        
        loginPanel.add(Box.createVerticalStrut(15));
        
        // Progress bar
        loginProgressBar = createStyledProgressBar();
        loginProgressBar.setVisible(false);
        loginPanel.add(loginProgressBar);
        
        loginPanel.add(Box.createVerticalStrut(10));
        
        // Status label
        loginStatusLabel = new JLabel(" ");
        loginStatusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        loginStatusLabel.setForeground(Color.WHITE);
        loginStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(loginStatusLabel);
        
        return loginPanel;
    }
    
    private JPanel createSignupPanel() {
        JPanel signupPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent background
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 20, 20);
                
                g2d.dispose();
            }
        };
        signupPanel.setOpaque(false);
        signupPanel.setLayout(new BoxLayout(signupPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(createSignupForm());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        signupPanel.add(scrollPane);
        
        return signupPanel;
    }
    
    private JPanel createSignupForm() {
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 40, 30, 40));
        
        // Add helpful instructions
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setOpaque(false);
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        
        JLabel instructionLabel = new JLabel("<html><div style='text-align: center; color: rgba(255,255,255,0.9); font-size: 12px;'>"
            + "🌟 Fields marked with * are required<br/>"
            + "📝 Username: 3-20 chars, letters/numbers/underscore only<br/>"
            + "💡 Password must be at least 6 characters long"
            + "</div></html>");
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructionsPanel.add(instructionLabel);
        
        formPanel.add(instructionsPanel);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Name fields in a row (optional)
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        namePanel.setOpaque(false);
        
        JPanel firstNamePanel = createStyledFieldPanel("👤 First Name (optional)", 
            signupFirstNameField = createAnimatedTextField(), false);
        firstNamePanel.setPreferredSize(new Dimension(200, 80));
        namePanel.add(firstNamePanel);
        
        JPanel lastNamePanel = createStyledFieldPanel("👤 Last Name (optional)", 
            signupLastNameField = createAnimatedTextField(), false);
        lastNamePanel.setPreferredSize(new Dimension(200, 80));
        namePanel.add(lastNamePanel);
        
        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Username field (required)
        formPanel.add(createStyledFieldPanel("🏷️ Username *", 
            signupUsernameField = createAnimatedTextField(), true));
        formPanel.add(Box.createVerticalStrut(15));
        
        // Email field (required)
        formPanel.add(createStyledFieldPanel("📧 Email Address *", 
            signupEmailField = createAnimatedTextField(), true));
        formPanel.add(Box.createVerticalStrut(15));
        
        // Password field with strength indicator
        JPanel passwordPanel = createPasswordWithStrength();
        formPanel.add(passwordPanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Confirm password field (required)
        formPanel.add(createStyledFieldPanel("🔒 Confirm Password *", 
            signupConfirmPasswordField = createAnimatedPasswordField(), true));
        formPanel.add(Box.createVerticalStrut(25));
        
        // Signup button with modern green gradient (like reference image)
        signupButton = createEnhancedButton("Sign Up", 
            new Color[]{new Color(76, 175, 80), new Color(56, 142, 60)}, Color.WHITE);
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(signupButton);
        
        formPanel.add(Box.createVerticalStrut(20));
        
        // Sign In link with modern styling
        JPanel signinLinkPanel = new JPanel();
        signinLinkPanel.setOpaque(false);
        signinLinkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        JLabel normalText2 = new JLabel("Already have an account?");
        normalText2.setFont(new Font("Arial", Font.PLAIN, 14));
        normalText2.setForeground(new Color(120, 120, 120));
        
        JLabel linkText2 = new JLabel("Sign In") {
            private boolean isHovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                String text = getText();
                Font font = new Font("Arial", Font.BOLD, 14);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                
                int x = 0;
                int y = fm.getAscent();
                
                // Main text with blue color
                g2d.setColor(isHovered ? new Color(25, 118, 210) : new Color(30, 144, 255));
                g2d.drawString(text, x, y);
                
                // Add underline for link effect
                if (isHovered) {
                    g2d.setStroke(new BasicStroke(1.0f));
                    g2d.drawLine(x, y + 2, x + fm.stringWidth(text), y + 2);
                }
                
                g2d.dispose();
            }
        };
        linkText2.setFont(new Font("Arial", Font.BOLD, 14));
        linkText2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkText2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tabbedPane.setSelectedIndex(0);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                try {
                    java.lang.reflect.Field field = label.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.setBoolean(label, true);
                } catch (Exception ex) {}
                linkText2.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                try {
                    java.lang.reflect.Field field = label.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.setBoolean(label, false);
                } catch (Exception ex) {}
                linkText2.repaint();
            }
        });
        
        signinLinkPanel.add(normalText2);
        signinLinkPanel.add(linkText2);
        signinLinkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(signinLinkPanel);
        
        formPanel.add(Box.createVerticalStrut(10));
        
        // Progress bar
        signupProgressBar = createStyledProgressBar();
        signupProgressBar.setVisible(false);
        formPanel.add(signupProgressBar);
        
        formPanel.add(Box.createVerticalStrut(10));
        
        // Status label
        signupStatusLabel = new JLabel(" ");
        signupStatusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        signupStatusLabel.setForeground(Color.WHITE);
        signupStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        signupStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(signupStatusLabel);
        
        return formPanel;
    }
    
    private JPanel createPasswordWithStrength() {
        JPanel passwordPanel = new JPanel();
        passwordPanel.setOpaque(false);
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Password field (required)
        JPanel fieldPanel = createStyledFieldPanel("🔒 Password *", 
            signupPasswordField = createAnimatedPasswordField(), true);
        passwordPanel.add(fieldPanel);
        
        passwordPanel.add(Box.createVerticalStrut(8));
        
        // Strength indicator
        passwordStrengthBar = new JProgressBar(0, 5) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Progress
                if (getValue() > 0) {
                    int width = (int)((double)getValue() / getMaximum() * getWidth());
                    Color strengthColor = getStrengthColor();
                    GradientPaint gradient = new GradientPaint(0, 0, strengthColor, width, 0, strengthColor.brighter());
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, width, getHeight(), 8, 8);
                }
                
                g2d.dispose();
            }
            
            private Color getStrengthColor() {
                switch (getValue()) {
                    case 0:
                    case 1: return new Color(231, 76, 60);
                    case 2: return new Color(243, 156, 18);
                    case 3: return new Color(241, 196, 15);
                    case 4: return new Color(39, 174, 96);
                    case 5: return new Color(46, 204, 113);
                    default: return Color.GRAY;
                }
            }
        };
        passwordStrengthBar.setValue(0);
        passwordStrengthBar.setPreferredSize(new Dimension(350, 8));
        passwordStrengthBar.setMaximumSize(new Dimension(350, 8));
        passwordStrengthBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordPanel.add(passwordStrengthBar);
        
        passwordPanel.add(Box.createVerticalStrut(5));
        
        // Strength label
        passwordStrengthLabel = new JLabel("Enter a password");
        passwordStrengthLabel.setFont(new Font("Arial", Font.BOLD, 12));
        passwordStrengthLabel.setForeground(new Color(255, 255, 255, 200));
        passwordStrengthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        passwordStrengthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordPanel.add(passwordStrengthLabel);
        
        return passwordPanel;
    }
    
    private JPanel createStyledFieldPanel(String labelText, JTextField field, boolean fullWidth) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setOpaque(false);
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        if (fullWidth) {
            fieldPanel.setMaximumSize(new Dimension(400, 80));
            fieldPanel.setPreferredSize(new Dimension(400, 80));
        }
        
        // Label with glow effect
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(255, 255, 255, 240));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldPanel.add(label);
        
        fieldPanel.add(Box.createVerticalStrut(8));
        
        // Configure field
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (fullWidth) {
            field.setMaximumSize(new Dimension(350, 45));
            field.setPreferredSize(new Dimension(350, 45));
        } else {
            field.setMaximumSize(new Dimension(180, 45));
            field.setPreferredSize(new Dimension(180, 45));
        }
        
        fieldPanel.add(field);
        
        return fieldPanel;
    }
    
    private JTextField createAnimatedTextField() {
        JTextField field = new JTextField() {
            private boolean focused = false;
            private float glowIntensity = 0.0f;
            private Timer glowTimer;
            
            {
                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        startGlowAnimation();
                    }
                    
                    @Override
                    public void focusLost(FocusEvent e) {
                        focused = false;
                    }
                });
            }
            
            private void startGlowAnimation() {
                if (glowTimer != null) glowTimer.stop();
                glowTimer = new Timer(50, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (focused) {
                            glowIntensity = Math.min(1.0f, glowIntensity + 0.1f);
                        } else {
                            glowIntensity = Math.max(0.0f, glowIntensity - 0.1f);
                            if (glowIntensity <= 0.0f) {
                                glowTimer.stop();
                            }
                        }
                        repaint();
                    }
                });
                glowTimer.start();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background with gradient
                GradientPaint bgGradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 250),
                    0, getHeight(), new Color(248, 249, 250)
                );
                g2d.setPaint(bgGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Beautiful blue glow effect when focused
                if (glowIntensity > 0) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowIntensity * 0.6f));
                    g2d.setColor(new Color(30, 144, 255));
                    g2d.setStroke(new BasicStroke(3.5f));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                    
                    // Add outer purple glow
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowIntensity * 0.3f));
                    g2d.setColor(new Color(138, 43, 226));
                    g2d.setStroke(new BasicStroke(5.0f));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }
                
                // Border
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(200, 206, 212));
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setForeground(new Color(44, 62, 80));
        field.setBorder(new EmptyBorder(10, 15, 10, 15));
        field.setOpaque(false);
        
        return field;
    }
    
    private JPasswordField createAnimatedPasswordField() {
        JPasswordField field = new JPasswordField() {
            private boolean focused = false;
            private float glowIntensity = 0.0f;
            private Timer glowTimer;
            
            {
                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        startGlowAnimation();
                    }
                    
                    @Override
                    public void focusLost(FocusEvent e) {
                        focused = false;
                    }
                });
            }
            
            private void startGlowAnimation() {
                if (glowTimer != null) glowTimer.stop();
                glowTimer = new Timer(50, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (focused) {
                            glowIntensity = Math.min(1.0f, glowIntensity + 0.1f);
                        } else {
                            glowIntensity = Math.max(0.0f, glowIntensity - 0.1f);
                            if (glowIntensity <= 0.0f) {
                                glowTimer.stop();
                            }
                        }
                        repaint();
                    }
                });
                glowTimer.start();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background with gradient
                GradientPaint bgGradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 250),
                    0, getHeight(), new Color(248, 249, 250)
                );
                g2d.setPaint(bgGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Beautiful blue glow effect when focused
                if (glowIntensity > 0) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowIntensity * 0.6f));
                    g2d.setColor(new Color(30, 144, 255));
                    g2d.setStroke(new BasicStroke(3.5f));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                    
                    // Add outer purple glow
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowIntensity * 0.3f));
                    g2d.setColor(new Color(138, 43, 226));
                    g2d.setStroke(new BasicStroke(5.0f));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }
                
                // Border
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(200, 206, 212));
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setForeground(new Color(44, 62, 80));
        field.setBorder(new EmptyBorder(10, 15, 10, 15));
        field.setOpaque(false);
        
        return field;
    }
    
    private JButton createEnhancedButton(String text, Color[] gradientColors, Color textColor) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            private boolean isPressed = false;
            private float hoverScale = 1.0f;
            private Timer hoverTimer;
            
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        startHoverAnimation();
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                    
                    @Override
                    public void mousePressed(MouseEvent e) {
                        isPressed = true;
                        repaint();
                    }
                    
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        isPressed = false;
                        repaint();
                    }
                });
            }
            
            private void startHoverAnimation() {
                if (hoverTimer != null) hoverTimer.stop();
                hoverTimer = new Timer(20, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isHovered && hoverScale < 1.05f) {
                            hoverScale += 0.01f;
                        } else if (!isHovered && hoverScale > 1.0f) {
                            hoverScale -= 0.01f;
                        } else {
                            hoverTimer.stop();
                        }
                        repaint();
                    }
                });
                hoverTimer.start();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Apply scale transformation
                if (hoverScale != 1.0f) {
                    double centerX = w / 2.0;
                    double centerY = h / 2.0;
                    g2d.translate(centerX, centerY);
                    g2d.scale(hoverScale, hoverScale);
                    g2d.translate(-centerX, -centerY);
                }
                
                // Shadow effect
                if (isHovered && !isPressed) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    g2d.setColor(Color.BLACK);
                    g2d.fillRoundRect(2, 4, w - 2, h - 2, 15, 15);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
                
                // Button background with gradient
                Color color1 = isPressed ? gradientColors[1].darker() : 
                              isHovered ? gradientColors[0].brighter() : gradientColors[0];
                Color color2 = isPressed ? gradientColors[1].darker().darker() : 
                              isHovered ? gradientColors[1].brighter() : gradientColors[1];
                
                GradientPaint gradient = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, w, h, 15, 15);
                
                // Highlight
                if (isHovered) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                    g2d.setColor(Color.WHITE);
                    g2d.fillRoundRect(0, 0, w, h/2, 15, 15);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
                
                // Border
                g2d.setColor(gradientColors[1].darker());
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 15, 15);
                
                g2d.dispose();
                
                // Paint text
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(textColor);
        button.setPreferredSize(new Dimension(320, 50));
        button.setMaximumSize(new Dimension(320, 50));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        
        return button;
    }
    
    private JProgressBar createStyledProgressBar() {
        JProgressBar progressBar = new JProgressBar() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Progress with animated gradient
                if (isIndeterminate()) {
                    int barWidth = getWidth() / 4;
                    int pos = (int)(System.currentTimeMillis() / 10) % (getWidth() + barWidth);
                    
                    Color[] colors = {new Color(138, 43, 226), new Color(30, 144, 255), new Color(25, 25, 112)};
                    for (int i = 0; i < colors.length; i++) {
                        int x = pos - barWidth + i * barWidth / colors.length;
                        g2d.setColor(colors[i]);
                        g2d.fillRoundRect(Math.max(0, x), 0, Math.min(barWidth / colors.length, getWidth() - Math.max(0, x)), getHeight(), 8, 8);
                    }
                }
                
                g2d.dispose();
            }
        };
        
        progressBar.setPreferredSize(new Dimension(320, 6));
        progressBar.setMaximumSize(new Dimension(320, 6));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setOpaque(false);
        progressBar.setBorderPainted(false);
        
        // Animate the progress bar
        Timer animationTimer = new Timer(50, e -> progressBar.repaint());
        animationTimer.start();
        
        return progressBar;
    }
    
    private void setupEventHandlers() {
        // Login button - only if it exists (old tabbed design)
        if (loginButton != null) {
            loginButton.addActionListener(e -> performLogin());
        }
        
        // Signup button - exists in both designs
        if (signupButton != null) {
            signupButton.addActionListener(e -> performSignup());
        }
        
        // Password strength checker for signup
        if (signupPasswordField != null) {
            signupPasswordField.getDocument().addDocumentListener(new DocumentListener() {
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
        
        // Enter key listeners for login
        KeyListener loginEnterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        
        if (loginUsernameField != null) loginUsernameField.addKeyListener(loginEnterListener);
        if (loginPasswordField != null) loginPasswordField.addKeyListener(loginEnterListener);
        
        // Enter key listeners for signup
        KeyListener signupEnterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSignup();
                }
            }
        };
        
        if (signupConfirmPasswordField != null) signupConfirmPasswordField.addKeyListener(signupEnterListener);
    }
    
    private void updatePasswordStrength() {
        if (signupPasswordField == null) return;
        
        String password = new String(signupPasswordField.getPassword());
        AuthenticationService.PasswordStrength strength = authService.checkPasswordStrength(password);
        
        passwordStrengthBar.setValue(strength.getScore());
        passwordStrengthLabel.setText(strength.getLevel() + " - " + strength.getFeedback());
        
        // Update color
        passwordStrengthBar.repaint();
        
        Color strengthColor = getPasswordStrengthColor(strength.getScore());
        passwordStrengthLabel.setForeground(strengthColor);
    }
    
    private Color getPasswordStrengthColor(int score) {
        switch (score) {
            case 0:
            case 1: return new Color(231, 76, 60, 200);
            case 2: return new Color(243, 156, 18, 200);
            case 3: return new Color(241, 196, 15, 200);
            case 4: return new Color(39, 174, 96, 200);
            case 5: return new Color(46, 204, 113, 200);
            default: return new Color(255, 255, 255, 200);
        }
    }
    
    private void performLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showLoginStatus("Please enter both username and password", false);
            return;
        }
        
        // Disable form during login
        setLoginFormEnabled(false);
        showLoginProgress(true);
        showLoginStatus("💫 Signing in...", true);
        
        // Perform login in background thread
        SwingWorker<AuthenticationService.AuthenticationResult, Void> worker = 
            new SwingWorker<AuthenticationService.AuthenticationResult, Void>() {
            @Override
            protected AuthenticationService.AuthenticationResult doInBackground() {
                return authService.login(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    AuthenticationService.AuthenticationResult result = get();
                    handleLoginResult(result);
                } catch (Exception e) {
                    logger.error("Error during login", e);
                    showLoginStatus("Login failed due to unexpected error", false);
                } finally {
                    setLoginFormEnabled(true);
                    showLoginProgress(false);
                }
            }
        };
        
        worker.execute();
    }
    
    private void performSignup() {
        logger.info("Starting signup process");
        
        // Get form data
        String username = signupUsernameField.getText().trim();
        String email = signupEmailField.getText().trim();
        String password = new String(signupPasswordField.getPassword());
        String confirmPassword = new String(signupConfirmPasswordField.getPassword());
        String firstName = signupFirstNameField.getText().trim();
        String lastName = signupLastNameField.getText().trim();
        
        // Handle empty name fields
        if (firstName.isEmpty()) firstName = null;
        if (lastName.isEmpty()) lastName = null;
        
        logger.info("Signup form data - Username: {}, Email: {}, FirstName: {}, LastName: {}", 
                   username, email, firstName, lastName);
        
        // Basic client-side validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            logger.warn("Signup validation failed - missing required fields");
            showSignupStatus("❌ Please fill in Username, Email, and Password", false);
            return;
        }
        
        // Validate username format and length
        if (username.length() < 3 || username.length() > 20) {
            showSignupStatus("❌ Username must be 3-20 characters long", false);
            return;
        }
        
        // Check username contains only valid characters
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showSignupStatus("❌ Username can only contain letters, numbers, and underscores", false);
            return;
        }
        
        // Validate email format
        if (!email.contains("@") || !email.contains(".")) {
            showSignupStatus("❌ Please enter a valid email address", false);
            return;
        }
        
        // Validate password length
        if (password.length() < 6) {
            showSignupStatus("❌ Password must be at least 6 characters long", false);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            logger.warn("Signup validation failed - passwords do not match");
            showSignupStatus("❌ Passwords do not match", false);
            return;
        }
        
        // Disable form during registration
        setSignupFormEnabled(false);
        showSignupProgress(true);
        showSignupStatus("🎆 Creating your account...", true);
        
        logger.info("About to start registration for user: {}", username);
        
        // Make variables effectively final for use in inner class
        final String finalUsername = username;
        final String finalEmail = email;
        final String finalPassword = password;
        final String finalConfirmPassword = confirmPassword;
        final String finalFirstName = firstName;
        final String finalLastName = lastName;
        
        // Perform registration in background thread
        SwingWorker<AuthenticationService.AuthenticationResult, Void> worker = 
            new SwingWorker<AuthenticationService.AuthenticationResult, Void>() {
            @Override
            protected AuthenticationService.AuthenticationResult doInBackground() {
                logger.info("Calling authService.register() in background thread");
                AuthenticationService.AuthenticationResult result = authService.register(
                    finalUsername, finalEmail, finalPassword, finalConfirmPassword, finalFirstName, finalLastName);
                logger.info("Registration result: success={}, message={}", result.isSuccess(), result.getMessage());
                return result;
            }
            
            @Override
            protected void done() {
                try {
                    AuthenticationService.AuthenticationResult result = get();
                    logger.info("Processing registration result in UI thread");
                    handleSignupResult(result);
                } catch (Exception e) {
                    logger.error("Error during registration", e);
                    showSignupStatus("❌ Registration failed: " + e.getMessage(), false);
                } finally {
                    setSignupFormEnabled(true);
                    showSignupProgress(false);
                }
            }
        };
        
        worker.execute();
    }
    
    private void handleLoginResult(AuthenticationService.AuthenticationResult result) {
        if (result.isSuccess()) {
            showLoginStatus("✨ Login successful! Welcome back.", true);
            
            // Start user session
            sessionManager.startSession(result.getUser());
            
            loginSuccessful = true;
            loggedInUser = result.getUser();
            
            // Close dialog after short delay
            Timer timer = new Timer(1000, e -> dispose());
            timer.setRepeats(false);
            timer.start();
            
        } else {
            showLoginStatus(result.getMessage(), false);
            loginPasswordField.setText("");
            loginPasswordField.requestFocus();
        }
    }
    
    private void handleSignupResult(AuthenticationService.AuthenticationResult result) {
        if (result.isSuccess()) {
            showSignupStatus("🎉 Account created successfully! Welcome to News Visualizer.", true);
            
            // Start user session
            sessionManager.startSession(result.getUser());
            
            loginSuccessful = true;
            loggedInUser = result.getUser();
            
            // Close dialog after short delay
            Timer timer = new Timer(2000, e -> dispose());
            timer.setRepeats(false);
            timer.start();
            
        } else {
            showSignupStatus(result.getMessage(), false);
        }
    }
    
    private void setLoginFormEnabled(boolean enabled) {
        loginUsernameField.setEnabled(enabled);
        loginPasswordField.setEnabled(enabled);
        loginButton.setEnabled(enabled);
    }
    
    private void setSignupFormEnabled(boolean enabled) {
        signupUsernameField.setEnabled(enabled);
        signupEmailField.setEnabled(enabled);
        signupPasswordField.setEnabled(enabled);
        signupConfirmPasswordField.setEnabled(enabled);
        signupFirstNameField.setEnabled(enabled);
        signupLastNameField.setEnabled(enabled);
        signupButton.setEnabled(enabled);
    }
    
    private void showLoginProgress(boolean show) {
        loginProgressBar.setVisible(show);
        loginProgressBar.setIndeterminate(show);
    }
    
    private void showSignupProgress(boolean show) {
        signupProgressBar.setVisible(show);
        signupProgressBar.setIndeterminate(show);
    }
    
    private void showLoginStatus(String message, boolean isSuccess) {
        loginStatusLabel.setText(message);
        loginStatusLabel.setForeground(isSuccess ? new Color(144, 238, 144) : new Color(255, 182, 193));
    }
    
    private void showSignupStatus(String message, boolean isSuccess) {
        signupStatusLabel.setText(message);
        signupStatusLabel.setForeground(isSuccess ? new Color(144, 238, 144) : new Color(255, 182, 193));
    }
    
    // Cleanup method for animations
    @Override
    public void dispose() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        super.dispose();
    }
    
    // Public methods
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
    
    public User getLoggedInUser() {
        return loggedInUser;
    }
}
