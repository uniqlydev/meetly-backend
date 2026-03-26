package com.meetly.modules.auth.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.cognito")
public class CognitoProperties {

    private String userPoolId;
    private String clientId;
    private String clientSecret;
    private String authFlow = "USER_PASSWORD_AUTH";

    public String getUserPoolId() {
        return userPoolId;
    }

    public void setUserPoolId(String userPoolId) {
        this.userPoolId = userPoolId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthFlow() {
        return authFlow;
    }

    public void setAuthFlow(String authFlow) {
        this.authFlow = authFlow;
    }
}
