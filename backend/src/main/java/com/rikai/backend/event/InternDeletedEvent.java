package com.rikai.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InternDeletedEvent extends ApplicationEvent {
    private final Long internId;
    private final String internName;
    
    public InternDeletedEvent(Object source, Long internId, String internName) {
        super(source);
        this.internId = internId;
        this.internName = internName;
    }
}
