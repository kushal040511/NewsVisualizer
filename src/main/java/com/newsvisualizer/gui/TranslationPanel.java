package com.newsvisualizer.gui;

import com.newsvisualizer.gui.theme.EnhancedModernTheme;
import com.newsvisualizer.service.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * User-friendly translation and dictionary panel for NewsVisualizer
 * Helps users understand words and phrases they encounter in news articles
 */
public class TranslationPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(TranslationPanel.class);

    public interface ActivityListener {
        void onTranslationAction(String actionType, String input, String resultSummary);
    }
    
    private final TranslationService translationService;
    private ActivityListener activityListener;
    
    // UI Components
    private JTextField inputField;
    private JTextArea resultArea;
    private JComboBox<String> featureCombo;
    private JComboBox<String> fromLanguageCombo;
    private JComboBox<String> toLanguageCombo;
    private JButton processButton;
    private JButton clearButton;
    private JButton historyButton;
    private JLabel statusLabel;
    private JPanel translationPanel;
    
    // Feature types
    private static final String[] FEATURES = {
        "Define Word",
        "Translate Text",
        "Explain Sentence",
        "Detect Language"
    };
    
    // Supported languages
    private static final String[] LANGUAGES = {
        "Auto-detect", "English", "Hindi", "Spanish", "French", "German", "Italian",
        "Portuguese", "Japanese", "Korean", "Chinese (Simplified)", "Dutch", "Polish",
        "Russian", "Turkish", "Ukrainian", "Arabic"
    };
    
    public TranslationPanel() {
        this.translationService = new TranslationService();
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        
        // Input field with optimal sizing
        inputField = new JTextField(35);
        inputField.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnhancedModernTheme.Colors.BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        inputField.setToolTipText("Enter word, phrase, or sentence to analyze");
        
        // Feature selection
        featureCombo = new JComboBox<>(FEATURES);
        featureCombo.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        featureCombo.setBackground(EnhancedModernTheme.Colors.WHITE);
        featureCombo.setToolTipText("Select what you want to do with your text");
        
        // Language selection
        fromLanguageCombo = new JComboBox<>(LANGUAGES);
        fromLanguageCombo.setSelectedItem("Auto-detect");
        fromLanguageCombo.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        fromLanguageCombo.setBackground(EnhancedModernTheme.Colors.WHITE);
        
        toLanguageCombo = new JComboBox<>(LANGUAGES);
        toLanguageCombo.setSelectedItem("Hindi");
        toLanguageCombo.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        toLanguageCombo.setBackground(EnhancedModernTheme.Colors.WHITE);
        
        // Translation panel (initially hidden)
        translationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        translationPanel.add(new JLabel("From:"));
        translationPanel.add(fromLanguageCombo);
        translationPanel.add(new JLabel("To:"));
        translationPanel.add(toLanguageCombo);
        translationPanel.setVisible(false);
        translationPanel.setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        
        // Buttons
        processButton = createStyledButton("Process", EnhancedModernTheme.Colors.PRIMARY, Color.WHITE);
        processButton.setToolTipText("Click to process your input");
        
        clearButton = createStyledButton("Clear", EnhancedModernTheme.Colors.GRAY_500, Color.WHITE);
        clearButton.setToolTipText("Clear all fields");
        
        historyButton = createStyledButton("Examples", EnhancedModernTheme.Colors.ACCENT, Color.WHITE);
        historyButton.setToolTipText("View example usage");
        
        // Result area with compact dimensions
        resultArea = new JTextArea(8, 40); // Reduced from 14x50 to 8x40
        resultArea.setFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);
        resultArea.setBackground(EnhancedModernTheme.Colors.WHITE);
        resultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String welcomeText = "AI Language Assistant\n\n" +
                           "This helper can define words, translate text, explain complex sentences, and detect language.\n\n" +
                           "Examples:\n" +
                           "• inflation\n" +
                           "• Hello world\n" +
                           "• नमस्ते\n" +
                           "• The central bank raised interest rates to control inflation.";
        resultArea.setText(welcomeText);
        
        // Status label
        statusLabel = new JLabel("Ready to help you understand news content 📰");
        statusLabel.setFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        statusLabel.setForeground(EnhancedModernTheme.Colors.TEXT_MUTED);
    }
    
    private void layoutComponents() {
        // Top panel - Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        JLabel titleLabel = new JLabel("📖 Translation & Dictionary Helper");
        titleLabel.setFont(EnhancedModernTheme.Fonts.SECTION_TITLE);
        titleLabel.setForeground(EnhancedModernTheme.Colors.TEXT_PRIMARY);
        titlePanel.add(titleLabel);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        inputPanel.setBorder(new TitledBorder("Enter Text to Analyze"));
        
        JPanel topInputPanel = new JPanel(new BorderLayout(5, 5));
        topInputPanel.setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        topInputPanel.add(inputField, BorderLayout.CENTER);
        topInputPanel.add(featureCombo, BorderLayout.EAST);
        
        inputPanel.add(topInputPanel, BorderLayout.NORTH);
        inputPanel.add(translationPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        buttonPanel.add(processButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(historyButton);
        
        // Result panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(new TitledBorder("Result"));
        resultPanel.setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(EnhancedModernTheme.Colors.BORDER_LIGHT, 1));
        
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        statusPanel.add(statusLabel);
        
        // Main layout
        add(titlePanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(EnhancedModernTheme.Colors.BACKGROUND_CONTENT);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(resultPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Feature selection change
        featureCombo.addActionListener(e -> {
            String selected = (String) featureCombo.getSelectedItem();
            boolean isTranslation = selected != null && selected.contains("Translate");
            translationPanel.setVisible(isTranslation);
            
            // Update placeholder text based on selection
            updatePlaceholderText(selected);
            revalidate();
            repaint();
        });
        
        // Process button
        processButton.addActionListener(e -> processInput());
        
        // Clear button
        clearButton.addActionListener(e -> clearAll());
        
        // History/Examples button
        historyButton.addActionListener(e -> showExamples());
        
        // Enter key in input field
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    processInput();
                }
            }
        });
        
        // Auto-clear status on input
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (statusLabel.getText().contains("Error") || statusLabel.getText().contains("No input")) {
                    statusLabel.setText("Ready to process...");
                }
            }
        });
    }
    
    private void processInput() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) {
            statusLabel.setText("⚠️ Please enter some text to process");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        String selectedFeature = (String) featureCombo.getSelectedItem();
        
        // Update status
        statusLabel.setText("Processing...");
        statusLabel.setForeground(new Color(59, 130, 246));
        processButton.setEnabled(false);
        
        // Process in background thread
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return processWithService(input, selectedFeature);
            }
            
            @Override
            protected void done() {
                try {
                    String result = get();
                    displayResult(result);
                    if (activityListener != null) {
                        activityListener.onTranslationAction(selectedFeature, input, summarizeResult(result));
                    }
                    statusLabel.setText("AI response ready");
                    statusLabel.setForeground(new Color(34, 197, 94));
                } catch (Exception e) {
                    logger.error("Error processing input", e);
                    resultArea.setText("❌ Error processing your request. Please try again.");
                    statusLabel.setText("❌ Processing failed");
                    statusLabel.setForeground(Color.RED);
                } finally {
                    processButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private String processWithService(String input, String feature) {
        try {
            if (feature.contains("Define")) {
                return translationService.getDefinition(input);
            } else if (feature.contains("Translate")) {
                String fromLang = (String) fromLanguageCombo.getSelectedItem();
                String toLang = (String) toLanguageCombo.getSelectedItem();
                return translationService.translateText(input, fromLang, toLang);
            } else if (feature.contains("Explain")) {
                return translationService.explainPhrase(input);
            } else if (feature.contains("Detect")) {
                return translationService.detectLanguage(input);
            }
            
            return "Feature not implemented yet.";
        } catch (Exception e) {
            logger.error("Error in translation service", e);
            return "Error processing request: " + e.getMessage();
        }
    }
    
    private void displayResult(String result) {
        resultArea.setText(result);
        resultArea.setCaretPosition(0); // Scroll to top
    }

    public void setActivityListener(ActivityListener activityListener) {
        this.activityListener = activityListener;
    }
    
    private void clearAll() {
        inputField.setText("");
        resultArea.setText("Ready for new input.\n\nChoose a mode and enter text to process.");
        statusLabel.setText("Cleared. Ready for new input.");
        statusLabel.setForeground(new Color(107, 114, 128));
        inputField.requestFocus();
    }
    
    private void showExamples() {
        String examples = "USAGE EXAMPLES\n\n" +
                         "DEFINE WORD\n" +
                         "• inflation\n" +
                         "• blockchain\n" +
                         "• नमस्ते\n\n" +

                         "TRANSLATE TEXT\n" +
                         "• Hello world -> Hindi\n" +
                         "• Bonjour tout le monde -> English\n" +
                         "• Long news paragraphs are supported\n\n" +

                         "EXPLAIN SENTENCE\n" +
                         "• The central bank raised interest rates to control inflation.\n" +
                         "• This can simplify complex news paragraphs.\n\n" +

                         "DETECT LANGUAGE\n" +
                         "• Hola amigo\n" +
                         "• こんにちは\n" +
                         "• Mixed multilingual input";
        
        resultArea.setText(examples);
        statusLabel.setText("Showing usage examples");
        statusLabel.setForeground(new Color(59, 130, 246));
    }
    
    private void updatePlaceholderText(String feature) {
        if (feature == null) return;
        
        if (feature.contains("Define")) {
            inputField.setToolTipText("Enter a word or phrase to get a definition with an example sentence.");
        } else if (feature.contains("Translate")) {
            inputField.setToolTipText("Enter any word, sentence, or paragraph to translate.");
        } else if (feature.contains("Explain")) {
            inputField.setToolTipText("Enter a sentence or paragraph to get a simpler explanation.");
        } else if (feature.contains("Detect")) {
            inputField.setToolTipText("Enter text in any language to detect the dominant language.");
        }
    }

    private String summarizeResult(String result) {
        if (result == null || result.isBlank()) {
            return "No result";
        }
        String normalized = result.replaceAll("\\s+", " ").trim();
        return normalized.length() > 120 ? normalized.substring(0, 117) + "..." : normalized;
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFont(EnhancedModernTheme.Fonts.BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 40));
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor;
            Color hoverColor = bgColor.darker();
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    /**
     * Public method to process text from outside the panel
     * Useful for integrating with other parts of the application
     */
    public void processText(String text, String featureType) {
        inputField.setText(text);
        
        // Set appropriate feature
        for (int i = 0; i < FEATURES.length; i++) {
            if (FEATURES[i].toLowerCase().contains(featureType.toLowerCase())) {
                featureCombo.setSelectedIndex(i);
                break;
            }
        }
        
        processInput();
    }
    
    /**
     * Quick definition lookup method
     */
    public void quickDefine(String word) {
        processText(word, "define");
    }
    
    /**
     * Quick translation method
     */
    public void quickTranslate(String text, String fromLang, String toLang) {
        inputField.setText(text);
        featureCombo.setSelectedItem("Translate Text");
        fromLanguageCombo.setSelectedItem(fromLang);
        toLanguageCombo.setSelectedItem(toLang);
        processInput();
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (translationService != null) {
            translationService.clearCaches();
        }
    }
}
