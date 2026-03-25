package com.meetly.modules.identity.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meetly.modules.identity.application.query.GetMyProfileQuery;
import com.meetly.modules.identity.domain.User;
import com.meetly.modules.identity.domain.UserRepository;
import com.meetly.shared.domain.DomainException;

@Service
public class IdentityQueryService {

    private final UserRepository userRepository;

    public IdentityQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getMyProfile(GetMyProfileQuery query) {
        return userRepository.findByExternalAuthId(query.externalAuthId())
                .orElseThrow(() -> new DomainException("User not found"));
    }
}