package com.meetly.modules.auth.infrastructure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
@EnableConfigurationProperties(CognitoProperties.class)
public class CognitoConfig {

    @Bean
    CognitoIdentityProviderClient cognitoIdentityProviderClient(Region awsRegion) {
        return CognitoIdentityProviderClient.builder()
                .region(awsRegion)
                .build();
    }

    @Bean
    Region awsRegion(org.springframework.core.env.Environment environment) {
        return Region.of(environment.getRequiredProperty("aws.region"));
    }
}
