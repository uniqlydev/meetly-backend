package com.meetly.modules.auth.infrastructure;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.meetly.shared.domain.DomainException;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;

@Service
public class CognitoService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Logger log = LoggerFactory.getLogger(CognitoService.class);

    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoProperties cognitoProperties;

    public CognitoService(CognitoIdentityProviderClient cognitoClient, CognitoProperties cognitoProperties) {
        this.cognitoClient = cognitoClient;
        this.cognitoProperties = cognitoProperties;
    }

    public String signUp(com.meetly.modules.auth.presentation.request.SignUpRequest request) {
        try {
            String cognitoUsername = resolveCognitoUsername(request);
            String preferredUsername = request.preferredUsername();

            List<AttributeType> userAttributes = new ArrayList<>();
            addIfPresent(userAttributes, "email", request.email());
            addIfPresent(userAttributes, "profile", request.profile());
            addIfPresent(userAttributes, "address", request.address());
            addIfPresent(userAttributes, "preferred_username", preferredUsername);
            addIfPresent(userAttributes, "picture", request.picture());
            addIfPresent(userAttributes, "phone_number", request.phoneNumber());
            addIfPresent(userAttributes, "given_name", request.givenName());
            addIfPresent(userAttributes, "family_name", request.familyName());
            addIfPresent(userAttributes, "middle_name", request.middleName());

            SignUpRequest.Builder requestBuilder = SignUpRequest.builder()
                    .clientId(cognitoProperties.getClientId())
                .username(cognitoUsername)
                    .password(request.password())
                    .userAttributes(userAttributes);

            String secretHash = buildSecretHash(cognitoUsername);
            if (secretHash != null) {
                requestBuilder.secretHash(secretHash);
            }

        cognitoClient.signUp(requestBuilder.build());
        log.info(
            "Cognito signUp succeeded [email={}, username={}, preferredUsername={}]",
            request.email(),
            cognitoUsername,
            preferredUsername
        );
        return cognitoUsername;
        } catch (CognitoIdentityProviderException | SdkClientException ex) {
            log.error(
                    "Cognito signup failed [email={}, username={}, preferredUsername={}]: {}",
                    request.email(),
                    request.username(),
                    request.preferredUsername(),
                    describeCognitoException(ex),
                    ex
            );
            throw new DomainException(normalizeCognitoError(cleanMessage(ex)));
        }
    }

    public void confirmSignUp(String username, String confirmationCode) {
        try {
            ConfirmSignUpRequest.Builder requestBuilder = ConfirmSignUpRequest.builder()
                    .clientId(cognitoProperties.getClientId())
                    .username(username)
                    .confirmationCode(confirmationCode);

            String secretHash = buildSecretHash(username);
            if (secretHash != null) {
                requestBuilder.secretHash(secretHash);
            }

            cognitoClient.confirmSignUp(requestBuilder.build());
            log.info("Cognito confirmSignUp succeeded [username={}]", username);
        } catch (CognitoIdentityProviderException | SdkClientException ex) {
            log.error(
                    "Cognito confirmSignUp failed [username={}]: {}",
                    username,
                    describeCognitoException(ex),
                    ex
            );
            throw new DomainException(cleanMessage(ex));
        }
    }

    public AuthenticationResultType signIn(String username, String password) {
        try {
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", username);
            authParameters.put("PASSWORD", password);

            String secretHash = buildSecretHash(username);
            if (secretHash != null) {
                authParameters.put("SECRET_HASH", secretHash);
            }

            InitiateAuthResponse response = cognitoClient.initiateAuth(
                    InitiateAuthRequest.builder()
                            .clientId(cognitoProperties.getClientId())
                            .authFlow(AuthFlowType.fromValue(cognitoProperties.getAuthFlow()))
                            .authParameters(authParameters)
                            .build()
            );

            return response.authenticationResult();
        } catch (CognitoIdentityProviderException | SdkClientException ex) {
            log.error(
                    "Cognito signIn failed [username={}, authFlow={}]: {}",
                    username,
                    cognitoProperties.getAuthFlow(),
                    describeCognitoException(ex),
                    ex
            );
            throw new DomainException(cleanMessage(ex));
        }
    }

    private String buildSecretHash(String username) {
        if (!StringUtils.hasText(cognitoProperties.getClientSecret())) {
            return null;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec signingKey = new SecretKeySpec(
                    cognitoProperties.getClientSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(signingKey);
            mac.update(username.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(cognitoProperties.getClientId().getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception ex) {
            throw new DomainException("Failed to build Cognito secret hash");
        }
    }

    private void addIfPresent(List<AttributeType> attributes, String name, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        attributes.add(AttributeType.builder().name(name).value(value).build());
    }

    private String resolveCognitoUsername(com.meetly.modules.auth.presentation.request.SignUpRequest request) {
        if (StringUtils.hasText(request.username())) {
            return request.username();
        }

        String email = request.email();
        int atIndex = email.indexOf('@');
        String prefix = atIndex > 0 ? email.substring(0, atIndex) : "user";
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return prefix + "_" + suffix;
    }

    private String normalizeCognitoError(String message) {
        if (message != null && message.contains("did not conform to the schema")) {
            return "Cognito user-pool schema rejects sign-up because some attributes are required. "
                    + "With your current setup, send all required attributes in /api/auth/signup: "
                    + "profile, address, preferredUsername, picture, phoneNumber, givenName, familyName, middleName. "
                    + "Original error: " + message;
        }

        if (message != null && message.contains("Username cannot be of email format")) {
            return "This Cognito pool uses email alias, so username must not be an email. "
                    + "Send a non-email username in /api/auth/signup.username, or let the backend auto-generate it.";
        }

        return message;
    }

    private String cleanMessage(Exception ex) {
        if (ex instanceof CognitoIdentityProviderException cognitoEx
                && cognitoEx.awsErrorDetails() != null
                && StringUtils.hasText(cognitoEx.awsErrorDetails().errorMessage())) {
            return cognitoEx.awsErrorDetails().errorMessage();
        }
        return ex.getMessage();
    }

    private String describeCognitoException(Exception ex) {
        if (ex instanceof CognitoIdentityProviderException cognitoEx && cognitoEx.awsErrorDetails() != null) {
            return "service=" + cognitoEx.awsErrorDetails().serviceName()
                    + ", code=" + cognitoEx.awsErrorDetails().errorCode()
                    + ", message=" + cognitoEx.awsErrorDetails().errorMessage()
                    + ", requestId=" + cognitoEx.requestId();
        }

        return ex.getMessage();
    }
}
