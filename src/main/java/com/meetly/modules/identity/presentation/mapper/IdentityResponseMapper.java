package com.meetly.modules.identity.presentation.mapper;

import org.springframework.stereotype.Component;

import com.meetly.modules.identity.domain.User;
import com.meetly.modules.identity.presentation.response.UserProfileResponse;

@Component
public class IdentityResponseMapper {

    public UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.isVerified()
        );
    }
}