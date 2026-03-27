package com.meetly.modules.events.presentation;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meetly.modules.events.application.EventCommandService;
import com.meetly.modules.events.application.EventQueryService;
import com.meetly.modules.events.application.command.CancelEventCommand;
import com.meetly.modules.events.application.command.CreateEventCommand;
import com.meetly.modules.events.application.query.GetEventDetailsQuery;
import com.meetly.modules.events.application.query.ListEventsQuery;
import com.meetly.modules.events.domain.Event;
import com.meetly.modules.events.presentation.mapper.EventResponseMapper;
import com.meetly.modules.events.presentation.request.CreateEventRequest;
import com.meetly.modules.events.presentation.response.EventDetailsResponse;
import com.meetly.modules.events.presentation.response.EventSummaryResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventCommandService eventCommandService;
    private final EventQueryService eventQueryService;
    private final EventResponseMapper eventResponseMapper;

    public EventController(
            EventCommandService eventCommandService,
            EventQueryService eventQueryService,
            EventResponseMapper eventResponseMapper
    ) {
        this.eventCommandService = eventCommandService;
        this.eventQueryService = eventQueryService;
        this.eventResponseMapper = eventResponseMapper;
    }

    @PostMapping
    public EventDetailsResponse createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String externalAuthId,
            @RequestHeader(value = "X-User-Username", required = false) String username
    ) {
        Event event = eventCommandService.createEvent(
                new CreateEventCommand(
                        resolveAuthIdentifier(externalAuthId, username),
                        request.title(),
                        request.description(),
                        request.locationText(),
                        request.scheduledAt(),
                        request.maxSlots(),
                        request.eventType(),
                        request.approvalRequired()
                )
        );

        return eventResponseMapper.toDetails(event);
    }

    @GetMapping
    public List<EventSummaryResponse> listEvents() {
        return eventQueryService.listEvents(new ListEventsQuery())
                .stream()
                .map(eventResponseMapper::toSummary)
                .toList();
    }

    @GetMapping("/{eventId}")
    public EventDetailsResponse getEvent(@PathVariable Long eventId) {
        return eventResponseMapper.toDetails(
                eventQueryService.getEventDetails(new GetEventDetailsQuery(eventId))
        );
    }

    @PatchMapping("/{eventId}/cancel")
    public void cancelEvent(
            @PathVariable Long eventId,
                        @RequestHeader(value = "X-User-Id", required = false) String externalAuthId,
                        @RequestHeader(value = "X-User-Username", required = false) String username
    ) {
                eventCommandService.cancelEvent(new CancelEventCommand(eventId, resolveAuthIdentifier(externalAuthId, username)));
        }

        private String resolveAuthIdentifier(String externalAuthId, String username) {
                if (externalAuthId != null && !externalAuthId.isBlank()) {
                        return externalAuthId;
                }

                if (username != null && !username.isBlank()) {
                        return username;
                }

                throw new com.meetly.shared.domain.DomainException("Missing authenticated user header");
    }
}