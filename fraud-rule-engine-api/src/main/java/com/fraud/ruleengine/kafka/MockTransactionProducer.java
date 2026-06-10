package com.fraud.ruleengine.kafka;

import com.fraud.ruleengine.domain.enums.TransactionType;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Mock transaction producer for testing and demonstration.
 *
 * Generates realistic-looking transactions on a schedule and publishes
 * them to Kafka. Some transactions are designed to trigger rules.
 *
 * Configuration:
 * - Enable/disable via: app.mock-producer.enabled
 * - Interval via: app.mock-producer.interval-ms
 *
 * Design: Uses @Scheduled for simplicity. Production would have
 * a more sophisticated event generation strategy.
 */
@Component
@ConditionalOnProperty(name = "app.mock-producer.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class MockTransactionProducer {

    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final Random random = new Random();

    @Value("${app.kafka.topics.transactions}")
    private String transactionsTopic;

    private int transactionCounter = 1;

    // Sample data for generating realistic transactions
    private static final List<String> CUSTOMER_IDS = List.of(
        "CUST-001", "CUST-002", "CUST-003", "CUST-004", "CUST-005",
        "CUST-006", "CUST-007", "CUST-008", "CUST-009", "CUST-010"
    );

    private static final List<String> ACCOUNT_IDS = List.of(
        "ACC-101", "ACC-102", "ACC-103", "ACC-104", "ACC-105",
        "ACC-106", "ACC-107", "ACC-108", "ACC-109", "ACC-110"
    );

    private static final List<String> COUNTRIES = List.of(
        "ZAF", "USA", "GBR", "DEU", "FRA", "RUS", "CHN", "JPN", "AUS", "CAN"
    );

    private static final List<String> MERCHANT_CATEGORIES = List.of(
        "RETAIL", "GROCERY", "RESTAURANTS", "FUEL", "ONLINE",
        "GAMBLING", "CRYPTO_EXCHANGE", "ENTERTAINMENT", "TRAVEL", "UTILITIES"
    );

    private static final List<String> MERCHANT_NAMES = List.of(
        "Amazon Store", "Local Grocery", "Pete's Diner", "Shell Gas",
        "Online Marketplace", "Las Vegas Casino", "CryptoEx", "Netflix",
        "Flight Booking", "Electric Company", "Moscow Casino Palace"
    );

    /**
     * Generates and publishes mock transactions on a schedule.
     */
    @Scheduled(fixedDelayString = "${app.mock-producer.interval-ms:10000}")
    public void produceTransaction() {
        Transaction transaction = generateTransaction();

        log.info("Publishing mock transaction to Kafka: {}", transaction.toLogString());

        kafkaTemplate.send(transactionsTopic, transaction)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish transaction {}: {}",
                        transaction.transactionId(), ex.getMessage());
                } else {
                    log.debug("Transaction {} published successfully to partition {}",
                        transaction.transactionId(),
                        result.getRecordMetadata().partition());
                }
            });
    }

    /**
     * Generates a realistic mock transaction.
     * Mix of normal and suspicious transactions.
     */
    private Transaction generateTransaction() {
        String transactionId = String.format("TXN-2026-%06d", transactionCounter++);
        String customerId = randomElement(CUSTOMER_IDS);
        String accountId = randomElement(ACCOUNT_IDS);
        TransactionType type = randomElement(TransactionType.values());

        // Generate transaction that may trigger rules
        BigDecimal amount;
        String country;
        String merchantCategory;
        String merchantName;

        int scenario = random.nextInt(100);

        if (scenario < 10) {
            // 10% - Large transaction (triggers AMOUNT_THRESHOLD)
            amount = new BigDecimal(50000 + random.nextInt(50000));
            country = randomElement(COUNTRIES);
            merchantCategory = randomElement(MERCHANT_CATEGORIES);
            merchantName = randomElement(MERCHANT_NAMES);
            log.debug("Generated LARGE transaction: {}", amount);

        } else if (scenario < 15) {
            // 5% - High-risk country (triggers GEOGRAPHIC_ANOMALY)
            amount = new BigDecimal(1000 + random.nextInt(10000));
            country = "RUS"; // High-risk country
            merchantCategory = randomElement(MERCHANT_CATEGORIES);
            merchantName = "Moscow Casino Palace";
            log.debug("Generated HIGH-RISK COUNTRY transaction");

        } else if (scenario < 20) {
            // 5% - Gambling (triggers MERCHANT_RISK)
            amount = new BigDecimal(500 + random.nextInt(5000));
            country = randomElement(COUNTRIES);
            merchantCategory = "GAMBLING";
            merchantName = "Las Vegas Casino";
            log.debug("Generated GAMBLING transaction");

        } else if (scenario < 23) {
            // 3% - Structuring amount (triggers AMOUNT_RANGE)
            amount = new BigDecimal(9000 + random.nextInt(999)); // 9000-9999
            country = randomElement(COUNTRIES);
            merchantCategory = randomElement(MERCHANT_CATEGORIES);
            merchantName = randomElement(MERCHANT_NAMES);
            log.debug("Generated STRUCTURING transaction: {}", amount);

        } else if (scenario < 25) {
            // 2% - Crypto exchange (triggers MERCHANT_RISK)
            amount = new BigDecimal(1000 + random.nextInt(20000));
            country = randomElement(COUNTRIES);
            merchantCategory = "CRYPTO_EXCHANGE";
            merchantName = "CryptoEx";
            log.debug("Generated CRYPTO transaction");

        } else {
            // 75% - Normal transaction (should not trigger)
            amount = new BigDecimal(10 + random.nextInt(1000));
            country = "ZAF"; // Safe country
            merchantCategory = "RETAIL";
            merchantName = randomElement(MERCHANT_NAMES);
        }

        return new Transaction(
            transactionId,
            accountId,
            customerId,
            amount,
            "ZAR", // Currency
            merchantName,
            merchantCategory,
            type,
            LocalDateTime.now(),
            country,
            "DEVICE-" + random.nextInt(1000),
            generateRandomIp(),
            String.format("%04d", random.nextInt(10000))
        );
    }

    private <T> T randomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private <T> T randomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }

    private String generateRandomIp() {
        return String.format("%d.%d.%d.%d",
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        );
    }
}
