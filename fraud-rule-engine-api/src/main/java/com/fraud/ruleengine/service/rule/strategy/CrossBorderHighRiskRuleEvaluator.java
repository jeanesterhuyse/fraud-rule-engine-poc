package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Evaluates CROSS_BORDER_HIGH_RISK rules.
 *
 * Triggers when customer transacts in a high-risk foreign country.
 * This is more severe than simple GEOGRAPHIC_ANOMALY as it combines
 * both cross-border AND high-risk country indicators.
 *
 * Configuration:
 * - customer_home_country: Customer's home country (e.g., "ZAF")
 * - country_code: High-risk destination country (e.g., "RUS")
 *
 * Risk Score: Fixed at 90 (very high risk)
 */
@Component
@Slf4j
public class CrossBorderHighRiskRuleEvaluator implements RuleEvaluationStrategy {

    private static final int RISK_SCORE = 90;

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.CROSS_BORDER_HIGH_RISK;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getCustomerHomeCountry() == null || rule.getCustomerHomeCountry().isBlank() ||
            rule.getCountryCode() == null || rule.getCountryCode().isBlank()) {
            log.warn("Cross-border high risk rule {} has no customer_home_country or country_code configured",
                rule.getId());
            return Optional.empty();
        }

        String transactionCountry = transaction.countryCode();

        if (transactionCountry == null || transactionCountry.isBlank()) {
            return Optional.empty();
        }

        // Check both conditions:
        // 1. Transaction is NOT in customer's home country (cross-border)
        // 2. Transaction IS in the configured high-risk country
        boolean isCrossBorder = !transactionCountry.equalsIgnoreCase(rule.getCustomerHomeCountry());
        boolean isHighRiskCountry = transactionCountry.equalsIgnoreCase(rule.getCountryCode());

        if (isCrossBorder && isHighRiskCountry) {
            String reason = String.format(
                "Cross-border transaction to high-risk country %s (customer home: %s) - %s %s",
                transactionCountry,
                rule.getCustomerHomeCountry(),
                transaction.amount(),
                transaction.currency()
            );

            log.warn("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            return Optional.of(new RuleMatch(rule, reason, RISK_SCORE));
        }

        return Optional.empty();
    }
}
