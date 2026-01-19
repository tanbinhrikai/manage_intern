package com.rikai.backend.model.Enum;

import lombok.Getter;

@Getter
public enum ScoreLabel {
    Excellent(9 , 10 ),
    Good(7 ,8),
    Average(5 ,6),
    Weak(0 ,4);

    ScoreLabel(int minScore  , int maxScore){
        this.minScore = minScore;
        this.maxScore = maxScore;
    }
    private  final int minScore;
    private  final int  maxScore;
}
