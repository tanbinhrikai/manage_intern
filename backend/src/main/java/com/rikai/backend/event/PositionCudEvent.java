package com.rikai.backend.event;

import org.springframework.context.ApplicationEvent;

import com.rikai.backend.model.Position;

import lombok.Getter;

@Getter
public class PositionCudEvent extends ApplicationEvent {
    private final String eventType;
    private final Position position;
    
    public PositionCudEvent(Object source, String eventType, Position position) {
        super(source);
        this.eventType = eventType;
        this.position = position;
    }
    
}
