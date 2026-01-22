package com.rikai.backend.dto.response.weeklyreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyReportDetailResponse {
    private Long id;
    private Long criteriaId;
    private String criteriaName;
    private BigDecimal score;
    private String comment;
}
