package com.meetly.modules.events.presentation.request;

import java.time.OffsetDateTime;

import com.meetly.modules.events.domain.EventType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEventRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 500) String description,
        @NotBlank @Size(max = 180) String locationText,
        @NotNull OffsetDateTime scheduledAt,
        @NotNull @Min(2) @Max(20) Integer maxSlots,
        @NotNull EventType eventType,
        boolean approvalRequired
) {
}