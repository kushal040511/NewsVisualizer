package com.newsvisualizer.visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Utility class for generating various types of charts for news data visualization
 */
public class ChartGenerator {
    
    // Apply modern theme to charts
    static {
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
    }
    
    /**
     * Create a pie chart for data distribution
     */
    public static JPanel createPieChart(String title, Map<String, Integer> data) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true, // legend
                true, // tooltips
                false // URLs
        );
        
        customizePieChart(chart);
        return new ChartPanel(chart);
    }
    
    /**
     * Create a bar chart for categorical data
     */
    public static JPanel createBarChart(String title, String categoryAxisLabel, 
                                       String valueAxisLabel, Map<String, Integer> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Count", entry.getKey());
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                categoryAxisLabel,
                valueAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                false, // legend
                true, // tooltips
                false // URLs
        );
        
        customizeBarChart(chart);
        return new ChartPanel(chart);
    }
    
    /**
     * Create a horizontal bar chart for categorical data with long labels
     */
    public static JPanel createHorizontalBarChart(String title, String categoryAxisLabel, 
                                                 String valueAxisLabel, Map<String, Integer> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        int count = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            if (count >= 10) break; // Limit to top 10 items
            String label = entry.getKey().length() > 30 ? 
                entry.getKey().substring(0, 30) + "..." : entry.getKey();
            dataset.addValue(entry.getValue(), "Count", label);
            count++;
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                valueAxisLabel,
                categoryAxisLabel,
                dataset,
                PlotOrientation.HORIZONTAL,
                false, // legend
                true, // tooltips
                false // URLs
        );
        
        customizeBarChart(chart);
        return new ChartPanel(chart);
    }
    
    // Timeline chart functionality removed
    
    /**
     * Create a sentiment analysis chart (special pie chart with sentiment colors)
     */
    public static JPanel createSentimentChart(String title, Map<String, Integer> sentimentData) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        for (Map.Entry<String, Integer> entry : sentimentData.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true, // legend
                true, // tooltips
                false // URLs
        );
        
        // Custom colors for sentiment
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Positive", new Color(76, 175, 80)); // Green
        plot.setSectionPaint("Negative", new Color(244, 67, 54)); // Red
        plot.setSectionPaint("Neutral", new Color(158, 158, 158)); // Gray
        
        customizePieChart(chart);
        return new ChartPanel(chart);
    }
    
    /**
     * Create a word frequency chart (horizontal bar chart for keywords)
     */
    public static JPanel createWordFrequencyChart(String title, Map<String, Integer> keywords) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        int count = 0;
        for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
            if (count >= 15) break; // Limit to top 15 keywords
            dataset.addValue(entry.getValue(), "Frequency", entry.getKey());
            count++;
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Frequency",
                "Keywords",
                dataset,
                PlotOrientation.HORIZONTAL,
                false, // legend
                true, // tooltips
                false // URLs
        );
        
        customizeBarChart(chart);
        
        // Color bars with gradient
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(33, 150, 243)); // Blue
        
        return new ChartPanel(chart);
    }
    
    /**
     * Customize general chart appearance
     */
    private static void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        chart.getTitle().setPaint(Color.BLACK);
    }
    
    /**
     * Customize pie chart appearance
     */
    private static void customizePieChart(JFreeChart chart) {
        customizeChart(chart);
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        plot.setLabelPaint(Color.BLACK);
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(Color.LIGHT_GRAY);
        plot.setLabelShadowPaint(null);
        plot.setSectionOutlinesVisible(true);
        plot.setDefaultSectionOutlinePaint(Color.WHITE);
    }
    
    /**
     * Customize bar chart appearance
     */
    private static void customizeBarChart(JFreeChart chart) {
        customizeChart(chart);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinesVisible(false);
        
        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setDefaultItemLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        renderer.setDefaultItemLabelPaint(Color.BLACK);
        renderer.setSeriesPaint(0, new Color(63, 81, 181)); // Indigo
    }
    
    /**
     * Create a summary statistics panel
     */
    public static JPanel createSummaryPanel(int totalArticles, double avgSentiment, 
                                          int uniqueSources, String topKeyword) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Summary Statistics"));
        panel.setBackground(Color.WHITE);
        
        // Total Articles
        JPanel totalPanel = createStatPanel("Total Articles", String.valueOf(totalArticles), Color.BLUE);
        panel.add(totalPanel);
        
        // Average Sentiment
        String sentimentText = String.format("%.3f (%s)", avgSentiment, 
            avgSentiment > 0.1 ? "Positive" : avgSentiment < -0.1 ? "Negative" : "Neutral");
        Color sentimentColor = avgSentiment > 0.1 ? Color.GREEN : avgSentiment < -0.1 ? Color.RED : Color.GRAY;
        JPanel sentimentPanel = createStatPanel("Avg Sentiment", sentimentText, sentimentColor);
        panel.add(sentimentPanel);
        
        // Unique Sources
        JPanel sourcesPanel = createStatPanel("Unique Sources", String.valueOf(uniqueSources), Color.ORANGE);
        panel.add(sourcesPanel);
        
        // Top Keyword
        JPanel keywordPanel = createStatPanel("Top Keyword", topKeyword != null ? topKeyword : "N/A", Color.MAGENTA);
        panel.add(keywordPanel);
        
        return panel;
    }
    
    /**
     * Create a single statistic panel
     */
    private static JPanel createStatPanel(String label, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(color, 2));
        
        JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
        labelLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        labelLabel.setForeground(Color.GRAY);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        valueLabel.setForeground(color);
        
        panel.add(labelLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
}