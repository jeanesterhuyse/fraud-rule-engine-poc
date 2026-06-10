package com.fraud.ruleengine.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka configuration for transaction consumption and error handling.
 *
 * Features:
 * - Auto-creates topics if they don't exist (for local dev)
 * - Configures Dead Letter Queue (DLQ) for failed messages
 * - Sets up retry logic with exponential backoff
 * - Error handler routes poison messages to DLQ after retries
 */
@Configuration
@Slf4j
public class KafkaConfig {

    @Value("${app.kafka.topics.transactions}")
    private String transactionsTopic;

    @Value("${app.kafka.topics.dlq}")
    private String dlqTopic;

    @Value("${app.kafka.auto-create:true}")
    private boolean autoCreateTopics;

    /**
     * Creates primary transactions topic.
     * Auto-creation only enabled for local dev (should be disabled in production).
     */
    @Bean
    public NewTopic transactionsTopic() {
        if (!autoCreateTopics) {
            log.info("Auto-create topics disabled, skipping topic creation");
            return null;
        }

        log.info("Creating Kafka topic: {}", transactionsTopic);
        return TopicBuilder.name(transactionsTopic)
            .partitions(1)  // Single partition for POC (preserves ordering)
            .replicas(1)    // Single replica for local dev
            .build();
    }

    /**
     * Creates Dead Letter Queue topic for failed messages.
     */
    @Bean
    public NewTopic dlqTopic() {
        if (!autoCreateTopics) {
            return null;
        }

        log.info("Creating Kafka DLQ topic: {}", dlqTopic);
        return TopicBuilder.name(dlqTopic)
            .partitions(1)
            .replicas(1)
            .build();
    }

    /**
     * Configures error handler with retry logic and DLQ routing.
     *
     * Retry Strategy:
     * - 3 retry attempts
     * - 1 second fixed interval between retries
     * - After 3 failures, route to DLQ
     *
     * Transient errors (network, DB timeout) get retried.
     * Permanent errors (deserialization, validation) go straight to DLQ.
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Fixed backoff: 1 second interval, 3 attempts
        var backOff = new FixedBackOff(1000L, 3L);

        var errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
            // This is executed after all retries have failed
            log.error("Message processing failed after {} retries. Routing to DLQ: {}",
                3, consumerRecord.value(), exception);

            // Send to DLQ
            kafkaTemplate.send(dlqTopic, consumerRecord.value());

        }, backOff);

        // Add exceptions that should NOT be retried (go straight to DLQ)
        errorHandler.addNotRetryableExceptions(
            org.springframework.kafka.support.serializer.DeserializationException.class,
            org.springframework.messaging.converter.MessageConversionException.class,
            org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException.class
        );

        log.info("Kafka error handler configured with 3 retry attempts and DLQ routing to {}", dlqTopic);

        return errorHandler;
    }
}
