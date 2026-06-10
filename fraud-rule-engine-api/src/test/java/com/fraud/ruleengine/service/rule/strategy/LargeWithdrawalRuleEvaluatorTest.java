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

class LargeWithdrawalRuleEvaluatorTest {

    private LargeWithdrawalRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new LargeWithdrawalRuleEvaluator();
    }

    @Test
    void shouldSupport_LargeWithdrawalRuleType() {
        assertThat(evaluator.supports(RuleType.LARGE_WITHDRAWAL)).isTrue();
        assertThat(evaluator.supports(RuleType.AMOUNT_THRESHOLD)).isFalse();
    }

    @Test
    void shouldTrigger_whenWithdrawalExceedsThreshold() {
        Rule rule = createRule(new BigDecimal("20000"));
        Transaction transaction = createTransaction(new BigDecimal("30000"), TransactionType.WITHDRAWAL);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();
        assertThat(result.get().matchReason()).contains("Large cash withdrawal");
        assertThat(result.get().riskScore()).isGreaterThanOrEqualTo(50);
        assertThat(result.get().riskScore()).isLessThanOrEqualTo(80);
    }

    @Test
    void shouldNotTrigger_forNonWithdrawalTransaction() {
        Rule rule = createRule(new BigDecimal("20000"));
        Transaction transaction = createTransaction(new BigDecimal("30000"), TransactionType.PURCHASE);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenWithdrawalBelowThreshold() {
        Rule rule = createRule(new BigDecimal("20000"));
        Transaction transaction = createTransaction(new BigDecimal("15000"), TransactionType.WITHDRAWAL);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCalculateHigherRiskScore_forLargerExcess() {
        Rule rule = createRule(new BigDecimal("20000"));
        Transaction transaction1 = createTransaction(new BigDecimal("25000"), TransactionType.WITHDRAWAL);
        Transaction transaction2 = createTransaction(new BigDecimal("60000"), TransactionType.WITHDRAWAL);

        Optional<RuleMatch> result1 = evaluator.evaluate(transaction1, rule);
        Optional<RuleMatch> result2 = evaluator.evaluate(transaction2, rule);

        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result2.get().riskScore()).isGreaterThan(result1.get().riskScore());
    }

    @Test
    void shouldNotTrigger_whenThresholdIsNull() {
        Rule rule = createRule(null);
        Transaction transaction = createTransaction(new BigDecimal("30000"), TransactionType.WITHDRAWAL);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    private Rule createRule(BigDecimal thresholdAmount) {
        return Rule.builder()
            .id(1L)
            .name("Test Rule")
            .ruleType(RuleType.LARGE_WITHDRAWAL)
            .enabled(true)
            .priority(160)
            .thresholdAmount(thresholdAmount)
            .build();
    }

    private Transaction createTransaction(BigDecimal amount, TransactionType type) {
        return new Transaction(
            "TXN-001",
            "ACC-001",
            "CUST-001",
            amount,
            "ZAR",
            "Test Merchant",
            "RETAIL",
            type,
            LocalDateTime.now(),
            "ZAF",
            "DEVICE-001",
            "192.168.1.1",
            "1234"
        );
    }
}
