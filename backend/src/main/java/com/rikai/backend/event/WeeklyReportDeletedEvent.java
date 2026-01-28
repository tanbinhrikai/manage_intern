package com.rikai.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WeeklyReportDeletedEvent extends ApplicationEvent {
    private final Integer reportId;
    private final String internName;
    private final String mentorName;
    
    public WeeklyReportDeletedEvent(Object source, Integer reportId, String internName, String mentorName) {
        super(source);
        this.reportId = reportId;
        this.internName = internName;
        this.mentorName = mentorName;
    }
}
