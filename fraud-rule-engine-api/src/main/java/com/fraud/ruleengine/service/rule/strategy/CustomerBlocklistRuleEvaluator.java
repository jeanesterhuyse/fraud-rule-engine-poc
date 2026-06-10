package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import com.fraud.ruleengine.repository.BlockedCustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Evaluates CUSTOMER_BLOCKLIST rules.
 *
 * Triggers when customer is on the blocklist. This is a critical rule with
 * the highest priority - should be evaluated first for instant blocking.
 *
 * Configuration: None (checks blocked_customers table)
 *
 * Risk Score: Fixed at 100 (critical - instant block)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerBlocklistRuleEvaluator implements RuleEvaluationStrategy {

    private static final int RISK_SCORE = 100;

    private final BlockedCustomerRepository blockedCustomerRepository;

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.CUSTOMER_BLOCKLIST;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        String customerId = transaction.customerId();

        if (customerId == null || customerId.isBlank()) {
            log.warn("Transaction {} has no customer ID for blocklist check", transaction.transactionId());
            return Optional.empty();
        }

        // Check if customer is blocked (and block hasn't expired)
        boolean isBlocked = blockedCustomerRepository.isBlocked(customerId, LocalDateTime.now());

        if (isBlocked) {
            String reason = String.format(
                "Customer %s is on the blocklist - transaction instantly blocked",
                customerId
            );

            log.warn("Rule {} triggered for transaction {}: BLOCKED CUSTOMER {}",
                rule.getName(), transaction.transactionId(), customerId);

            return Optional.of(new RuleMatch(rule, reason, RISK_SCORE));
        }

        return Optional.empty();
    }
}
