package com.meetly.modules.auth.presentation.response;

public record SignUpResponse(
        String message,
        String username,
        String email
) {
}