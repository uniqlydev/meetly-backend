package com.meetly.modules.auth.application;

import java.time.OffsetDateTime;

public record UserConfirmedEvent(
        String eventId,
        String externalAuthId,
        String username,
        String email,
        String givenName,
        String familyName,
        OffsetDateTime confirmedAt,
        int schemaVersion
) {
}