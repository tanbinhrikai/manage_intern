package com.rikai.backend.ai.event;

import com.rikai.backend.model.WeeklyReport;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class ReportSavedEvent extends ApplicationEvent {

    private final WeeklyReport report;

    public ReportSavedEvent(Object source, WeeklyReport report) {
        super(source);
        this.report = report;
    }
}
