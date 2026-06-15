package com.rikai.backend.event;

import org.springframework.context.ApplicationEvent;

import com.rikai.backend.model.Users;

import lombok.Getter;

@Getter
public class UserCudEvent extends ApplicationEvent {
    private String eventType;
    private Users user;

    public UserCudEvent(Object source, String eventType, Users user) {
        super(source);
        this.eventType = eventType;
        this.user = user;  
    }

}