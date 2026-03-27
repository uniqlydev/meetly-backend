package com.meetly.modules.auth.presentation.response;

import java.time.OffsetDateTime;

public record AuthUserResponse(
        Long id,
        String externalAuthId,
        String username,
        String email,
        String name,
        String bio,
        String profileImageUrl,
        boolean verified,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}