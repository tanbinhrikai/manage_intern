package com.rikai.backend.event;

import com.rikai.backend.model.WeeklyReport;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WeeklyReportUpdatedEvent extends ApplicationEvent {
    private final WeeklyReport weeklyReport;
    
    public WeeklyReportUpdatedEvent(Object source, WeeklyReport weeklyReport) {
        super(source);
        this.weeklyReport = weeklyReport;
    }
}
