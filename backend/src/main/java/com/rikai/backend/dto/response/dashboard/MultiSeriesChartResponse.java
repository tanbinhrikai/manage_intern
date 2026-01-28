package com.rikai.backend.dto.response.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MultiSeriesChartResponse {
    List<String> labels; // X-axis labels (e.g., months)
    List<Series> series; // Multiple data series
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Series {
        String name;   // Series name (e.g., position/department name)
        List<Double> data; // Values for each label
    }
}
