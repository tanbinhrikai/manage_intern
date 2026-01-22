package com.rikai.backend.dto.response.weeklyreport;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeeklyReportDetailResponse {
    private Long id;
    private Long criteriaId;
    private String criteriaName;
    private Byte score;
    private String comment;
}
