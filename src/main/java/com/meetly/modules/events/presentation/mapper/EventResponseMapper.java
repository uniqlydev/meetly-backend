package com.meetly.modules.events.presentation.mapper;

import org.springframework.stereotype.Component;

import com.meetly.modules.events.domain.Event;
import com.meetly.modules.events.presentation.response.EventDetailsResponse;
import com.meetly.modules.events.presentation.response.EventSummaryResponse;

@Component
public class EventResponseMapper {

    public EventSummaryResponse toSummary(Event event) {
        return EventSummaryResponse.from(event);
    }

    public EventDetailsResponse toDetails(Event event) {
        return EventDetailsResponse.from(event);
    }
}