package com.meetly.modules.auth.application;

public interface UserConfirmedEventPublisher {
    void publish(UserConfirmedEvent event);
}