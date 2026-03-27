package com.meetly.modules.events.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meetly.modules.events.application.command.CancelEventCommand;
import com.meetly.modules.events.application.command.CreateEventCommand;
import com.meetly.modules.events.domain.Event;
import com.meetly.modules.events.domain.EventRepository;
import com.meetly.modules.identity.domain.User;
import com.meetly.modules.identity.domain.UserRepository;
import com.meetly.shared.domain.DomainException;

@Service
public class EventCommandService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventCommandService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Event createEvent(CreateEventCommand command) {
        User host = resolveUser(command.externalAuthId())
            .orElseThrow(() -> new DomainException("Host user not found"));

        Event event = Event.create(
                host,
                command.title(),
                command.description(),
                command.locationText(),
                command.scheduledAt(),
                command.maxSlots(),
                command.eventType(),
                command.approvalRequired()
        );

        return eventRepository.save(event);
    }

    @Transactional
    public void cancelEvent(CancelEventCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new DomainException("Event not found"));

        User actingUser = resolveUser(command.externalAuthId())
            .orElseThrow(() -> new DomainException("User not found"));

        event.cancel(actingUser.getId());
    }

        private java.util.Optional<User> resolveUser(String authIdentifier) {
        return userRepository.findByExternalAuthId(authIdentifier)
            .or(() -> userRepository.findByUsername(authIdentifier));
        }
}