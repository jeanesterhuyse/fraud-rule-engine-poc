package com.fraud.ruleengine.repository;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION_TESTS", matches = "true", disabledReason = "Integration tests require Docker and RUN_INTEGRATION_TESTS=true")
class RuleRepositoryTest {

    @Autowired
    private RuleRepository ruleRepository;

    @Test
    void shouldFindEnabledRules_orderedByPriorityDesc() {
        // Given
        createRule("Rule1", RuleType.AMOUNT_THRESHOLD, true, 100);
        createRule("Rule2", RuleType.CUSTOMER_BLOCKLIST, true, 200);
        createRule("Rule3", RuleType.MERCHANT_RISK, false, 150);

        // When
        List<Rule> rules = ruleRepository.findByEnabledTrueOrderByPriorityDesc();

        // Then
        assertThat(rules).hasSize(2);
        assertThat(rules.get(0).getPriority()).isEqualTo(200);
        assertThat(rules.get(1).getPriority()).isEqualTo(100);
    }

    @Test
    void shouldCountEnabledRules() {
        // Given
        createRule("Rule1", RuleType.AMOUNT_THRESHOLD, true, 100);
        createRule("Rule2", RuleType.GEOGRAPHIC_ANOMALY, true, 200);
        createRule("Rule3", RuleType.MERCHANT_RISK, false, 150);

        // When
        long count = ruleRepository.countByEnabledTrue();

        // Then
        assertThat(count).isEqualTo(2);
    }

    private void createRule(String name, RuleType type, boolean enabled, int priority) {
        Rule rule = Rule.builder()
            .name(name)
            .description("Test rule")
            .ruleType(type)
            .enabled(enabled)
            .priority(priority)
            .thresholdAmount(new BigDecimal("10000"))
            .build();
        ruleRepository.save(rule);
    }
}
