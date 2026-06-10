package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Evaluates MERCHANT_RISK rules.
 *
 * Triggers when transaction occurs at a high-risk merchant category.
 *
 * Configuration:
 * - merchant_category: High-risk category (e.g., GAMBLING, CRYPTO_EXCHANGE)
 *
 * Risk Score Calculation:
 * - GAMBLING: 70
 * - CRYPTO_EXCHANGE: 65
 * - Other: 60
 */
@Component
@Slf4j
public class MerchantRiskRuleEvaluator implements RuleEvaluationStrategy {

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.MERCHANT_RISK;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getMerchantCategory() == null || rule.getMerchantCategory().isBlank()) {
            log.warn("Merchant risk rule {} has no merchant_category configured", rule.getId());
            return Optional.empty();
        }

        if (transaction.merchantCategory() == null) {
            // Transaction has no merchant category, cannot evaluate
            return Optional.empty();
        }

        if (transaction.merchantCategory().equalsIgnoreCase(rule.getMerchantCategory())) {
            String reason = String.format(
                "Transaction at high-risk merchant category: %s (merchant: %s)",
                transaction.merchantCategory(),
                transaction.merchantName() != null ? transaction.merchantName() : "Unknown"
            );

            int riskScore = calculateRiskScore(rule.getMerchantCategory());

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            return Optional.of(new RuleMatch(rule, reason, riskScore));
        }

        return Optional.empty();
    }

    private int calculateRiskScore(String merchantCategory) {
        return switch (merchantCategory.toUpperCase()) {
            case "GAMBLING" -> 70;
            case "CRYPTO_EXCHANGE", "CRYPTOCURRENCY" -> 65;
            case "ADULT_ENTERTAINMENT" -> 75;
            case "OFFSHORE_GAMBLING" -> 80;
            default -> 60;
        };
    }
}
