package com.fraud.ruleengine.kafka;

import com.fraud.ruleengine.domain.model.Transaction;
import com.fraud.ruleengine.service.rule.RuleEvaluatorOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Kafka consumer for fraud transactions.
 *
 * Consumes transactions from the primary topic and evaluates them
 * against all active fraud rules.
 *
 * Error Handling:
 * - Validation errors: logged and routed to DLQ
 * - Transient errors: retried (configured in KafkaConfig)
 * - After retry exhaustion: routed to DLQ
 *
 * Design: Asynchronous processing with custom thread pool for scalability.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Validated
public class TransactionConsumer {

    private final RuleEvaluatorOrchestrator ruleEvaluator;

    /**
     * Consumes transactions from Kafka and evaluates them against fraud rules.
     *
     * Processing is done asynchronously to improve throughput and scalability.
     *
     * @param transaction The transaction (deserialized from JSON)
     * @param partition The Kafka partition
     * @param offset The message offset
     */
    @Async
    @KafkaListener(
        topics = "${app.kafka.topics.transactions}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransaction(
        @Payload @Valid Transaction transaction,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        MDC.put("transaction_id", transaction.transactionId());
        MDC.put("customer_id", transaction.customerId());

        try {
            log.info("Received transaction from Kafka [partition={}, offset={}]: {}",
                partition, offset, transaction.toLogString());

            var triggeredRecords = ruleEvaluator.evaluateTransaction(transaction);

            if (triggeredRecords.isEmpty()) {
                log.debug("Transaction {} did not trigger any rules. Not persisting.",
                    transaction.transactionId());
            } else {
                log.info("Transaction {} triggered {} rule(s) and was persisted to database",
                    transaction.transactionId(), triggeredRecords.size());
            }

        } catch (Exception e) {
            log.error("Error processing transaction {}: {}",
                transaction.transactionId(), e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    /**
     * Logs consumer startup.
     */
    public void onStartup() {
        log.info("Transaction consumer started. Listening on topic: ${app.kafka.topics.transactions}");
    }
}
