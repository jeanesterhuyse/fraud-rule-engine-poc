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

class CurrencyMismatchRuleEvaluatorTest {

    private CurrencyMismatchRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new CurrencyMismatchRuleEvaluator();
    }

    @Test
    void shouldSupport_CurrencyMismatchRuleType() {
        assertThat(evaluator.supports(RuleType.CURRENCY_MISMATCH)).isTrue();
        assertThat(evaluator.supports(RuleType.AMOUNT_THRESHOLD)).isFalse();
    }

    @Test
    void shouldTrigger_whenForeignCurrencyAndForeignCountry() {
        Rule rule = createRule("ZAF", "ZAR");
        Transaction transaction = createTransaction("USD", "USA");

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();
        assertThat(result.get().matchReason()).contains("Foreign currency");
        assertThat(result.get().riskScore()).isEqualTo(55);
    }

    @Test
    void shouldNotTrigger_whenHomeCurrencyUsed() {
        Rule rule = createRule("ZAF", "ZAR");
        Transaction transaction = createTransaction("ZAR", "ZAF");

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenForeignCurrencyButHomeCountry() {
        Rule rule = createRule("ZAF", "ZAR");
        Transaction transaction = createTransaction("USD", "ZAF");

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenHomeCurrencyButForeignCountry() {
        Rule rule = createRule("ZAF", "ZAR");
        Transaction transaction = createTransaction("ZAR", "USA");

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenConfigIsNull() {
        Rule rule = createRule(null, "ZAR");
        Transaction transaction = createTransaction("USD", "USA");

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    private Rule createRule(String customerHomeCountry, String customerHomeCurrency) {
        return Rule.builder()
            .id(1L)
            .name("Test Rule")
            .ruleType(RuleType.CURRENCY_MISMATCH)
            .enabled(true)
            .priority(100)
            .customerHomeCountry(customerHomeCountry)
            .customerHomeCurrency(customerHomeCurrency)
            .build();
    }

    private Transaction createTransaction(String currency, String countryCode) {
        return new Transaction(
            "TXN-001",
            "ACC-001",
            "CUST-001",
            new BigDecimal("1000"),
            currency,
            "Test Merchant",
            "RETAIL",
            TransactionType.PURCHASE,
            LocalDateTime.now(),
            countryCode,
            "DEVICE-001",
            "192.168.1.1",
            "1234"
        );
    }
}
