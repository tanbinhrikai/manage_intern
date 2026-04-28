package com.rikai.backend.event;

import com.rikai.backend.model.Intern;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InternCreatedEvent extends ApplicationEvent {
    private final Intern intern;
    
    public InternCreatedEvent(Object source, Intern intern) {
        super(source);
        this.intern = intern;
    }
}
