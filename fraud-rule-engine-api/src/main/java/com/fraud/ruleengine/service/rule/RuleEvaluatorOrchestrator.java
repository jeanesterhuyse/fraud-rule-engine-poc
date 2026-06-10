package com.fraud.ruleengine.service.rule;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.entity.TriggeredTransaction;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import com.fraud.ruleengine.repository.RuleRepository;
import com.fraud.ruleengine.repository.TriggeredTransactionRepository;
import com.fraud.ruleengine.service.rule.strategy.RuleEvaluationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates rule evaluation for incoming transactions.
 *
 * This service:
 * 1. Loads all active rules (ordered by priority DESC)
 * 2. Evaluates each rule against the transaction
 * 3. Collects all matches
 * 4. Persists triggered_transactions records
 *
 * Design:
 * - Uses Strategy Pattern with Spring dependency injection
 * - All RuleEvaluationStrategy beans are auto-discovered
 * - Adding new rule types requires only new @Component implementation
 * - Transaction: ALL rules are evaluated (no short-circuit)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluatorOrchestrator {

    private final RuleRepository ruleRepository;
    private final TriggeredTransactionRepository triggeredTransactionRepository;
    private final List<RuleEvaluationStrategy> strategies;

    /**
     * Evaluates a transaction against all active rules.
     *
     * @param transaction The transaction to evaluate
     * @return List of persisted TriggeredTransaction records (one per rule match)
     */
    @Transactional
    public List<TriggeredTransaction> evaluateTransaction(Transaction transaction) {
        log.debug("Evaluating transaction {} against all active rules", transaction.transactionId());

        // Load all enabled rules ordered by priority (highest first)
        List<Rule> activeRules = ruleRepository.findByEnabledTrueOrderByPriorityDesc();

        if (activeRules.isEmpty()) {
            log.warn("No active rules found in database");
            return List.of();
        }

        log.debug("Loaded {} active rules for evaluation", activeRules.size());

        List<RuleMatch> matches = new ArrayList<>();

        // Evaluate each rule (all rules are evaluated, no short-circuit)
        for (Rule rule : activeRules) {
            try {
                RuleEvaluationStrategy strategy = findStrategy(rule.getRuleType());

                if (strategy == null) {
                    log.error("No strategy found for rule type: {}. Rule {} will be skipped.",
                        rule.getRuleType(), rule.getId());
                    continue;
                }

                strategy.evaluate(transaction, rule).ifPresent(match -> {
                    matches.add(match);
                    log.info("Transaction {} triggered rule {} (type: {}, priority: {})",
                        transaction.transactionId(),
                        rule.getName(),
                        rule.getRuleType(),
                        rule.getPriority());
                });

            } catch (Exception e) {
                log.error("Error evaluating rule {} (type: {}) for transaction {}: {}",
                    rule.getId(),
                    rule.getRuleType(),
                    transaction.transactionId(),
                    e.getMessage(), e);
                // Continue with other rules even if one fails
            }
        }

        if (matches.isEmpty()) {
            log.debug("Transaction {} did not trigger any rules", transaction.transactionId());
            return List.of();
        }

        // Persist all matches
        log.info("Transaction {} triggered {} rule(s). Persisting to database.",
            transaction.transactionId(), matches.size());

        List<TriggeredTransaction> persistedRecords = matches.stream()
            .map(match -> createTriggeredTransaction(transaction, match))
            .map(triggeredTransactionRepository::save)
            .toList();

        log.info("Successfully persisted {} triggered transaction record(s) for transaction {}",
            persistedRecords.size(), transaction.transactionId());

        return persistedRecords;
    }

    /**
     * Finds the strategy that supports the given rule type.
     *
     * @param ruleType The rule type
     * @return The strategy, or null if not found
     */
    private RuleEvaluationStrategy findStrategy(RuleType ruleType) {
        return strategies.stream()
            .filter(strategy -> strategy.supports(ruleType))
            .findFirst()
            .orElse(null);
    }

    /**
     * Creates a TriggeredTransaction entity from transaction and rule match.
     *
     * @param transaction The original transaction
     * @param match The rule match result
     * @return Unsaved TriggeredTransaction entity
     */
    private TriggeredTransaction createTriggeredTransaction(Transaction transaction, RuleMatch match) {
        return TriggeredTransaction.builder()
            .rule(match.rule())
            .transactionId(transaction.transactionId())
            .accountId(transaction.accountId())
            .customerId(transaction.customerId())
            .amount(transaction.amount())
            .currency(transaction.currency())
            .merchantName(transaction.merchantName())
            .merchantCategory(transaction.merchantCategory())
            .transactionType(transaction.transactionType())
            .transactionTimestamp(transaction.transactionTimestamp())
            .countryCode(transaction.countryCode())
            .deviceId(transaction.deviceId())
            .ipAddress(transaction.ipAddress())
            .cardLastFour(transaction.cardLastFour())
            .matchReason(match.matchReason())
            .ruleName(match.rule().getName())
            .ruleType(match.rule().getRuleType())
            .riskScore(match.riskScore())
            .build();
    }

    /**
     * Returns the number of registered strategies.
     * Useful for health checks and diagnostics.
     *
     * @return Number of strategies
     */
    public int getRegisteredStrategyCount() {
        return strategies.size();
    }

    /**
     * Returns list of registered strategy class names.
     * Useful for diagnostics and logging.
     *
     * @return List of strategy class simple names
     */
    public List<String> getRegisteredStrategyNames() {
        return strategies.stream()
            .map(s -> s.getClass().getSimpleName())
            .sorted()
            .toList();
    }
}
