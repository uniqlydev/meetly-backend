package com.meetly.modules.auth.application;

import org.springframework.stereotype.Service;

import com.meetly.modules.auth.infrastructure.CognitoService;
import com.meetly.modules.auth.presentation.request.SignUpRequest;
import com.meetly.modules.auth.presentation.response.LoginResponse;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

@Service
public class AuthService {

    private final CognitoService cognitoService;

    public AuthService(CognitoService cognitoService) {
        this.cognitoService = cognitoService;
    }

    public String signUp(SignUpRequest request) {
        return cognitoService.signUp(request);
    }

    public void confirmSignUp(String username, String confirmationCode) {
        cognitoService.confirmSignUp(username, confirmationCode);
    }

    public LoginResponse signIn(String username, String password) {
        AuthenticationResultType result = cognitoService.signIn(username, password);

        return new LoginResponse(
                result.accessToken(),
                result.idToken(),
                result.refreshToken(),
                result.expiresIn(),
                result.tokenType()
        );
    }
}
