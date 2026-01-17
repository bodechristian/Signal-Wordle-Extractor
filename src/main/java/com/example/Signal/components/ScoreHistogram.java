package com.example.Signal.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom histogram component that displays the distribution of Wordle scores for a person
 */
public class ScoreHistogram extends VerticalLayout {
    
    private static final int MAX_BAR_HEIGHT = 200; // pixels
    
    public ScoreHistogram(String personName, List<Integer> scores) {
        this.addClassName("score-histogram");
        this.setWidth("320px"); // Fixed width to prevent stretching
        
        // Title
        H4 title = new H4(personName);
        title.addClassName("score-histogram__title");
        this.add(title);
        
        // Count scores
        Map<Integer, Integer> scoreCounts = countScores(scores);
        int maxCount = scoreCounts.values().stream().max(Integer::compareTo).orElse(1);
        
        // Calculate statistics
        int totalGames = scores.size();
        long missCount = scores.stream().filter(s -> s == 7).count();
        double missPercentage = totalGames > 0 ? (missCount * 100.0 / totalGames) : 0;
        
        // Calculate average excluding misses (score 7)
        double avgScore = scores.stream()
                .filter(s -> s != 7)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        
        // Stats line
        Span statsSpan = new Span(String.format("Games: %d | Avg: %.2f | Miss: %.1f%%", 
                totalGames, avgScore, missPercentage));
        statsSpan.addClassName("score-histogram__stats");
        this.add(statsSpan);
        
        // Create chart area with bars and labels combined
        VerticalLayout chartContainer = new VerticalLayout();
        chartContainer.addClassName("score-histogram__chart-container");
        
        // Create chart area for bars
        HorizontalLayout chartArea = new HorizontalLayout();
        chartArea.addClassName("score-histogram__chart-area");
        
        // Create bars for scores 1-7
        for (int score = 1; score <= 7; score++) {
            int count = scoreCounts.getOrDefault(score, 0);
            VerticalLayout bar = createBar(score, count, maxCount);
            chartArea.add(bar);
        }
        
        chartContainer.add(chartArea);
        this.add(chartContainer);
    }
    
    /**
     * Counts occurrences of each score
     */
    private Map<Integer, Integer> countScores(List<Integer> scores) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int score : scores) {
            counts.put(score, counts.getOrDefault(score, 0) + 1);
        }
        return counts;
    }
    
    /**
     * Creates a single bar for the histogram with its label
     */
    private VerticalLayout createBar(int score, int count, int maxCount) {
        VerticalLayout barContainer = new VerticalLayout();
        barContainer.addClassName("score-histogram__bar-container");
        barContainer.setWidth("32px"); // Match CSS width
        barContainer.setPadding(false);
        barContainer.setSpacing(false);
        barContainer.setAlignItems(Alignment.CENTER);
        
        // Count label on top
        Span countLabel = new Span(count > 0 ? String.valueOf(count) : "");
        countLabel.addClassName("score-histogram__count-label");
        
        // Bar
        Div bar = new Div();
        bar.addClassNames("score-histogram__bar", "score-histogram__bar--score-" + score);
        
        // Calculate bar height proportional to count
        int barHeight = maxCount > 0 ? (count * MAX_BAR_HEIGHT) / maxCount : 0;
        if (count > 0 && barHeight < 5) {
            barHeight = 5; // Minimum height for visibility
        }
        bar.setHeight(barHeight + "px");
        bar.setWidth("32px"); // Match CSS width
        
        // Add spacer to push bar to bottom
        Div spacer = new Div();
        spacer.setHeight((MAX_BAR_HEIGHT - barHeight) + "px");
        spacer.setWidth("32px"); // Match CSS width
        
        // X-axis label at the bottom
        Span xLabel = new Span(score == 7 ? "X" : String.valueOf(score));
        xLabel.addClassName("score-histogram__x-label");
        
        barContainer.add(countLabel, spacer, bar, xLabel);
        
        return barContainer;
    }
}
