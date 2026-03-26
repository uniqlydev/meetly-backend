package com.meetly.modules.events.application.command;

import java.time.OffsetDateTime;

import com.meetly.modules.events.domain.EventType;

public record CreateEventCommand(
        String externalAuthId,
        String title,
        String description,
        String locationText,
        OffsetDateTime scheduledAt,
        Integer maxSlots,
        EventType eventType,
        boolean approvalRequired
) {
}