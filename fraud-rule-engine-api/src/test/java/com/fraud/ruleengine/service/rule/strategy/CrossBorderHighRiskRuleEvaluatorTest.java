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

class CrossBorderHighRiskRuleEvaluatorTest {

    private CrossBorderHighRiskRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new CrossBorderHighRiskRuleEvaluator();
    }

    @Test
    void shouldSupport_CrossBorderHighRiskRuleType() {
        assertThat(evaluator.supports(RuleType.CROSS_BORDER_HIGH_RISK)).isTrue();
        assertThat(evaluator.supports(RuleType.AMOUNT_THRESHOLD)).isFalse();
    }

    @Test
    void shouldTrigger_whenCrossBorderToHighRiskCountry() {
        Rule rule = createRule("ZAF", "RUS");
        Transaction transaction = createTransaction("RUS");

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();
        assertThat(result.get().matchReason()).contains("Cross-border transaction to high-risk country");
        assertThat(result.get().riskScore()).isEqualTo(90);
    }

    @Test
    void shouldNotTrigger_whenTransactionInHomeCountry() {
        Rule rule = createRule("ZAF", "RUS");
        Transaction transaction = createTransaction("ZAF");

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenTransactionNotInConfiguredHighRiskCountry() {
        Rule rule = createRule("ZAF", "RUS");
        Transaction transaction = createTransaction("USA");

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenConfigIsNull() {
        Rule rule = createRule(null, "RUS");
        Transaction transaction = createTransaction("RUS");

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    private Rule createRule(String customerHomeCountry, String highRiskCountry) {
        return Rule.builder()
            .id(1L)
            .name("Test Rule")
            .ruleType(RuleType.CROSS_BORDER_HIGH_RISK)
            .enabled(true)
            .priority(185)
            .customerHomeCountry(customerHomeCountry)
            .countryCode(highRiskCountry)
            .build();
    }

    private Transaction createTransaction(String countryCode) {
        return new Transaction(
            "TXN-001",
            "ACC-001",
            "CUST-001",
            new BigDecimal("5000"),
            "ZAR",
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
