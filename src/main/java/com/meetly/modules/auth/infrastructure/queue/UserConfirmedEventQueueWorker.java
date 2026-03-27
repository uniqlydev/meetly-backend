package com.meetly.modules.auth.infrastructure.queue;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetly.modules.auth.application.UserConfirmedEvent;
import com.meetly.modules.auth.application.UserConfirmedEventHandler;

import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
@ConditionalOnProperty(prefix = "aws.sqs.user-confirmed", name = "consumer-enabled", havingValue = "true")
public class UserConfirmedEventQueueWorker {

    private static final Logger log = LoggerFactory.getLogger(UserConfirmedEventQueueWorker.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final UserConfirmedEventHandler eventHandler;
    private final UserConfirmedQueueProperties properties;

    private final ExecutorService workers;
    private volatile boolean running = true;

    public UserConfirmedEventQueueWorker(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            UserConfirmedEventHandler eventHandler,
            UserConfirmedQueueProperties properties
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.eventHandler = eventHandler;
        this.properties = properties;

        if (!StringUtils.hasText(properties.getQueueUrl())) {
            throw new IllegalStateException("aws.sqs.user-confirmed.queue-url must not be blank");
        }

        this.workers = Executors.newFixedThreadPool(properties.getWorkerCount());
        for (int i = 0; i < properties.getWorkerCount(); i++) {
            workers.submit(this::runWorkerLoop);
        }

        log.info(
                "Started user-confirmed queue worker [workers={}, maxMessages={}, waitTimeSeconds={}]",
                properties.getWorkerCount(),
                properties.getMaxMessages(),
                properties.getWaitTimeSeconds()
        );
    }

    private void runWorkerLoop() {
        while (running) {
            try {
                ReceiveMessageRequest.Builder requestBuilder = ReceiveMessageRequest.builder()
                        .queueUrl(properties.getQueueUrl())
                        .maxNumberOfMessages(properties.getMaxMessages())
                        .waitTimeSeconds(properties.getWaitTimeSeconds());

                if (properties.getVisibilityTimeoutSeconds() != null) {
                    requestBuilder.visibilityTimeout(properties.getVisibilityTimeoutSeconds());
                }

                List<Message> messages = sqsClient.receiveMessage(requestBuilder.build()).messages();
                for (Message message : messages) {
                    processSingleMessage(message);
                }
            } catch (Exception ex) {
                // Keep loop alive under transient network/service errors.
                log.error("User-confirmed queue polling failed: {}", ex.getMessage(), ex);
                sleepQuietly(1_000);
            }
        }
    }

    private void processSingleMessage(Message message) {
        try {
            UserConfirmedEvent event = objectMapper.readValue(message.body(), UserConfirmedEvent.class);
            eventHandler.handle(event);

            sqsClient.deleteMessage(
                    DeleteMessageRequest.builder()
                            .queueUrl(properties.getQueueUrl())
                            .receiptHandle(message.receiptHandle())
                            .build()
            );
        } catch (Exception ex) {
            // Do not delete failed messages; the queue visibility timeout allows retry.
            log.error(
                    "Failed processing user-confirmed event messageId={} body={} error={}",
                    message.messageId(),
                    message.body(),
                    ex.getMessage(),
                    ex
            );
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    void shutdown() {
        running = false;
        workers.shutdown();
        try {
            if (!workers.awaitTermination(10, TimeUnit.SECONDS)) {
                workers.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            workers.shutdownNow();
        }
    }
}