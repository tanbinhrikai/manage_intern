package com.rikai.backend.model.Enum;

import lombok.Getter;

@Getter
public enum ScoreLabel {
    EXCELLENT(9 , 10 ),
    GOOD(7 ,8),
    AVERAGE(5 ,6),
    WEAK(0 ,4);

    private final int minScore;
    private final int maxScore;


    ScoreLabel(int minScore, int maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
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
