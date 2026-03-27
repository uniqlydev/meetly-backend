package com.meetly.modules.events.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.meetly.modules.events.application.command.CreateEventCommand;
import com.meetly.modules.events.domain.Event;
import com.meetly.modules.events.domain.EventRepository;
import com.meetly.modules.events.domain.EventType;
import com.meetly.modules.identity.domain.User;
import com.meetly.modules.identity.domain.UserRepository;
import com.meetly.shared.domain.DomainException;

@ExtendWith(MockitoExtension.class)
class EventCommandServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventCommandService eventCommandService;

    @Test
    void createEventFallsBackToUsernameLookup() {
        User host = User.create("cognito-sub-123", "brendawg", "brendan@example.com", "Brendan Castillo");
        CreateEventCommand command = new CreateEventCommand(
                "brendawg",
                "F1 Watch Party",
                "Come watch qualifying",
                "Makati",
                OffsetDateTime.now().plusDays(1),
                8,
                EventType.PUBLIC_PLACE,
                false
        );

        when(userRepository.findByExternalAuthId("brendawg")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("brendawg")).thenReturn(Optional.of(host));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event created = eventCommandService.createEvent(command);

        assertEquals("F1 Watch Party", created.getTitle());
        assertEquals("brendawg", created.getHost().getUsername());
        verify(userRepository).findByUsername("brendawg");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEventThrowsWhenHostCannotBeResolved() {
        CreateEventCommand command = new CreateEventCommand(
                "missing-user",
                "Coffee Hangout",
                "Morning coffee",
                "BGC",
                OffsetDateTime.now().plusDays(1),
                4,
                EventType.PUBLIC_PLACE,
                true
        );

        when(userRepository.findByExternalAuthId("missing-user")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("missing-user")).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> eventCommandService.createEvent(command));
    }
}