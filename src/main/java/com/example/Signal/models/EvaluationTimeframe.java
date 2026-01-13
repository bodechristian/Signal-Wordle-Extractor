package com.example.Signal.models;

import lombok.Getter;

import java.time.LocalDate;

/**
 * Enum representing available timeframe options for filtering evaluation statistics by date range.
 * Each timeframe defines a display name and the number of days to look back from today.
 */
@Getter
public enum EvaluationTimeframe {
    ALL_TIME("All time", Integer.MAX_VALUE),
    LAST_7_DAYS("Last 7 days", 7),
    LAST_30_DAYS("Last 30 days", 30),
    LAST_90_DAYS("Last 90 days", 90),
    LAST_365_DAYS("Last 365 days", 365);
    
    private final String displayName;
    private final int days;
    
    EvaluationTimeframe(String displayName, int days) {
        this.displayName = displayName;
        this.days = days;
    }
    
    /**
     * Calculates the cutoff date for this timeframe.
     * @return The cutoff date to filter messages
     */
    public LocalDate getCutoffDate() {
        return LocalDate.now().minusDays(days);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
