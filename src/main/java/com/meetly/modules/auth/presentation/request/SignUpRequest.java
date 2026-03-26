package com.meetly.modules.auth.presentation.request;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @JsonAlias({"user_name"})
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[^@\\s]+$", message = "username must not be an email")
        String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @JsonAlias({"profile_url", "profile url", "profileUrl"})
        @NotBlank @Size(max = 2048) String profile,
        @NotBlank @Size(max = 512) String address,
        @JsonAlias({"preferred_username", "preferred username"})
        @NotBlank @Size(min = 3, max = 64)
        @Pattern(regexp = "^[^@\\s]+$", message = "preferredUsername must not be an email")
        String preferredUsername,
        @JsonAlias({"pictureUrl", "picture_url"})
        @NotBlank @Size(max = 2048) String picture,
        @JsonAlias({"phone_number", "phone number"})
        @NotBlank @Size(max = 32) String phoneNumber,
        @JsonAlias({"given_name", "given name"})
        @NotBlank @Size(max = 100) String givenName,
        @JsonAlias({"family_name", "family name", "last_name", "last name", "lastName"})
        @NotBlank @Size(max = 100) String familyName,
        @JsonAlias({"middle_name", "middle name"})
        @NotBlank @Size(max = 100) String middleName
) {
}
