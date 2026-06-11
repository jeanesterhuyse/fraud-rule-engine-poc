package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.enums.TransactionType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import com.fraud.ruleengine.repository.BlockedCustomerRepository;
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
class CustomerBlocklistRuleEvaluatorTest {

    @Mock
    private BlockedCustomerRepository blockedCustomerRepository;

    private CustomerBlocklistRuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new CustomerBlocklistRuleEvaluator(blockedCustomerRepository);
    }

    @Test
    void shouldSupport_CustomerBlocklistRuleType() {
        assertThat(evaluator.supports(RuleType.CUSTOMER_BLOCKLIST)).isTrue();
        assertThat(evaluator.supports(RuleType.AMOUNT_THRESHOLD)).isFalse();
    }

    @Test
    void shouldTrigger_whenCustomerIsBlocked() {
        Rule rule = createRule();
        Transaction transaction = createTransaction("BLOCKED-CUST-001");

        when(blockedCustomerRepository.isBlocked(eq("BLOCKED-CUST-001"), any(LocalDateTime.class)))
            .thenReturn(true);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isPresent();
        assertThat(result.get().matchReason()).contains("blocklist");
        assertThat(result.get().riskScore()).isEqualTo(100);
    }

    @Test
    void shouldNotTrigger_whenCustomerIsNotBlocked() {
        Rule rule = createRule();
        Transaction transaction = createTransaction("GOOD-CUST-001");

        when(blockedCustomerRepository.isBlocked(eq("GOOD-CUST-001"), any(LocalDateTime.class)))
            .thenReturn(false);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotTrigger_whenCustomerIdIsNull() {
        Rule rule = createRule();
        Transaction transaction = createTransaction(null);

        Optional<RuleMatch> result = evaluator.evaluate(transaction, rule);

        assertThat(result).isEmpty();
    }

    private Rule createRule() {
        return Rule.builder()
            .id(1L)
            .name("Test Rule")
            .ruleType(RuleType.CUSTOMER_BLOCKLIST)
            .enabled(true)
            .priority(1000)
            .build();
    }

    private Transaction createTransaction(String customerId) {
        return new Transaction(
            "TXN-001",
            "ACC-001",
            customerId,
            new BigDecimal("1000"),
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
