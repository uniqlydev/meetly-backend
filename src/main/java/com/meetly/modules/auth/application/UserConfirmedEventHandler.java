package com.meetly.modules.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meetly.modules.identity.application.IdentityCommandService;
import com.meetly.modules.identity.application.command.UpsertUserCommand;
import com.meetly.modules.identity.domain.User;

@Service
public class UserConfirmedEventHandler {

    private final IdentityCommandService identityCommandService;

    public UserConfirmedEventHandler(IdentityCommandService identityCommandService) {
        this.identityCommandService = identityCommandService;
    }

    @Transactional
    public User handle(UserConfirmedEvent event) {
        return identityCommandService.upsertUser(
                new UpsertUserCommand(
                        event.externalAuthId(),
                event.username(),
                        event.email(),
                buildDisplayName(event.givenName(), event.familyName(), event.username())
                )
        );
    }

    private String buildDisplayName(String givenName, String familyName, String fallbackUsername) {
        String first = givenName == null ? "" : givenName.trim();
        String last = familyName == null ? "" : familyName.trim();
        String joined = (first + " " + last).trim();
        return joined.isEmpty() ? fallbackUsername : joined;
    }
}