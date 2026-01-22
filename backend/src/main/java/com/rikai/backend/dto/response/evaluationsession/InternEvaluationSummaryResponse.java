package com.rikai.backend.dto.response.evaluationsession;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InternEvaluationSummaryResponse {
    private Long internId;
    private String internName;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Weekly reports summary
    private Integer totalWeeklyReports;
    private BigDecimal averageWeeklyScore;
    
    // Evaluation sessions
    private EvaluationSessionResponse firstTermSession;
    private EvaluationSessionResponse midTermSession;
    private EvaluationSessionResponse finalSession;
    
    // Overall assessment
    private BigDecimal overallFinalScore;
    private String overallLevel;
    private String overallConclusion;
    
    // Score trend data (for charts)
    private List<WeeklyScoreData> weeklyScoreTrend;
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WeeklyScoreData {
        private LocalDate weekStartDate;
        private BigDecimal averageScore;
    }
}
