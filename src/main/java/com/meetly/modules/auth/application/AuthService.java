package com.meetly.modules.auth.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.meetly.modules.auth.infrastructure.CognitoService;
import com.meetly.modules.auth.infrastructure.CognitoService.CognitoUserProfile;
import com.meetly.modules.auth.infrastructure.queue.UserConfirmedQueueProperties;
import com.meetly.modules.auth.presentation.request.SignUpRequest;
import com.meetly.modules.auth.presentation.response.AuthUserResponse;
import com.meetly.modules.auth.presentation.response.LoginResponse;
import com.meetly.modules.identity.application.IdentityCommandService;
import com.meetly.modules.identity.application.command.UpsertUserCommand;
import com.meetly.modules.identity.domain.User;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final CognitoService cognitoService;
    private final UserConfirmedEventPublisher userConfirmedEventPublisher;
    private final UserConfirmedEventHandler userConfirmedEventHandler;
    private final UserConfirmedQueueProperties userConfirmedQueueProperties;
    private final IdentityCommandService identityCommandService;

    public AuthService(
            CognitoService cognitoService,
            UserConfirmedEventPublisher userConfirmedEventPublisher,
            UserConfirmedEventHandler userConfirmedEventHandler,
            UserConfirmedQueueProperties userConfirmedQueueProperties,
            IdentityCommandService identityCommandService
    ) {
        this.cognitoService = cognitoService;
        this.userConfirmedEventPublisher = userConfirmedEventPublisher;
        this.userConfirmedEventHandler = userConfirmedEventHandler;
        this.userConfirmedQueueProperties = userConfirmedQueueProperties;
        this.identityCommandService = identityCommandService;
    }

    public String signUp(SignUpRequest request) {
        String username = cognitoService.signUp(request);
        return username;
    }

    public void confirmSignUp(String username, String confirmationCode) {
        cognitoService.confirmSignUp(username, confirmationCode);

        CognitoUserProfile profile = cognitoService.getUserProfile(username);
        UserConfirmedEvent event = new UserConfirmedEvent(
                UUID.randomUUID().toString(),
                profile.sub(),
                profile.username(),
                profile.email(),
                profile.givenName(),
                profile.familyName(),
                OffsetDateTime.now(),
                1
        );

        try {
            userConfirmedEventPublisher.publish(event);
            if (!userConfirmedQueueProperties.isConsumerEnabled()) {
                userConfirmedEventHandler.handle(event);
                log.info("User-confirmed event published and persisted locally because queue consumer is disabled [username={}]", username);
            }
        } catch (Exception ex) {
            // Fallback avoids losing confirmed users if queue publish fails.
            log.error("Failed publishing user-confirmed event. Falling back to direct upsert [username={}]", username, ex);
            userConfirmedEventHandler.handle(event);
        }
    }

    public LoginResponse signIn(String username, String password) {
        AuthenticationResultType result = cognitoService.signIn(username, password);
        User user = ensureUserRecord(username);

        return new LoginResponse(
                result.accessToken(),
                result.idToken(),
                result.refreshToken(),
                result.expiresIn(),
                result.tokenType(),
                toAuthUserResponse(user)
        );
    }

    public AuthUserResponse toAuthUserResponse(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getExternalAuthId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.isVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private User ensureUserRecord(String username) {
        CognitoUserProfile profile = cognitoService.getUserProfile(username);
        return identityCommandService.upsertUser(
                new UpsertUserCommand(
                        profile.sub(),
                        profile.username(),
                        profile.email(),
                        buildDisplayName(profile.givenName(), profile.familyName(), profile.username())
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
