package com.fraud.ruleengine.repository;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Rule entities.
 *
 * Provides CRUD operations and custom query methods for rule management.
 * All queries are optimized with appropriate indexes defined in the entity.
 */
@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {

    /**
     * Finds all enabled rules ordered by priority (descending).
     * This is the primary query used by the rule engine.
     *
     * Higher priority rules are evaluated first (priority 200 before priority 100).
     *
     * @return List of enabled rules in priority order
     */
    List<Rule> findByEnabledTrueOrderByPriorityDesc();

    /**
     * Finds all enabled rules of a specific type ordered by priority.
     *
     * @param ruleType The type of rules to find
     * @return List of enabled rules of the specified type
     */
    List<Rule> findByEnabledTrueAndRuleTypeOrderByPriorityDesc(RuleType ruleType);

    /**
     * Finds all rules of a specific type (enabled or disabled).
     *
     * @param ruleType The type of rules to find
     * @param pageable Pagination information
     * @return Page of rules
     */
    Page<Rule> findByRuleType(RuleType ruleType, Pageable pageable);

    /**
     * Finds rules by enabled status.
     *
     * @param enabled Whether to find enabled or disabled rules
     * @param pageable Pagination information
     * @return Page of rules
     */
    Page<Rule> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * Finds rules by name containing the given string (case-insensitive).
     *
     * @param name Part of the rule name to search for
     * @param pageable Pagination information
     * @return Page of matching rules
     */
    Page<Rule> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Finds rule by exact name (case-sensitive).
     * Useful for checking duplicate rule names.
     *
     * @param name The exact rule name
     * @return Optional containing the rule if found
     */
    Optional<Rule> findByName(String name);

    /**
     * Counts enabled rules.
     *
     * @return Number of enabled rules
     */
    long countByEnabledTrue();

    /**
     * Counts rules by type.
     *
     * @param ruleType The rule type
     * @return Number of rules of that type
     */
    long countByRuleType(RuleType ruleType);

    /**
     * Checks if a rule with the given name exists (excluding a specific rule ID).
     * Useful for update operations to check for duplicate names.
     *
     * @param name The rule name to check
     * @param id The ID to exclude from the check
     * @return true if a different rule with this name exists
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Finds rules with priority greater than or equal to the specified value.
     *
     * @param priority Minimum priority
     * @param pageable Pagination information
     * @return Page of rules
     */
    Page<Rule> findByPriorityGreaterThanEqual(Integer priority, Pageable pageable);

    /**
     * Custom query to find rules by multiple criteria.
     * Useful for advanced filtering in the UI.
     *
     * @param ruleType Optional rule type filter
     * @param enabled Optional enabled status filter
     * @param minPriority Optional minimum priority filter
     * @param pageable Pagination information
     * @return Page of matching rules
     */
    @Query("SELECT r FROM Rule r WHERE " +
           "(:ruleType IS NULL OR r.ruleType = :ruleType) AND " +
           "(:enabled IS NULL OR r.enabled = :enabled) AND " +
           "(:minPriority IS NULL OR r.priority >= :minPriority)")
    Page<Rule> findByFilters(
        @Param("ruleType") RuleType ruleType,
        @Param("enabled") Boolean enabled,
        @Param("minPriority") Integer minPriority,
        Pageable pageable
    );

    /**
     * Finds recently updated rules.
     * Useful for audit and change tracking.
     *
     * @param pageable Pagination information (should include sort by updatedAt DESC)
     * @return Page of recently updated rules
     */
    @Query("SELECT r FROM Rule r ORDER BY r.updatedAt DESC")
    Page<Rule> findRecentlyUpdated(Pageable pageable);
}
