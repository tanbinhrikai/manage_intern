package com.rikai.backend.event;

import org.springframework.context.ApplicationEvent;

import com.rikai.backend.model.InternshipBatch;

import lombok.Getter;

@Getter
public class InternShipBatchCudEvent extends ApplicationEvent {
    String eventType;
    InternshipBatch internshipBatch;

    public InternShipBatchCudEvent(Object source, String eventType, InternshipBatch internshipBatch) {
        super(source);
        this.eventType = eventType;
        this.internshipBatch = internshipBatch;
    }
}
