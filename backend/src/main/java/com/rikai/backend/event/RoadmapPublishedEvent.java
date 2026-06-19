package com.rikai.backend.event;

import org.springframework.context.ApplicationEvent;
import com.rikai.backend.model.Roadmap;
import lombok.Getter;

@Getter
public class RoadmapPublishedEvent extends ApplicationEvent {
    private final Roadmap roadmap;
    
    public RoadmapPublishedEvent(Object source, Roadmap roadmap) {
        super(source);
        this.roadmap = roadmap;
    }
}
