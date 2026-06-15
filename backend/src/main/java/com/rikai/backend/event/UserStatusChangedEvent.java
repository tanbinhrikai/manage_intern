package com.rikai.backend.event;

import org.springframework.context.ApplicationEvent;

import com.rikai.backend.model.Users;

import lombok.Getter;

@Getter
public class UserStatusChangedEvent extends ApplicationEvent{
    private final Users users;

    public UserStatusChangedEvent(Object source, Users users) {
        super(source);
        this.users = users;
    }
}
