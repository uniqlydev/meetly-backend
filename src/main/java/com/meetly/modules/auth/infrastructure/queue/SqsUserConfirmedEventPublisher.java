package com.meetly.modules.auth.infrastructure.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetly.modules.auth.application.UserConfirmedEvent;
import com.meetly.modules.auth.application.UserConfirmedEventPublisher;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class SqsUserConfirmedEventPublisher implements UserConfirmedEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SqsUserConfirmedEventPublisher.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final UserConfirmedQueueProperties queueProperties;

    public SqsUserConfirmedEventPublisher(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            UserConfirmedQueueProperties queueProperties
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueProperties = queueProperties;
    }

    @Override
    public void publish(UserConfirmedEvent event) {
        if (!StringUtils.hasText(queueProperties.getQueueUrl())) {
            throw new IllegalStateException("aws.sqs.user-confirmed.queue-url is not configured");
        }

        try {
            String payload = objectMapper.writeValueAsString(event);
            sqsClient.sendMessage(
                    SendMessageRequest.builder()
                            .queueUrl(queueProperties.getQueueUrl())
                            .messageBody(payload)
                            .build()
            );
            log.debug("Published user-confirmed event [eventId={}, externalAuthId={}]",
                    event.eventId(), event.externalAuthId());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize user-confirmed event", ex);
        }
    }
}