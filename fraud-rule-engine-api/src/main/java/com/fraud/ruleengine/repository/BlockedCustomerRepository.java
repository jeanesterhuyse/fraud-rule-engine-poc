package com.fraud.ruleengine.repository;

import com.fraud.ruleengine.domain.entity.BlockedCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for blocked customers.
 */
@Repository
public interface BlockedCustomerRepository extends JpaRepository<BlockedCustomer, String> {

    /**
     * Check if a customer is currently blocked (not expired).
     *
     * @param customerId Customer ID to check
     * @param now Current timestamp
     * @return true if customer is blocked and block hasn't expired
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM BlockedCustomer b " +
           "WHERE b.customerId = :customerId " +
           "AND (b.expiresAt IS NULL OR b.expiresAt > :now)")
    boolean isBlocked(@Param("customerId") String customerId, @Param("now") LocalDateTime now);
}
