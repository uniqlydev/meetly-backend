package com.meetly.modules.events.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.meetly.modules.events.domain.Event;
import com.meetly.modules.events.domain.EventRepository;
import com.meetly.modules.events.domain.EventStatus;

interface SpringDataEventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventStatusAndScheduledAtAfterOrderByScheduledAtAsc(
            EventStatus eventStatus,
            OffsetDateTime scheduledAt
    );
}

@Repository
public class JpaEventRepository implements EventRepository {

    private final SpringDataEventRepository springDataEventRepository;

    public JpaEventRepository(SpringDataEventRepository springDataEventRepository) {
        this.springDataEventRepository = springDataEventRepository;
    }

    @Override
    public Event save(Event event) {
        return springDataEventRepository.save(event);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return springDataEventRepository.findById(id);
    }

    @Override
    public List<Event> findAllActiveUpcoming() {
        return springDataEventRepository.findByEventStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                EventStatus.ACTIVE,
                OffsetDateTime.now()
        );
    }
}