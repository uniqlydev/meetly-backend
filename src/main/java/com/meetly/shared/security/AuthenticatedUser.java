package com.meetly.shared.security;

public record AuthenticatedUser(
        String externalAuthId,
        String email,
        String name
) {
}