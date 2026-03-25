package com.meetly.modules.identity.presentation.response;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String bio,
        String profileImageUrl,
        boolean verified
) {
}