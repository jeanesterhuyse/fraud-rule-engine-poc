package com.fraud.ruleengine.repository;

import com.fraud.ruleengine.domain.entity.TriggeredTransaction;
import com.fraud.ruleengine.domain.enums.RuleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for TriggeredTransaction entities.
 *
 * Provides CRUD operations and custom query methods for fraud investigation
 * and dashboard metrics.
 */
@Repository
public interface TriggeredTransactionRepository extends JpaRepository<TriggeredTransaction, Long> {

    // Basic queries

    /**
     * Finds all triggered transactions for a specific rule.
     *
     * @param ruleId The rule ID
     * @param pageable Pagination information
     * @return Page of triggered transactions
     */
    Page<TriggeredTransaction> findByRuleId(Long ruleId, Pageable pageable);

    /**
     * Finds all triggered transactions for a specific customer.
     *
     * @param customerId The customer ID
     * @param pageable Pagination information
     * @return Page of triggered transactions
     */
    Page<TriggeredTransaction> findByCustomerId(String customerId, Pageable pageable);

    /**
     * Finds all triggered transactions for a specific account.
     *
     * @param accountId The account ID
     * @param pageable Pagination information
     * @return Page of triggered transactions
     */
    Page<TriggeredTransaction> findByAccountId(String accountId, Pageable pageable);

    /**
     * Finds triggered transactions by transaction ID.
     * A transaction may have multiple records if it triggered multiple rules.
     *
     * @param transactionId The transaction ID
     * @return List of triggered transaction records
     */
    List<TriggeredTransaction> findByTransactionId(String transactionId);

    /**
     * Finds triggered transactions by rule type.
     *
     * @param ruleType The rule type
     * @param pageable Pagination information
     * @return Page of triggered transactions
     */
    Page<TriggeredTransaction> findByRuleType(RuleType ruleType, Pageable pageable);

    // Time-based queries (for velocity rules and dashboard)

    /**
     * Counts transactions for a customer after a specific timestamp.
     * Used by velocity rule evaluators.
     *
     * @param customerId The customer ID
     * @param timestamp The start timestamp
     * @return Count of transactions
     */
    long countByCustomerIdAndTransactionTimestampAfter(String customerId, LocalDateTime timestamp);

    /**
     * Counts transactions for an account after a specific timestamp.
     * Used by rapid-fire and dormant account rule evaluators.
     *
     * @param accountId The account ID
     * @param timestamp The start timestamp
     * @return Count of transactions
     */
    long countByAccountIdAndTransactionTimestampAfter(String accountId, LocalDateTime timestamp);

    /**
     * Finds triggered transactions after a specific timestamp.
     * Used for "recent activity" dashboard widgets.
     *
     * @param timestamp The start timestamp
     * @param pageable Pagination information
     * @return Page of recent triggered transactions
     */
    Page<TriggeredTransaction> findByTriggeredAtAfter(LocalDateTime timestamp, Pageable pageable);

    /**
     * Finds the most recent transaction for a customer before a specific timestamp.
     * Used by dormant account rule evaluator.
     *
     * @param customerId The customer ID
     * @param timestamp The end timestamp
     * @return The most recent triggered transaction, or null if none found
     */
    @Query("SELECT t FROM TriggeredTransaction t WHERE t.customerId = :customerId " +
           "AND t.transactionTimestamp < :timestamp " +
           "ORDER BY t.transactionTimestamp DESC LIMIT 1")
    TriggeredTransaction findMostRecentBeforeTimestamp(
        @Param("customerId") String customerId,
        @Param("timestamp") LocalDateTime timestamp
    );

    // Advanced filtering

    /**
     * Finds triggered transactions by multiple filters.
     * Supports complex filtering in the UI.
     *
     * @param ruleType Optional rule type filter
     * @param customerId Optional customer ID filter
     * @param accountId Optional account ID filter
     * @param minAmount Optional minimum amount filter
     * @param maxAmount Optional maximum amount filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @param pageable Pagination information
     * @return Page of matching triggered transactions
     */
    @Query("SELECT t FROM TriggeredTransaction t WHERE " +
           "(:ruleType IS NULL OR t.ruleType = :ruleType) AND " +
           "(:customerId IS NULL OR t.customerId = :customerId) AND " +
           "(:accountId IS NULL OR t.accountId = :accountId) AND " +
           "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR t.amount <= :maxAmount) AND " +
           "(:startDate IS NULL OR t.triggeredAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.triggeredAt <= :endDate)")
    Page<TriggeredTransaction> findByFilters(
        @Param("ruleType") RuleType ruleType,
        @Param("customerId") String customerId,
        @Param("accountId") String accountId,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Full-text search across transaction ID, customer ID, account ID, and merchant name.
     *
     * @param searchTerm The search term
     * @param pageable Pagination information
     * @return Page of matching triggered transactions
     */
    @Query("SELECT t FROM TriggeredTransaction t WHERE " +
           "LOWER(t.transactionId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.customerId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.accountId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.merchantName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<TriggeredTransaction> search(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Dashboard metrics queries

    /**
     * Counts total triggered transactions.
     *
     * @return Total count
     */
    @Query("SELECT COUNT(t) FROM TriggeredTransaction t")
    long countTotal();

    /**
     * Counts triggered transactions within a date range.
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Count of triggered transactions
     */
    long countByTriggeredAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Calculates average risk score.
     *
     * @return Average risk score, or 0 if no transactions
     */
    @Query("SELECT COALESCE(AVG(t.riskScore), 0) FROM TriggeredTransaction t WHERE t.riskScore IS NOT NULL")
    Double calculateAverageRiskScore();

    /**
     * Finds the highest risk score.
     *
     * @return Maximum risk score, or 0 if no transactions
     */
    @Query("SELECT COALESCE(MAX(t.riskScore), 0) FROM TriggeredTransaction t WHERE t.riskScore IS NOT NULL")
    Integer findMaxRiskScore();

    /**
     * Calculates sum of all flagged amounts.
     *
     * @return Total amount flagged
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TriggeredTransaction t")
    BigDecimal calculateTotalFlaggedAmount();

    /**
     * Groups triggered transactions by rule type with counts.
     * Used for pie chart on dashboard.
     *
     * @return List of [RuleType, Count] tuples
     */
    @Query("SELECT t.ruleType, COUNT(t) FROM TriggeredTransaction t GROUP BY t.ruleType")
    List<Object[]> countByRuleTypeGrouped();

    /**
     * Finds most frequently triggered rules.
     * Used for "Top Triggered Rules" dashboard widget.
     *
     * @param startDate Optional start date filter
     * @param pageable Pagination (includes limit)
     * @return List of [Rule ID, Rule Name, Count, Total Amount, Avg Risk Score] tuples
     */
    @Query("SELECT t.rule.id, t.ruleName, COUNT(t), SUM(t.amount), AVG(t.riskScore) " +
           "FROM TriggeredTransaction t " +
           "WHERE :startDate IS NULL OR t.triggeredAt >= :startDate " +
           "GROUP BY t.rule.id, t.ruleName " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> findTopTriggeredRules(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    /**
     * Finds customers with highest risk activity.
     * Used for "Customer Risk Leaderboard" dashboard widget.
     *
     * @param startDate Optional start date filter
     * @param pageable Pagination (includes limit)
     * @return List of [Customer ID, Count, Total Amount, Avg Risk Score, Max Timestamp] tuples
     */
    @Query("SELECT t.customerId, COUNT(t), SUM(t.amount), AVG(t.riskScore), MAX(t.triggeredAt) " +
           "FROM TriggeredTransaction t " +
           "WHERE :startDate IS NULL OR t.triggeredAt >= :startDate " +
           "GROUP BY t.customerId " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> findTopRiskCustomers(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    /**
     * Gets trigger trends by hour/day/week/month.
     * Used for trend chart on dashboard.
     *
     * @param startDate Start date
     * @param endDate End date
     * @return List of [Timestamp (truncated), Count, Total Amount, Avg Risk Score] tuples
     */
    @Query("SELECT " +
           "FUNCTION('DATE_TRUNC', 'hour', t.triggeredAt), " +
           "COUNT(t), " +
           "SUM(t.amount), " +
           "AVG(t.riskScore) " +
           "FROM TriggeredTransaction t " +
           "WHERE t.triggeredAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_TRUNC', 'hour', t.triggeredAt) " +
           "ORDER BY FUNCTION('DATE_TRUNC', 'hour', t.triggeredAt)")
    List<Object[]> findTrendsByHour(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
