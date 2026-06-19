package com.rikai.backend.event;

import org.springframework.context.ApplicationEvent;

import com.rikai.backend.model.Intern;

import lombok.Getter;

@Getter
public class InternCudEvent extends ApplicationEvent {
    private String eventType;
    private Intern intern;

    public InternCudEvent(Object source, String eventType, Intern intern) {
        super(source);
        this.eventType = eventType;
        this.intern = intern;
    }
}
