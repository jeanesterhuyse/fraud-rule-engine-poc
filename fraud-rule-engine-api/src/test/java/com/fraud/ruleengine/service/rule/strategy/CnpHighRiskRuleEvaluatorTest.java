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

class CnpHighRiskRuleEvaluatorTest {

    private CnpHighRiskRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new CnpHighRiskRuleEvaluator();
    }

    @Test
    void shouldSupport_CnpHighRiskRuleType() {
        assertThat(evaluator.supports(RuleType.CNP_HIGH_RISK)).isTrue();
        assertThat(evaluator.supports(RuleType.AMOUNT_THRESHOLD)).isFalse();
    }

    @Test
    void shouldTrigger_forJewelryPurchase() {
        Rule rule = createRule("JEWELRY");
        Transaction transaction = createTransaction("JEWELRY", TransactionType.PURCHASE);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();
        assertThat(result.get().matchReason()).contains("Card-not-present");
        assertThat(result.get().riskScore()).isEqualTo(75);
    }

    @Test
    void shouldTrigger_forElectronicsPurchase() {
        Rule rule = createRule("ELECTRONICS");
        Transaction transaction = createTransaction("ELECTRONICS", TransactionType.PURCHASE);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();
        assertThat(result.get().riskScore()).isEqualTo(65);
    }

    @Test
    void shouldTrigger_forTravelPurchase() {
        Rule rule = createRule("TRAVEL");
        Transaction transaction = createTransaction("TRAVEL", TransactionType.PURCHASE);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();
        assertThat(result.get().riskScore()).isEqualTo(60);
    }

    @Test
    void shouldNotTrigger_forNonPurchaseTransaction() {
        Rule rule = createRule("ELECTRONICS");
        Transaction transaction = createTransaction("ELECTRONICS", TransactionType.WITHDRAWAL);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenMerchantCategoryDoesNotMatch() {
        Rule rule = createRule("ELECTRONICS");
        Transaction transaction = createTransaction("RETAIL", TransactionType.PURCHASE);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenConfigIsNull() {
        Rule rule = createRule(null);
        Transaction transaction = createTransaction("ELECTRONICS", TransactionType.PURCHASE);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    private Rule createRule(String merchantCategory) {
        return Rule.builder()
            .id(1L)
            .name("Test Rule")
            .ruleType(RuleType.CNP_HIGH_RISK)
            .enabled(true)
            .priority(100)
            .merchantCategory(merchantCategory)
            .build();
    }

    private Transaction createTransaction(String merchantCategory, TransactionType type) {
        return new Transaction(
            "TXN-001",
            "ACC-001",
            "CUST-001",
            new BigDecimal("5000"),
            "ZAR",
            "Test Merchant",
            merchantCategory,
            type,
            LocalDateTime.now(),
            "ZAF",
            "DEVICE-001",
            "192.168.1.1",
            "1234"
        );
    }
}
