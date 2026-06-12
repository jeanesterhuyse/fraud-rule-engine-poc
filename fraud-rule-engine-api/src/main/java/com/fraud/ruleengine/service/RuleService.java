package com.fraud.ruleengine.service;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.exception.RuleNotFoundException;
import com.fraud.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for rule management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleService {

    private final RuleRepository ruleRepository;

    @Transactional(readOnly = true)
    public Page<Rule> findAll(Pageable pageable) {
        return ruleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Rule findById(Long id) {
        return ruleRepository.findById(id)
            .orElseThrow(() -> new RuleNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Rule> findByFilters(RuleType ruleType, Boolean enabled, Integer minPriority, Pageable pageable) {
        return ruleRepository.findByFilters(ruleType, enabled, minPriority, pageable);
    }

    @Transactional
    public Rule create(Rule rule) {
        log.info("Creating new rule: {}", rule.getName());
        return ruleRepository.save(rule);
    }

    @Transactional
    public Rule update(Long id, Rule updatedRule) {
        Rule existingRule = findById(id);

        existingRule.setName(updatedRule.getName());
        existingRule.setDescription(updatedRule.getDescription());
        existingRule.setRuleType(updatedRule.getRuleType());
        existingRule.setEnabled(updatedRule.getEnabled());
        existingRule.setPriority(updatedRule.getPriority());

        // Clear all rule-specific fields first to prevent validation errors when changing rule types
        existingRule.setThresholdAmount(null);
        existingRule.setThresholdCount(null);
        existingRule.setTimeWindowMinutes(null);
        existingRule.setMerchantCategory(null);
        existingRule.setCountryCode(null);
        existingRule.setMinAmount(null);
        existingRule.setMaxAmount(null);
        existingRule.setStartHour(null);
        existingRule.setEndHour(null);
        existingRule.setMinimumAmount(null);
        existingRule.setRoundToNearest(null);
        existingRule.setCustomerHomeCountry(null);
        existingRule.setCustomerHomeCurrency(null);

        // Now set the new values from updatedRule
        existingRule.setThresholdAmount(updatedRule.getThresholdAmount());
        existingRule.setThresholdCount(updatedRule.getThresholdCount());
        existingRule.setTimeWindowMinutes(updatedRule.getTimeWindowMinutes());
        existingRule.setMerchantCategory(updatedRule.getMerchantCategory());
        existingRule.setCountryCode(updatedRule.getCountryCode());
        existingRule.setMinAmount(updatedRule.getMinAmount());
        existingRule.setMaxAmount(updatedRule.getMaxAmount());
        existingRule.setStartHour(updatedRule.getStartHour());
        existingRule.setEndHour(updatedRule.getEndHour());
        existingRule.setMinimumAmount(updatedRule.getMinimumAmount());
        existingRule.setRoundToNearest(updatedRule.getRoundToNearest());
        existingRule.setCustomerHomeCountry(updatedRule.getCustomerHomeCountry());
        existingRule.setCustomerHomeCurrency(updatedRule.getCustomerHomeCurrency());

        log.info("Updated rule: {}", existingRule.getName());
        return ruleRepository.save(existingRule);
    }

    @Transactional
    public void delete(Long id) {
        Rule rule = findById(id);
        log.info("Deleting rule: {} (id={})", rule.getName(), id);
        ruleRepository.delete(rule);
    }

    @Transactional
    public Rule enable(Long id) {
        Rule rule = findById(id);
        rule.setEnabled(true);
        log.info("Enabled rule: {}", rule.getName());
        return ruleRepository.save(rule);
    }

    @Transactional
    public Rule disable(Long id) {
        Rule rule = findById(id);
        rule.setEnabled(false);
        log.info("Disabled rule: {}", rule.getName());
        return ruleRepository.save(rule);
    }

    @Transactional(readOnly = true)
    public long countActiveRules() {
        return ruleRepository.countByEnabledTrue();
    }
}
