package com.meetly.modules.auth.presentation;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meetly.modules.auth.application.AuthService;
import com.meetly.modules.auth.presentation.request.ConfirmSignUpRequest;
import com.meetly.modules.auth.presentation.request.LoginRequest;
import com.meetly.modules.auth.presentation.request.SignUpRequest;
import com.meetly.modules.auth.presentation.response.LoginResponse;
import com.meetly.modules.auth.presentation.response.MessageResponse;
import com.meetly.modules.auth.presentation.response.SignUpResponse;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public SignUpResponse signUp(@Valid @RequestBody SignUpRequest request) {
        String username = authService.signUp(request);
        return new SignUpResponse(
                "Sign up successful. Please verify your email with the confirmation code.",
                username,
                request.email(),
                null
        );
    }

    @PostMapping("/confirm-signup")
    public MessageResponse confirmSignUp(@Valid @RequestBody ConfirmSignUpRequest request) {
        authService.confirmSignUp(request.username(), request.confirmationCode());
        return new MessageResponse("Account confirmed successfully.");
    }

    @PostMapping("/signin")
    public LoginResponse signIn(@Valid @RequestBody LoginRequest request) {
        return authService.signIn(request.username(), request.password());
    }
}
