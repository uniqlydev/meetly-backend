package com.meetly.modules.auth.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmSignUpRequest(
        @NotBlank String username,
        @NotBlank String confirmationCode
) {
}
