package com.rikai.backend.event;

import com.rikai.backend.model.InternStatusHistory;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InternStatusChangedEvent extends ApplicationEvent {
    private final InternStatusHistory statusHistory;
    
    public InternStatusChangedEvent(Object source, InternStatusHistory statusHistory) {
        super(source);
        this.statusHistory = statusHistory;
    }
}
