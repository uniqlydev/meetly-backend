package com.meetly.modules.auth.presentation.response;

public record LoginResponse(
        String accessToken,
        String idToken,
        String refreshToken,
        Integer expiresIn,
        String tokenType,
        AuthUserResponse user
) {
}
