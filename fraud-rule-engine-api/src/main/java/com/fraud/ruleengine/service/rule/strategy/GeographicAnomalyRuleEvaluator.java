package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Evaluates GEOGRAPHIC_ANOMALY rules.
 *
 * Triggers when transaction originates from a high-risk country.
 *
 * Configuration:
 * - country_code: ISO 3166-1 alpha-3 country code (e.g., RUS, PRK, CHN)
 *
 * Risk Score: Fixed at 80 (high-risk countries are serious red flags)
 */
@Component
@Slf4j
public class GeographicAnomalyRuleEvaluator implements RuleEvaluationStrategy {

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.GEOGRAPHIC_ANOMALY;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getCountryCode() == null || rule.getCountryCode().isBlank()) {
            log.warn("Geographic anomaly rule {} has no country_code configured", rule.getId());
            return Optional.empty();
        }

        if (transaction.countryCode() == null) {
            // Transaction has no country code, cannot evaluate
            return Optional.empty();
        }

        if (transaction.countryCode().equalsIgnoreCase(rule.getCountryCode())) {
            String reason = String.format(
                "Transaction originated from high-risk country: %s",
                transaction.countryCode()
            );

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            // Geographic anomalies are high risk
            return Optional.of(new RuleMatch(rule, reason, 80));
        }

        return Optional.empty();
    }
}
