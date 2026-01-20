package com.rikai.backend.model.Enum;

import lombok.Getter;

@Getter
public enum ScoreLabel {
    EXCELLENT(9, 10, "Excellent", "Excellent grade (9 - 10)"),
    GOOD(7, 8, "Good", "Good grade (7 - 8)"),
    AVERAGE(5, 6, "Average", "Average grade (5 - 6)"),
    WEAK(0, 4, "Weak", "Weak grade (<5)");

    private final int minScore;
    private final int maxScore;
    private final String displayName;
    private final String description;

    ScoreLabel(int minScore, int maxScore, String displayName, String description) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Pick appropriate ScoreLabel from given score
     */
    public static ScoreLabel fromScore(int score) {
        for (ScoreLabel label : values()) {
            if (score >= label.minScore && score <= label.maxScore) {
                return label;
            }
        }
        return WEAK;
    }
}
