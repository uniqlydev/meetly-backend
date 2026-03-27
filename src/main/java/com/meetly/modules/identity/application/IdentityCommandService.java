package com.meetly.modules.identity.application;


import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
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
                .map(existingUser -> updateExisting(existingUser, command))
                .orElseGet(() -> userRepository.findByUsername(command.username())
                        .map(existingUser -> updateExisting(existingUser, command))
                        .orElseGet(() -> createIfAbsent(command)));
    }

    private User updateExisting(User existingUser, UpsertUserCommand command) {
        existingUser.syncAuthProfile(
                command.externalAuthId(),
                command.username(),
                command.email(),
                command.name()
        );
        return userRepository.save(existingUser);
    }

    private User createIfAbsent(UpsertUserCommand command) {
        try {
            return userRepository.save(
                User.create(
                    command.externalAuthId(),
                    command.username(),
                    command.email(),
                    command.name()
                )
            );
        } catch (DataIntegrityViolationException ex) {
            // Another concurrent request inserted the same externalAuthId first.
            return userRepository.findByExternalAuthId(command.externalAuthId())
                .or(() -> userRepository.findByUsername(command.username()))
                .orElseThrow(() -> ex);
        }
    }
}