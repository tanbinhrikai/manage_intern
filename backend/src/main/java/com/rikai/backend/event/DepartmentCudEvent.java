package com.rikai.backend.event;

import org.springframework.context.ApplicationEvent;

import com.rikai.backend.model.Department;

import lombok.Getter;

@Getter
public class DepartmentCudEvent extends ApplicationEvent {
    private String eventType;
    private Department department;

    public DepartmentCudEvent(Object source, String eventType, Department department) {
        super(source);
        this.eventType = eventType;
        this.department = department;
    }
}
