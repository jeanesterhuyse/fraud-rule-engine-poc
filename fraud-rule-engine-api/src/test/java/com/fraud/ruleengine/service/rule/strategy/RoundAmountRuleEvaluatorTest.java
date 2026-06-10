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

class RoundAmountRuleEvaluatorTest {

    private RoundAmountRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new RoundAmountRuleEvaluator();
    }

    @Test
    void shouldSupport_RoundAmountRuleType() {
        assertThat(evaluator.supports(RuleType.ROUND_AMOUNT)).isTrue();
        assertThat(evaluator.supports(RuleType.AMOUNT_THRESHOLD)).isFalse();
    }

    @Test
    void shouldTrigger_whenAmountIsRoundAndAboveMinimum() {
        Rule rule = createRule(new BigDecimal("1000"), 100);
        Transaction transaction = createTransaction(new BigDecimal("5000"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();

        RuleMatch match = result.get();

        // Assert higher risk score for amounts >= 5000
        assertThat(match.riskScore()).isEqualTo(65);

        // Assert match reason contains all key details
        assertThat(match.matchReason())
            .contains("5000")              // Amount
            .contains("ZAR")               // Currency
            .contains("100")               // Round to nearest
            .contains("round number")
            .contains("card testing");

        // Assert rule reference
        assertThat(match.rule()).isEqualTo(rule);
    }

    @Test
    void shouldNotTrigger_whenAmountIsNotRound() {
        Rule rule = createRule(new BigDecimal("1000"), 100);
        Transaction transaction = createTransaction(new BigDecimal("5123"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        // 5123 is not a multiple of 100, should not trigger
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenAmountBelowMinimum() {
        Rule rule = createRule(new BigDecimal("1000"), 100);
        Transaction transaction = createTransaction(new BigDecimal("500"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        // 500 is below minimum_amount of 1000, should not trigger
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCalculateLowerRiskScore_forSmallerAmounts() {
        Rule rule = createRule(new BigDecimal("1000"), 100);
        Transaction transaction = createTransaction(new BigDecimal("3000"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();

        // Assert lower risk score for amounts < 5000
        assertThat(result.get().riskScore()).isEqualTo(55);

        assertThat(result.get().matchReason())
            .contains("3000")
            .contains("100");
    }

    @Test
    void shouldNotTrigger_whenConfigIsNull() {
        Rule rule = createRule(null, 100);
        Transaction transaction = createTransaction(new BigDecimal("1000"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldTrigger_forExactlyMinimumAmount() {
        Rule rule = createRule(new BigDecimal("1000"), 100);
        Transaction transaction = createTransaction(new BigDecimal("1000"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        // 1000 equals minimum_amount and is a multiple of 100, SHOULD trigger
        // (rule requires amount >= minimum, 1000 >= 1000 is true)
        assertThat(result).isPresent();
        assertThat(result.get().riskScore()).isEqualTo(55); // < 5000
        assertThat(result.get().matchReason()).contains("1000");
    }

    @Test
    void shouldTrigger_forJustAboveMinimum() {
        Rule rule = createRule(new BigDecimal("1000"), 100);
        Transaction transaction = createTransaction(new BigDecimal("1100"));

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        // 1100 is above minimum_amount and is a multiple of 100, should trigger
        assertThat(result).isPresent();
        assertThat(result.get().riskScore()).isEqualTo(55); // < 5000
        assertThat(result.get().matchReason()).contains("1100");
    }

    @Test
    void shouldTrigger_forBoundaryOf5000() {
        Rule rule = createRule(new BigDecimal("1000"), 100);

        // 4999 - should trigger with lower risk score (if it's round)
        // Actually 4999 is not a multiple of 100, so it won't trigger
        Transaction below5000 = createTransaction(new BigDecimal("4900"));
        Optional<RuleMatch> resultBelow = evaluator.evaluate(below5000, rule);
        assertThat(resultBelow).isPresent();
        assertThat(resultBelow.get().riskScore()).isEqualTo(55);

        // Exactly 5000 - should trigger with higher risk score
        Transaction exactly5000 = createTransaction(new BigDecimal("5000"));
        Optional<RuleMatch> resultExact = evaluator.evaluate(exactly5000, rule);
        assertThat(resultExact).isPresent();
        assertThat(resultExact.get().riskScore()).isEqualTo(65);

        // 5100 - should trigger with higher risk score
        Transaction above5000 = createTransaction(new BigDecimal("5100"));
        Optional<RuleMatch> resultAbove = evaluator.evaluate(above5000, rule);
        assertThat(resultAbove).isPresent();
        assertThat(resultAbove.get().riskScore()).isEqualTo(65);
    }

    @Test
    void shouldWork_withDifferentRoundToValues() {
        // Round to nearest 10
        Rule rule10 = createRule(new BigDecimal("100"), 10);
        assertThat(evaluator.evaluate(createTransaction(new BigDecimal("1230")), rule10)).isPresent();
        assertThat(evaluator.evaluate(createTransaction(new BigDecimal("1235")), rule10)).isEmpty();

        // Round to nearest 500
        Rule rule500 = createRule(new BigDecimal("1000"), 500);
        assertThat(evaluator.evaluate(createTransaction(new BigDecimal("5000")), rule500)).isPresent();
        assertThat(evaluator.evaluate(createTransaction(new BigDecimal("5500")), rule500)).isPresent();
        assertThat(evaluator.evaluate(createTransaction(new BigDecimal("5123")), rule500)).isEmpty();

        // Round to nearest 1000
        Rule rule1000 = createRule(new BigDecimal("1000"), 1000);
        assertThat(evaluator.evaluate(createTransaction(new BigDecimal("10000")), rule1000)).isPresent();
        assertThat(evaluator.evaluate(createTransaction(new BigDecimal("9999")), rule1000)).isEmpty();
    }

    private Rule createRule(BigDecimal minimumAmount, Integer roundToNearest) {
        return Rule.builder()
            .id(1L)
            .name("Test Rule")
            .ruleType(RuleType.ROUND_AMOUNT)
            .enabled(true)
            .priority(100)
            .minimumAmount(minimumAmount)
            .roundToNearest(roundToNearest)
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
