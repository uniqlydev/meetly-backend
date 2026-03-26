package com.meetly.modules.events.application.command;

public record CancelEventCommand(
        Long eventId,
        String externalAuthId
) {
}