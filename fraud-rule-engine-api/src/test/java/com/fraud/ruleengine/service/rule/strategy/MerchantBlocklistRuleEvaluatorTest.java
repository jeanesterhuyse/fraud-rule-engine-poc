package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.enums.TransactionType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import com.fraud.ruleengine.repository.BlockedMerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantBlocklistRuleEvaluatorTest {

    @Mock
    private BlockedMerchantRepository blockedMerchantRepository;

    private MerchantBlocklistRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new MerchantBlocklistRuleEvaluator(blockedMerchantRepository);
    }

    @Test
    void shouldSupport_MerchantBlocklistRuleType() {
        assertThat(evaluator.supports(RuleType.MERCHANT_BLOCKLIST)).isTrue();
        assertThat(evaluator.supports(RuleType.AMOUNT_THRESHOLD)).isFalse();
    }

    @Test
    void shouldTrigger_whenMerchantIsBlocked() {
        Rule rule = createRule();
        Transaction transaction = createTransaction("Fraudulent Merchant");

        when(blockedMerchantRepository.isBlocked(eq("Fraudulent Merchant"), any(LocalDateTime.class)))
            .thenReturn(true);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();
        assertThat(result.get().matchReason()).contains("blocklist");
        assertThat(result.get().riskScore()).isEqualTo(95);
    }

    @Test
    void shouldNotTrigger_whenMerchantIsNotBlocked() {
        Rule rule = createRule();
        Transaction transaction = createTransaction("Legitimate Merchant");

        when(blockedMerchantRepository.isBlocked(eq("Legitimate Merchant"), any(LocalDateTime.class)))
            .thenReturn(false);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenMerchantNameIsNull() {
        Rule rule = createRule();
        Transaction transaction = createTransaction(null);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    private Rule createRule() {
        return Rule.builder()
            .id(1L)
            .name("Test Rule")
            .ruleType(RuleType.MERCHANT_BLOCKLIST)
            .enabled(true)
            .priority(990)
            .build();
    }

    private Transaction createTransaction(String merchantName) {
        return new Transaction(
            "TXN-001",
            "ACC-001",
            "CUST-001",
            new BigDecimal("1000"),
            "ZAR",
            merchantName,
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
