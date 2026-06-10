package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import com.fraud.ruleengine.repository.BlockedMerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Evaluates MERCHANT_BLOCKLIST rules.
 *
 * Triggers when merchant is on the blocklist. This is a critical rule with
 * very high priority - should be evaluated early for instant blocking.
 *
 * Configuration: None (checks blocked_merchants table)
 *
 * Risk Score: Fixed at 95 (critical)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MerchantBlocklistRuleEvaluator implements RuleEvaluationStrategy {

    private static final int RISK_SCORE = 95;

    private final BlockedMerchantRepository blockedMerchantRepository;

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.MERCHANT_BLOCKLIST;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        String merchantName = transaction.merchantName();

        if (merchantName == null || merchantName.isBlank()) {
            // No merchant name to check - skip this rule
            return Optional.empty();
        }

        // Check if merchant is blocked (and block hasn't expired)
        boolean isBlocked = blockedMerchantRepository.isBlocked(merchantName, LocalDateTime.now());

        if (isBlocked) {
            String reason = String.format(
                "Merchant '%s' is on the blocklist - transaction instantly blocked",
                merchantName
            );

            log.warn("Rule {} triggered for transaction {}: BLOCKED MERCHANT '{}'",
                rule.getName(), transaction.transactionId(), merchantName);

            return Optional.of(new RuleMatch(rule, reason, RISK_SCORE));
        }

        return Optional.empty();
    }
}
