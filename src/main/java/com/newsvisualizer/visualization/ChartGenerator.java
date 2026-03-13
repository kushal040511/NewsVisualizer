package com.newsvisualizer.visualization;

import com.newsvisualizer.gui.theme.EnhancedModernTheme;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
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
        StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();
        theme.setExtraLargeFont(EnhancedModernTheme.Fonts.SECTION_TITLE);
        theme.setLargeFont(EnhancedModernTheme.Fonts.HEADING_MEDIUM);
        theme.setRegularFont(EnhancedModernTheme.Fonts.BODY_MEDIUM);
        theme.setSmallFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        theme.setPlotBackgroundPaint(EnhancedModernTheme.Colors.WHITE);
        theme.setChartBackgroundPaint(EnhancedModernTheme.Colors.WHITE);
        ChartFactory.setChartTheme(theme);
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
        return wrapChart(chart);
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
        return wrapChart(chart);
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
        return wrapChart(chart);
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
        plot.setSectionPaint("Positive", new Color(16, 185, 129));
        plot.setSectionPaint("Negative", new Color(239, 68, 68));
        plot.setSectionPaint("Neutral", new Color(99, 102, 241));
        
        customizePieChart(chart);
        return wrapChart(chart);
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
        renderer.setSeriesPaint(0, EnhancedModernTheme.Colors.PRIMARY);
        
        return wrapChart(chart);
    }
    
    /**
     * Customize general chart appearance
     */
    private static void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(EnhancedModernTheme.Colors.WHITE);
        chart.getTitle().setFont(EnhancedModernTheme.Fonts.HEADING_MEDIUM);
        chart.getTitle().setPaint(EnhancedModernTheme.Colors.TEXT_PRIMARY);
    }
    
    /**
     * Customize pie chart appearance
     */
    private static void customizePieChart(JFreeChart chart) {
        customizeChart(chart);
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(EnhancedModernTheme.Colors.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        plot.setLabelPaint(EnhancedModernTheme.Colors.TEXT_PRIMARY);
        plot.setLabelBackgroundPaint(EnhancedModernTheme.Colors.WHITE);
        plot.setLabelOutlinePaint(EnhancedModernTheme.Colors.BORDER_LIGHT);
        plot.setLabelShadowPaint(null);
        plot.setSectionOutlinesVisible(true);
        plot.setDefaultSectionOutlinePaint(EnhancedModernTheme.Colors.WHITE);
        plot.setShadowPaint(null);
        plot.setInteriorGap(0.08);
    }
    
    /**
     * Customize bar chart appearance
     */
    private static void customizeBarChart(JFreeChart chart) {
        customizeChart(chart);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(EnhancedModernTheme.Colors.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(EnhancedModernTheme.Colors.BORDER_LIGHT);
        plot.setDomainGridlinesVisible(false);
        if (plot.getDomainAxis() instanceof CategoryAxis categoryAxis) {
            categoryAxis.setTickLabelFont(EnhancedModernTheme.Fonts.BODY_SMALL);
            categoryAxis.setTickLabelPaint(EnhancedModernTheme.Colors.TEXT_SECONDARY);
            categoryAxis.setLabelFont(EnhancedModernTheme.Fonts.BODY_SMALL);
            categoryAxis.setLabelPaint(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        }
        if (plot.getRangeAxis() instanceof NumberAxis numberAxis) {
            numberAxis.setTickLabelFont(EnhancedModernTheme.Fonts.BODY_SMALL);
            numberAxis.setTickLabelPaint(EnhancedModernTheme.Colors.TEXT_SECONDARY);
            numberAxis.setLabelFont(EnhancedModernTheme.Fonts.BODY_SMALL);
            numberAxis.setLabelPaint(EnhancedModernTheme.Colors.TEXT_SECONDARY);
        }
        
        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setDefaultItemLabelFont(EnhancedModernTheme.Fonts.BODY_SMALL);
        renderer.setDefaultItemLabelPaint(EnhancedModernTheme.Colors.TEXT_PRIMARY);
        renderer.setSeriesPaint(0, EnhancedModernTheme.Colors.PRIMARY);
    }

    private static JPanel wrapChart(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setOpaque(false);
        panel.setMouseWheelEnabled(true);
        panel.setDomainZoomable(false);
        panel.setRangeZoomable(false);
        panel.setBackground(EnhancedModernTheme.Colors.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return panel;
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
