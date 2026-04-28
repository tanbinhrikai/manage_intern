package com.rikai.backend.event;

import com.rikai.backend.model.EvaluationSession;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EvaluationSessionUpdatedEvent extends ApplicationEvent {
    private final EvaluationSession evaluationSession;
    
    public EvaluationSessionUpdatedEvent(Object source, EvaluationSession evaluationSession) {
        super(source);
        this.evaluationSession = evaluationSession;
    }
}
