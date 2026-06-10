package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;

import java.util.Optional;

/**
 * Strategy interface for rule evaluation.
 *
 * Each rule type has a corresponding implementation of this interface.
 * Implementations are Spring components that are automatically discovered
 * by the RuleEvaluatorOrchestrator.
 *
 * Design Pattern: Strategy Pattern with Spring dependency injection.
 *
 * To add a new rule type:
 * 1. Add enum value to RuleType
 * 2. Create @Component class implementing this interface
 * 3. Implement supports() and evaluate() methods
 * 4. No changes needed to orchestrator!
 */
public interface RuleEvaluationStrategy {

    /**
     * Determines if this strategy can evaluate the given rule type.
     *
     * @param ruleType The rule type to check
     * @return true if this strategy supports the rule type
     */
    boolean supports(RuleType ruleType);

    /**
     * Evaluates a transaction against a rule.
     *
     * @param transaction The transaction to evaluate
     * @param rule The rule to evaluate against
     * @return Optional containing RuleMatch if rule triggered, empty otherwise
     */
    Optional<RuleMatch> evaluate(Transaction transaction, Rule rule);
}
