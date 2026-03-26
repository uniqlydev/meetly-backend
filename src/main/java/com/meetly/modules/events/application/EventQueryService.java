package com.meetly.modules.events.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meetly.modules.events.application.query.GetEventDetailsQuery;
import com.meetly.modules.events.application.query.ListEventsQuery;
import com.meetly.modules.events.domain.Event;
import com.meetly.modules.events.domain.EventRepository;
import com.meetly.shared.domain.DomainException;

@Service
public class EventQueryService {

    private final EventRepository eventRepository;

    public EventQueryService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<Event> listEvents(ListEventsQuery query) {
        return eventRepository.findAllActiveUpcoming();
    }

    @Transactional(readOnly = true)
    public Event getEventDetails(GetEventDetailsQuery query) {
        return eventRepository.findById(query.eventId())
                .orElseThrow(() -> new DomainException("Event not found"));
    }
}