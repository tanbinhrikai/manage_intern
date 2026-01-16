package com.rikai.backend.dto.response.weeklyreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyReportDetailResponse {
    private Integer id;
    private Integer criteriaId;
    private String criteriaName;
    private Byte score;
    private String comment;
}
