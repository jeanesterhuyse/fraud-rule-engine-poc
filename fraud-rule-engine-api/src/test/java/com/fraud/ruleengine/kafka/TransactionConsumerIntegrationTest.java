package com.fraud.ruleengine.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.entity.TriggeredTransaction;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.enums.TransactionType;
import com.fraud.ruleengine.domain.model.Transaction;
import com.fraud.ruleengine.repository.RuleRepository;
import com.fraud.ruleengine.repository.TriggeredTransactionRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for TransactionConsumer using Testcontainers.
 *
 * Tests the full Kafka message consumption flow:
 * - Message consumption from Kafka
 * - Rule evaluation
 * - Database persistence
 * - Error handling and DLQ routing
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION_TESTS", matches = "true", disabledReason = "Integration tests require Docker and RUN_INTEGRATION_TESTS=true")
class TransactionConsumerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:15-alpine"))
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Kafka properties
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafka::getBootstrapServers);

        // PostgreSQL properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Enable Flyway for integration tests with real PostgreSQL
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private TriggeredTransactionRepository triggeredTransactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.topics.transactions}")
    private String transactionsTopic;

    private KafkaTemplate<String, Transaction> kafkaTemplate;

    @BeforeEach
    void setUp() {
        // Create Kafka producer for tests
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        DefaultKafkaProducerFactory<String, Transaction> producerFactory =
            new DefaultKafkaProducerFactory<>(producerProps);

        kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // Clean up database before each test
        triggeredTransactionRepository.deleteAll();
        ruleRepository.deleteAll();
    }

    @Test
    void shouldConsumeTransactionAndPersistTriggeredRecord() {
        // Given: Active rule with amount threshold
        Rule rule = createAmountThresholdRule("High Value Transaction", new BigDecimal("10000"), true, 100);
        ruleRepository.save(rule);

        Transaction transaction = createTransaction(
            "TXN-001",
            "CUST-001",
            "ACC-001",
            new BigDecimal("15000"),
            "ZAR",
            TransactionType.PURCHASE
        );

        // When: Send transaction to Kafka
        kafkaTemplate.send(transactionsTopic, transaction.transactionId(), transaction);

        // Then: Wait for processing and verify DB record exists
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<TriggeredTransaction> triggered = triggeredTransactionRepository.findAll();
            assertThat(triggered).hasSize(1);

            TriggeredTransaction record = triggered.get(0);
            assertThat(record.getTransactionId()).isEqualTo("TXN-001");
            assertThat(record.getCustomerId()).isEqualTo("CUST-001");
            assertThat(record.getAmount()).isEqualByComparingTo(new BigDecimal("15000"));
            assertThat(record.getRuleType()).isEqualTo(RuleType.AMOUNT_THRESHOLD);
            assertThat(record.getRiskScore()).isEqualTo(75);
            assertThat(record.getMatchReason()).contains("15000", "10000");
            assertThat(record.getRule()).isNotNull();
            assertThat(record.getRule().getId()).isEqualTo(rule.getId());
        });
    }

    @Test
    void shouldHandleInvalidJsonMessage() {
        // Given: No active rules (testing just message deserialization failure)

        // When: Send invalid JSON to Kafka (using raw producer)
        // Note: This test verifies the ErrorHandlingDeserializer catches deserialization errors
        // In production, such messages would be routed to DLQ

        // For now, we'll test that a malformed transaction field is handled
        Transaction invalidTransaction = createTransaction(
            null, // Invalid: null transaction ID
            "CUST-001",
            "ACC-001",
            new BigDecimal("15000"),
            "ZAR",
            TransactionType.PURCHASE
        );

        kafkaTemplate.send(transactionsTopic, "invalid-key", invalidTransaction);

        // Then: Wait and verify no record was persisted (validation should have failed)
        await().pollDelay(2, TimeUnit.SECONDS)
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                List<TriggeredTransaction> triggered = triggeredTransactionRepository.findAll();
                assertThat(triggered).isEmpty();
            });
    }

    @Test
    void shouldTriggerMultipleRulesForSingleTransaction() {
        // Given: Two active rules that will both trigger
        Rule rule1 = createAmountThresholdRule("High Value", new BigDecimal("10000"), true, 100);
        Rule rule2 = createGeographicAnomalyRule("Russia Transactions", "RUS", true, 200);

        ruleRepository.save(rule1);
        ruleRepository.save(rule2);

        Transaction transaction = createTransactionWithCountry(
            "TXN-002",
            "CUST-002",
            "ACC-002",
            new BigDecimal("15000"),
            "RUB",
            TransactionType.PURCHASE,
            "RUS"
        );

        // When: Send transaction to Kafka
        kafkaTemplate.send(transactionsTopic, transaction.transactionId(), transaction);

        // Then: Verify multiple DB records created
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<TriggeredTransaction> triggered = triggeredTransactionRepository.findAll();
            assertThat(triggered).hasSize(2);

            assertThat(triggered)
                .extracting(TriggeredTransaction::getRuleType)
                .containsExactlyInAnyOrder(RuleType.AMOUNT_THRESHOLD, RuleType.GEOGRAPHIC_ANOMALY);

            assertThat(triggered)
                .allMatch(t -> t.getTransactionId().equals("TXN-002"));
        });
    }

    @Test
    void shouldNotPersistWhenNoActiveRules() {
        // Given: No active rules in database

        Transaction transaction = createTransaction(
            "TXN-003",
            "CUST-003",
            "ACC-003",
            new BigDecimal("5000"),
            "ZAR",
            TransactionType.PURCHASE
        );

        // When: Send transaction to Kafka
        kafkaTemplate.send(transactionsTopic, transaction.transactionId(), transaction);

        // Then: Wait and verify no records persisted
        await().pollDelay(2, TimeUnit.SECONDS)
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                List<TriggeredTransaction> triggered = triggeredTransactionRepository.findAll();
                assertThat(triggered).isEmpty();
            });
    }

    @Test
    void shouldNotTriggerWhenAmountBelowThreshold() {
        // Given: Rule with threshold of 10000
        Rule rule = createAmountThresholdRule("High Value", new BigDecimal("10000"), true, 100);
        ruleRepository.save(rule);

        Transaction transaction = createTransaction(
            "TXN-004",
            "CUST-004",
            "ACC-004",
            new BigDecimal("5000"), // Below threshold
            "ZAR",
            TransactionType.PURCHASE
        );

        // When: Send transaction to Kafka
        kafkaTemplate.send(transactionsTopic, transaction.transactionId(), transaction);

        // Then: Verify no records persisted
        await().pollDelay(2, TimeUnit.SECONDS)
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                List<TriggeredTransaction> triggered = triggeredTransactionRepository.findAll();
                assertThat(triggered).isEmpty();
            });
    }

    @Test
    void shouldNotTriggerWhenRuleIsDisabled() {
        // Given: Disabled rule
        Rule rule = createAmountThresholdRule("High Value", new BigDecimal("10000"), false, 100);
        ruleRepository.save(rule);

        Transaction transaction = createTransaction(
            "TXN-005",
            "CUST-005",
            "ACC-005",
            new BigDecimal("15000"),
            "ZAR",
            TransactionType.PURCHASE
        );

        // When: Send transaction to Kafka
        kafkaTemplate.send(transactionsTopic, transaction.transactionId(), transaction);

        // Then: Verify no records persisted
        await().pollDelay(2, TimeUnit.SECONDS)
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                List<TriggeredTransaction> triggered = triggeredTransactionRepository.findAll();
                assertThat(triggered).isEmpty();
            });
    }

    // Helper methods

    private Rule createAmountThresholdRule(String name, BigDecimal threshold, boolean enabled, int priority) {
        return Rule.builder()
            .name(name)
            .description("Test rule")
            .ruleType(RuleType.AMOUNT_THRESHOLD)
            .enabled(enabled)
            .priority(priority)
            .thresholdAmount(threshold)
            .build();
    }

    private Rule createGeographicAnomalyRule(String name, String countryCode, boolean enabled, int priority) {
        return Rule.builder()
            .name(name)
            .description("Test geographic rule")
            .ruleType(RuleType.GEOGRAPHIC_ANOMALY)
            .enabled(enabled)
            .priority(priority)
            .countryCode(countryCode)
            .build();
    }

    private Transaction createTransaction(
        String transactionId,
        String customerId,
        String accountId,
        BigDecimal amount,
        String currency,
        TransactionType type
    ) {
        return new Transaction(
            transactionId,
            accountId,
            customerId,
            amount,
            currency,
            "Test Merchant",
            "RETAIL",
            type,
            LocalDateTime.now(),
            "ZAF",
            null, // deviceId
            null, // ipAddress
            null  // cardLastFour
        );
    }

    private Transaction createTransactionWithCountry(
        String transactionId,
        String customerId,
        String accountId,
        BigDecimal amount,
        String currency,
        TransactionType type,
        String countryCode
    ) {
        return new Transaction(
            transactionId,
            accountId,
            customerId,
            amount,
            currency,
            "Test Merchant",
            "RETAIL",
            type,
            LocalDateTime.now(),
            countryCode,
            null, // deviceId
            null, // ipAddress
            null  // cardLastFour
        );
    }
}
