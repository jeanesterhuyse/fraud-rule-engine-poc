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

class AmountThresholdRuleEvaluatorTest {

    private AmountThresholdRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new AmountThresholdRuleEvaluator();
    }

    @Test
    void shouldSupport_AmountThresholdRuleType() {
        assertThat(evaluator.supports(RuleType.AMOUNT_THRESHOLD)).isTrue();
        assertThat(evaluator.supports(RuleType.VELOCITY)).isFalse();
    }

    @Test
    void shouldTrigger_whenAmountExceedsThreshold() {
        Rule rule = createRule(new BigDecimal("10000"));
        Transaction transaction = createTransaction(new BigDecimal("15000"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();
        assertThat(result.get().matchReason()).contains("exceeds threshold");
        assertThat(result.get().riskScore()).isGreaterThan(50);
    }

    @Test
    void shouldNotTrigger_whenAmountBelowThreshold() {
        Rule rule = createRule(new BigDecimal("10000"));
        Transaction transaction = createTransaction(new BigDecimal("5000"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenAmountEqualsThreshold() {
        Rule rule = createRule(new BigDecimal("10000"));
        Transaction transaction = createTransaction(new BigDecimal("10000"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenThresholdIsNull() {
        Rule rule = createRule(null);
        Transaction transaction = createTransaction(new BigDecimal("15000"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    private Rule createRule(BigDecimal thresholdAmount) {
        return Rule.builder()
            .id(1L)
            .name("Test Rule")
            .ruleType(RuleType.AMOUNT_THRESHOLD)
            .enabled(true)
            .priority(100)
            .thresholdAmount(thresholdAmount)
            .build();
    }

    private Transaction createTransaction(BigDecimal amount) {
        return new Transaction(
            "TXN-001",
            "ACC-001",
            "CUST-001",
            amount,
            "ZAR",
            "Test Merchant",
            "RETAIL",
            TransactionType.PURCHASE,
            LocalDateTime.now(),
            "ZAF",
            "DEVICE-001",
            "192.168.1.1",
            "1234"
        );
    }
}
