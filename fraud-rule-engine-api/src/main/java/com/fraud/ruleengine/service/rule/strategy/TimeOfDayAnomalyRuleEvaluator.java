package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Evaluates TIME_OF_DAY_ANOMALY rules.
 *
 * Triggers when transaction occurs during unusual hours (e.g., 2 AM - 5 AM).
 *
 * Configuration:
 * - start_hour: Start of unusual period (0-23)
 * - end_hour: End of unusual period (0-23)
 *
 * Risk Score: Fixed at 60 (moderate risk - unusual hours may indicate unauthorized access)
 *
 * Note: Handles midnight wrap-around (e.g., 22:00 to 02:00 spans midnight)
 */
@Component
@Slf4j
public class TimeOfDayAnomalyRuleEvaluator implements RuleEvaluationStrategy {

    private static final int RISK_SCORE = 60;

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.TIME_OF_DAY_ANOMALY;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getStartHour() == null || rule.getEndHour() == null) {
            log.warn("Time of day anomaly rule {} has no start_hour or end_hour configured", rule.getId());
            return Optional.empty();
        }

        int hour = transaction.transactionTimestamp().getHour();
        int startHour = rule.getStartHour();
        int endHour = rule.getEndHour();

        boolean triggeredInRange;

        // Handle midnight wrap-around (e.g., 22:00 to 02:00)
        if (startHour <= endHour) {
            // Normal range (e.g., 02:00 to 05:00)
            triggeredInRange = hour >= startHour && hour <= endHour;
        } else {
            // Wrap-around range (e.g., 22:00 to 02:00)
            triggeredInRange = hour >= startHour || hour <= endHour;
        }

        if (triggeredInRange) {
            String reason = String.format(
                "Transaction at %02d:00 during unusual hours (%02d:00 - %02d:00)",
                hour, startHour, endHour
            );

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            return Optional.of(new RuleMatch(rule, reason, RISK_SCORE));
        }

        return Optional.empty();
    }
}
