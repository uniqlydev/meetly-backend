package com.meetly.shared.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.meetly.modules.auth.infrastructure.queue.UserConfirmedQueueProperties;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableConfigurationProperties(UserConfirmedQueueProperties.class)
public class AwsMessagingConfig {

    @Bean
    SqsClient sqsClient(Region awsRegion) {
        return SqsClient.builder()
                .region(awsRegion)
                .build();
    }
}