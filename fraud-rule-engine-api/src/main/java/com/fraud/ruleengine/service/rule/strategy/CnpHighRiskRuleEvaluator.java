package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.enums.TransactionType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Evaluates CNP_HIGH_RISK (Card Not Present) rules.
 *
 * Triggers on purchase transactions at high-risk merchant categories.
 * Card-not-present (online/phone) transactions carry higher fraud risk,
 * especially for high-value resellable goods.
 *
 * Configuration:
 * - merchant_category: High-risk category (ELECTRONICS, JEWELRY, TRAVEL)
 *
 * Risk Score Calculation:
 * - JEWELRY: 75 (highest - very high resale value)
 * - ELECTRONICS: 65 (high - commonly targeted)
 * - TRAVEL: 60 (moderate - often used for card testing)
 */
@Component
@Slf4j
public class CnpHighRiskRuleEvaluator implements RuleEvaluationStrategy {

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.CNP_HIGH_RISK;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getMerchantCategory() == null || rule.getMerchantCategory().isBlank()) {
            log.warn("CNP high risk rule {} has no merchant_category configured", rule.getId());
            return Optional.empty();
        }

        // Only trigger on purchase transactions (card-not-present indicator)
        if (transaction.transactionType() != TransactionType.PURCHASE) {
            return Optional.empty();
        }

        String merchantCategory = transaction.merchantCategory();
        if (merchantCategory == null || merchantCategory.isBlank()) {
            return Optional.empty();
        }

        // Check if transaction matches the high-risk merchant category
        if (merchantCategory.equalsIgnoreCase(rule.getMerchantCategory())) {
            String reason = String.format(
                "Card-not-present transaction at high-risk merchant category '%s' (%s %s)",
                merchantCategory,
                transaction.amount(),
                transaction.currency()
            );

            int riskScore = calculateRiskScore(merchantCategory);

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            return Optional.of(new RuleMatch(rule, reason, riskScore));
        }

        return Optional.empty();
    }

    private int calculateRiskScore(String merchantCategory) {
        return switch (merchantCategory.toUpperCase()) {
            case "JEWELRY" -> 75;
            case "ELECTRONICS" -> 65;
            case "TRAVEL" -> 60;
            default -> 65; // Default for unknown high-risk categories
        };
    }
}
