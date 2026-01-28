package com.rikai.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EvaluationSessionDeletedEvent extends ApplicationEvent {
    private final Integer sessionId;
    private final String internName;
    private final String mentorName;
    private final String sessionType;
    
    public EvaluationSessionDeletedEvent(Object source, Integer sessionId, String internName, String mentorName, String sessionType) {
        super(source);
        this.sessionId = sessionId;
        this.internName = internName;
        this.mentorName = mentorName;
        this.sessionType = sessionType;
    }
}
