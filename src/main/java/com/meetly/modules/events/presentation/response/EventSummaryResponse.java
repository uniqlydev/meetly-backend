package com.meetly.modules.events.presentation.response;

import java.time.OffsetDateTime;

import com.meetly.modules.events.domain.Event;
import com.meetly.modules.events.domain.EventStatus;
import com.meetly.modules.events.domain.EventType;

public record EventSummaryResponse(
        Long id,
        String title,
        String description,
        String locationText,
        OffsetDateTime scheduledAt,
        Integer maxSlots,
        EventType eventType,
        EventStatus eventStatus,
        boolean approvalRequired,
        Long hostId,
        String hostName
) {
    public static EventSummaryResponse from(Event event) {
        return new EventSummaryResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocationText(),
                event.getScheduledAt(),
                event.getMaxSlots(),
                event.getEventType(),
                event.getEventStatus(),
                event.isApprovalRequired(),
                event.getHost().getId(),
                event.getHost().getName()
        );
    }
}