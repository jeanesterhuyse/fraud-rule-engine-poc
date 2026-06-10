package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.enums.TransactionType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TimeOfDayAnomalyRuleEvaluatorTest {

    private TimeOfDayAnomalyRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new TimeOfDayAnomalyRuleEvaluator();
    }

    @Test
    void shouldSupport_TimeOfDayAnomalyRuleType() {
        assertThat(evaluator.supports(RuleType.TIME_OF_DAY_ANOMALY)).isTrue();
        assertThat(evaluator.supports(RuleType.AMOUNT_THRESHOLD)).isFalse();
    }

    @Test
    void shouldTrigger_whenTransactionDuringUnusualHours() {
        Rule rule = createRule(2, 5); // 2 AM - 5 AM
        Transaction transaction = createTransaction(LocalDateTime.of(2026, 6, 10, 3, 30)); // 3:30 AM

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();

        RuleMatch match = result.get();

        // Assert fixed risk score for time anomaly
        assertThat(match.riskScore()).isEqualTo(60);

        // Assert match reason contains specific time details
        assertThat(match.matchReason())
            .contains("03:00")           // Transaction hour (rounds down to hour)
            .contains("02:00")           // Start hour
            .contains("05:00")           // End hour
            .contains("unusual hours");

        // Assert rule reference
        assertThat(match.rule()).isEqualTo(rule);
    }

    @Test
    void shouldNotTrigger_whenTransactionOutsideUnusualHours() {
        Rule rule = createRule(2, 5); // 2 AM - 5 AM
        Transaction transaction = createTransaction(LocalDateTime.of(2026, 6, 10, 14, 30)); // 2:30 PM

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleMidnightWrapAround() {
        Rule rule = createRule(22, 2); // 10 PM - 2 AM (crosses midnight)

        // 11 PM - Should trigger (part of 22:00-02:00 range)
        Transaction transaction1 = createTransaction(LocalDateTime.of(2026, 6, 10, 23, 0));
        Optional<RuleMatch> result1 = evaluator.evaluate(transaction1, rule);
        assertThat(result1).isPresent();
        assertThat(result1.get().matchReason())
            .contains("23:00")
            .contains("22:00")
            .contains("02:00");

        // 1 AM - Should trigger (part of 22:00-02:00 range)
        Transaction transaction2 = createTransaction(LocalDateTime.of(2026, 6, 10, 1, 0));
        Optional<RuleMatch> result2 = evaluator.evaluate(transaction2, rule);
        assertThat(result2).isPresent();
        assertThat(result2.get().matchReason())
            .contains("01:00")
            .contains("22:00")
            .contains("02:00");

        // 3 AM - Should NOT trigger (outside 22:00-02:00 range)
        Transaction transaction3 = createTransaction(LocalDateTime.of(2026, 6, 10, 3, 0));
        assertThat(evaluator.evaluate(transaction3, rule)).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenConfigIsNull() {
        Rule rule = createRule(null, 5);
        Transaction transaction = createTransaction(LocalDateTime.of(2026, 6, 10, 3, 0));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldTrigger_forBoundaryHours() {
        Rule rule = createRule(2, 5); // 2 AM - 5 AM

        // Exactly at start hour (2 AM) - SHOULD trigger
        Transaction atStart = createTransaction(LocalDateTime.of(2026, 6, 10, 2, 0));
        Optional<RuleMatch> resultStart = evaluator.evaluate(atStart, rule);
        assertThat(resultStart).isPresent();
        assertThat(resultStart.get().matchReason()).contains("02:00");

        // Exactly at end hour (5 AM) - SHOULD trigger
        Transaction atEnd = createTransaction(LocalDateTime.of(2026, 6, 10, 5, 0));
        Optional<RuleMatch> resultEnd = evaluator.evaluate(atEnd, rule);
        assertThat(resultEnd).isPresent();
        assertThat(resultEnd.get().matchReason()).contains("05:00");

        // One hour before start (1 AM) - should NOT trigger
        Transaction beforeStart = createTransaction(LocalDateTime.of(2026, 6, 10, 1, 0));
        assertThat(evaluator.evaluate(beforeStart, rule)).isEmpty();

        // One hour after end (6 AM) - should NOT trigger
        Transaction afterEnd = createTransaction(LocalDateTime.of(2026, 6, 10, 6, 0));
        assertThat(evaluator.evaluate(afterEnd, rule)).isEmpty();
    }

    @Test
    void shouldHandleFullDayRange() {
        Rule rule = createRule(0, 23); // Entire day 0:00 - 23:00

        // Should trigger at any hour
        Transaction midnight = createTransaction(LocalDateTime.of(2026, 6, 10, 0, 0));
        Transaction noon = createTransaction(LocalDateTime.of(2026, 6, 10, 12, 0));
        Transaction evening = createTransaction(LocalDateTime.of(2026, 6, 10, 23, 0));

        assertThat(evaluator.evaluate(midnight, rule)).isPresent();
        assertThat(evaluator.evaluate(noon, rule)).isPresent();
        assertThat(evaluator.evaluate(evening, rule)).isPresent();
    }

    private Rule createRule(Integer startHour, Integer endHour) {
        return Rule.builder()
            .id(1L)
            .name("Test Rule")
            .ruleType(RuleType.TIME_OF_DAY_ANOMALY)
            .enabled(true)
            .priority(100)
            .startHour(startHour)
            .endHour(endHour)
            .build();
    }

    private Transaction createTransaction(LocalDateTime timestamp) {
        return new Transaction(
            "TXN-001",
            "ACC-001",
            "CUST-001",
            new BigDecimal("1000"),
            "ZAR",
            "Test Merchant",
            "RETAIL",
            TransactionType.PURCHASE,
            timestamp,
            "ZAF",
            "DEVICE-001",
            "192.168.1.1",
            "1234"
        );
    }
}
