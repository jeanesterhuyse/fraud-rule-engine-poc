package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Evaluates CURRENCY_MISMATCH rules.
 *
 * Triggers when transaction currency doesn't match customer's home currency
 * AND transaction occurs in a foreign country. May indicate unauthorized card use.
 *
 * Configuration:
 * - customer_home_country: Customer's home country (e.g., "ZAF")
 * - customer_home_currency: Customer's home currency (e.g., "ZAR")
 *
 * Risk Score: Fixed at 55 (moderate risk)
 */
@Component
@Slf4j
public class CurrencyMismatchRuleEvaluator implements RuleEvaluationStrategy {

    private static final int RISK_SCORE = 55;

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.CURRENCY_MISMATCH;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getCustomerHomeCountry() == null || rule.getCustomerHomeCountry().isBlank() ||
            rule.getCustomerHomeCurrency() == null || rule.getCustomerHomeCurrency().isBlank()) {
            log.warn("Currency mismatch rule {} has no customer_home_country or customer_home_currency configured",
                rule.getId());
            return Optional.empty();
        }

        String transactionCurrency = transaction.currency();
        String transactionCountry = transaction.countryCode();

        if (transactionCurrency == null || transactionCountry == null) {
            return Optional.empty();
        }

        // Check both conditions:
        // 1. Currency doesn't match home currency
        // 2. Transaction is in a foreign country
        boolean currencyMismatch = !transactionCurrency.equalsIgnoreCase(rule.getCustomerHomeCurrency());
        boolean foreignCountry = !transactionCountry.equalsIgnoreCase(rule.getCustomerHomeCountry());

        if (currencyMismatch && foreignCountry) {
            String reason = String.format(
                "Foreign currency transaction: %s %s in %s (customer home: %s, %s)",
                transaction.amount(),
                transactionCurrency,
                transactionCountry,
                rule.getCustomerHomeCountry(),
                rule.getCustomerHomeCurrency()
            );

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            return Optional.of(new RuleMatch(rule, reason, RISK_SCORE));
        }

        return Optional.empty();
    }
}
