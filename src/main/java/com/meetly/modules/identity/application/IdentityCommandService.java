package com.meetly.modules.identity.application;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meetly.modules.identity.application.command.UpsertUserCommand;
import com.meetly.modules.identity.domain.User;
import com.meetly.modules.identity.domain.UserRepository;

@Service
public class IdentityCommandService {

    private final UserRepository userRepository;

    public IdentityCommandService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User upsertUser(UpsertUserCommand command) {
        return userRepository.findByExternalAuthId(command.externalAuthId())
                .orElseGet(() -> userRepository.save(
                        User.create(
                                command.externalAuthId(),
                                command.email(),
                                command.name()
                        )
                ));
    }
}