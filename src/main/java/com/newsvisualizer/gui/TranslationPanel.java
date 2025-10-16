package com.newsvisualizer.gui;

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
    
    private final TranslationService translationService;
    
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
        "🔍 Define Word/Phrase",
        "🌐 Translate Text", 
        "📝 Explain Sentence",
        "🔤 Detect Language",
        "📚 Find Synonyms"
    };
    
    // Supported languages
    private static final String[] LANGUAGES = {
        "English", "Hindi", "Chinese (Simplified)", "Chinese (Traditional)", "Japanese", "Spanish", "French", "German", "Italian", "Auto-detect"
    };
    
    public TranslationPanel() {
        this.translationService = new TranslationService();
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(5, 10, 5, 10)); // Much more compact padding
        setBackground(new Color(248, 249, 250));
        
        // Input field with optimal sizing
        inputField = new JTextField(35);
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        inputField.setToolTipText("Enter word, phrase, or sentence to analyze");
        
        // Feature selection
        featureCombo = new JComboBox<>(FEATURES);
        featureCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        featureCombo.setBackground(Color.WHITE);
        featureCombo.setToolTipText("Select what you want to do with your text");
        
        // Language selection
        fromLanguageCombo = new JComboBox<>(LANGUAGES);
        fromLanguageCombo.setSelectedItem("Auto-detect");
        fromLanguageCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fromLanguageCombo.setBackground(Color.WHITE);
        
        toLanguageCombo = new JComboBox<>(LANGUAGES);
        toLanguageCombo.setSelectedItem("Hindi");
        toLanguageCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        toLanguageCombo.setBackground(Color.WHITE);
        
        // Translation panel (initially hidden)
        translationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        translationPanel.add(new JLabel("From:"));
        translationPanel.add(fromLanguageCombo);
        translationPanel.add(new JLabel("To:"));
        translationPanel.add(toLanguageCombo);
        translationPanel.setVisible(false);
        translationPanel.setBackground(new Color(248, 249, 250));
        
        // Buttons
        processButton = createStyledButton("🔍 Process", new Color(34, 197, 94), Color.WHITE);
        processButton.setToolTipText("Click to process your input");
        
        clearButton = createStyledButton("🗑️ Clear", new Color(107, 114, 128), Color.WHITE);
        clearButton.setToolTipText("Clear all fields");
        
        historyButton = createStyledButton("📝 Examples", new Color(59, 130, 246), Color.WHITE);
        historyButton.setToolTipText("View example usage");
        
        // Result area with compact dimensions
        resultArea = new JTextArea(8, 40); // Reduced from 14x50 to 8x40
        resultArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);
        resultArea.setBackground(Color.WHITE);
        resultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String welcomeText = "👋 Welcome to the Translation & Dictionary Tool!\n\n" +
                           "Enter any word or phrase you don't understand from news articles.\n" +
                           "Choose what you want to do from the dropdown above and click Process.\n\n" +
                           "Examples:\n" +
                           "• Type 'inflation' and select 'Define Word' to get its meaning\n" +
                           "• Type 'breaking news' to understand the phrase\n" +
                           "• Type text in Hindi and detect its language\n" +
                           "• Translate common news terms to Hindi";
        resultArea.setText(welcomeText);
        
        // Status label
        statusLabel = new JLabel("Ready to help you understand news content 📰");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(107, 114, 128));
    }
    
    private void layoutComponents() {
        // Top panel - Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(248, 249, 250));
        JLabel titleLabel = new JLabel("📖 Translation & Dictionary Helper");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(31, 41, 55));
        titlePanel.add(titleLabel);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(new Color(248, 249, 250));
        inputPanel.setBorder(new TitledBorder("Enter Text to Analyze"));
        
        JPanel topInputPanel = new JPanel(new BorderLayout(5, 5));
        topInputPanel.setBackground(new Color(248, 249, 250));
        topInputPanel.add(inputField, BorderLayout.CENTER);
        topInputPanel.add(featureCombo, BorderLayout.EAST);
        
        inputPanel.add(topInputPanel, BorderLayout.NORTH);
        inputPanel.add(translationPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.add(processButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(historyButton);
        
        // Result panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(new TitledBorder("Result"));
        resultPanel.setBackground(new Color(248, 249, 250));
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(248, 249, 250));
        statusPanel.add(statusLabel);
        
        // Main layout
        add(titlePanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(new Color(248, 249, 250));
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
                    statusLabel.setText("✅ Processing complete");
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
                return "📖 Definition:\n\n" + translationService.getDefinition(input);
            } else if (feature.contains("Translate")) {
                String fromLang = (String) fromLanguageCombo.getSelectedItem();
                String toLang = (String) toLanguageCombo.getSelectedItem();
                return "🌐 Translation:\n\n" + translationService.translateText(input, fromLang, toLang);
            } else if (feature.contains("Explain")) {
                return "📝 Explanation:\n\n" + translationService.explainPhrase(input);
            } else if (feature.contains("Detect")) {
                return "🔤 " + translationService.detectLanguage(input);
            } else if (feature.contains("Synonyms")) {
                return "📚 " + translationService.getSynonyms(input);
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
    
    private void clearAll() {
        inputField.setText("");
        resultArea.setText("Ready for new input...\n\nTip: Select a feature from the dropdown and enter your text.");
        statusLabel.setText("Cleared. Ready for new input 🆕");
        statusLabel.setForeground(new Color(107, 114, 128));
        inputField.requestFocus();
    }
    
    private void showExamples() {
        String examples = "📚 USAGE EXAMPLES\n\n" +
                         "🔍 DEFINE WORD/PHRASE:\n" +
                         "• inflation → Get economic definition\n" +
                         "• breaking news → Understand news terminology\n" +
                         "• constituency → Learn political terms\n" +
                         "• cryptocurrency → Tech definitions\n\n" +
                         
                         "🌐 TRANSLATE TEXT:\n" +
                         "• news → समाचार (Hindi)\n" +
                         "• government → सरकार (Hindi)\n" +
                         "• Select languages from dropdowns\n\n" +
                         
                         "📝 EXPLAIN SENTENCE:\n" +
                         "• 'According to sources' → News phrase explanation\n" +
                         "• 'Developing story' → What this means\n" +
                         "• Complex sentences → Simplified explanations\n\n" +
                         
                         "🔤 DETECT LANGUAGE:\n" +
                         "• Mixed language text → Identify language\n" +
                         "• Unknown script → Language detection\n\n" +
                         
                         "📚 FIND SYNONYMS:\n" +
                         "• good → excellent, great, fine, wonderful\n" +
                         "• important → significant, crucial, vital\n\n" +
                         
                         "💡 TIPS:\n" +
                         "• Double-click words in news articles to quickly look them up\n" +
                         "• Use this tool while reading to improve comprehension\n" +
                         "• Cache remembers recent lookups for faster access";
        
        resultArea.setText(examples);
        statusLabel.setText("📚 Showing usage examples");
        statusLabel.setForeground(new Color(59, 130, 246));
    }
    
    private void updatePlaceholderText(String feature) {
        if (feature == null) return;
        
        if (feature.contains("Define")) {
            inputField.setToolTipText("Enter a word or phrase to get its definition (e.g., 'inflation', 'breaking news')");
        } else if (feature.contains("Translate")) {
            inputField.setToolTipText("Enter text to translate between languages (e.g., 'news', 'government')");
        } else if (feature.contains("Explain")) {
            inputField.setToolTipText("Enter a sentence or phrase to get an explanation (e.g., 'according to sources')");
        } else if (feature.contains("Detect")) {
            inputField.setToolTipText("Enter text to detect its language (works with multiple languages)");
        } else if (feature.contains("Synonyms")) {
            inputField.setToolTipText("Enter a word to find similar words (e.g., 'good', 'important')");
        }
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 40));
        
        // Hover effect
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
        featureCombo.setSelectedItem("🌐 Translate Text");
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