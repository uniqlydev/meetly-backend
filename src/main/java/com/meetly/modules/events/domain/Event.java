package com.meetly.modules.events.domain;

import java.time.OffsetDateTime;

import com.meetly.modules.identity.domain.User;
import com.meetly.shared.domain.BaseEntity;
import com.meetly.shared.domain.DomainException;

import jakarta.persistence.*;


@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "location_text", nullable = false, length = 180)
    private String locationText;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(name = "max_slots", nullable = false)
    private Integer maxSlots;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 20)
    private EventStatus eventStatus;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;

    protected Event() {
    }

    private Event(
            User host,
            String title,
            String description,
            String locationText,
            OffsetDateTime scheduledAt,
            Integer maxSlots,
            EventType eventType,
            boolean approvalRequired
    ) {
        this.host = host;
        this.title = title;
        this.description = description;
        this.locationText = locationText;
        this.scheduledAt = scheduledAt;
        this.maxSlots = maxSlots;
        this.eventType = eventType;
        this.eventStatus = EventStatus.ACTIVE;
        this.approvalRequired = approvalRequired;
    }

    public static Event create(
            User host,
            String title,
            String description,
            String locationText,
            OffsetDateTime scheduledAt,
            Integer maxSlots,
            EventType eventType,
            boolean approvalRequired
    ) {
        if (scheduledAt.isBefore(OffsetDateTime.now())) {
            throw new DomainException("Event must be scheduled in the future");
        }

        return new Event(
                host,
                title,
                description,
                locationText,
                scheduledAt,
                maxSlots,
                eventType,
                approvalRequired
        );
    }

    public void cancel(Long actingUserId) {
        if (!host.getId().equals(actingUserId)) {
            throw new DomainException("Only the host can cancel the event");
        }

        if (eventStatus != EventStatus.ACTIVE) {
            throw new DomainException("Only active events can be cancelled");
        }

        this.eventStatus = EventStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public User getHost() {
        return host;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocationText() {
        return locationText;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public Integer getMaxSlots() {
        return maxSlots;
    }

    public EventType getEventType() {
        return eventType;
    }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }
}