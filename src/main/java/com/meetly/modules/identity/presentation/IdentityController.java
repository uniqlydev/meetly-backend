package com.meetly.modules.identity.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meetly.modules.identity.application.IdentityCommandService;
import com.meetly.modules.identity.application.IdentityQueryService;
import com.meetly.modules.identity.application.command.UpsertUserCommand;
import com.meetly.modules.identity.application.query.GetMyProfileQuery;
import com.meetly.modules.identity.presentation.mapper.IdentityResponseMapper;
import com.meetly.modules.identity.presentation.response.UserProfileResponse;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final IdentityCommandService identityCommandService;
    private final IdentityQueryService identityQueryService;
    private final IdentityResponseMapper identityResponseMapper;

    public IdentityController(
            IdentityCommandService identityCommandService,
            IdentityQueryService identityQueryService,
            IdentityResponseMapper identityResponseMapper
    ) {
        this.identityCommandService = identityCommandService;
        this.identityQueryService = identityQueryService;
        this.identityResponseMapper = identityResponseMapper;
    }

    @GetMapping("/me")
    public UserProfileResponse me(
            @RequestHeader(value = "X-User-Id", defaultValue = "dev-user-1") String externalAuthId,
            @RequestHeader(value = "X-User-Email", defaultValue = "dev1@meetly.app") String email,
            @RequestHeader(value = "X-User-Name", defaultValue = "Brendan") String name
    ) {
        identityCommandService.upsertUser(
                new UpsertUserCommand(externalAuthId, email, name)
        );

        return identityResponseMapper.toResponse(
                identityQueryService.getMyProfile(new GetMyProfileQuery(externalAuthId))
        );
    }
}