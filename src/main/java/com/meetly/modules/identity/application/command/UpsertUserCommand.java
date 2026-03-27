package com.meetly.modules.identity.application.command;

public record UpsertUserCommand(
        String externalAuthId,
        String username,
        String email,
        String name
) {
}