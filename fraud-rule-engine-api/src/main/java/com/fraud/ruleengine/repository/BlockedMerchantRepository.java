package com.fraud.ruleengine.repository;

import com.fraud.ruleengine.domain.entity.BlockedMerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for blocked merchants.
 */
@Repository
public interface BlockedMerchantRepository extends JpaRepository<BlockedMerchant, String> {

    /**
     * Check if a merchant is currently blocked (not expired).
     *
     * @param merchantName Merchant name to check
     * @param now Current timestamp
     * @return true if merchant is blocked and block hasn't expired
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM BlockedMerchant b " +
           "WHERE b.merchantName = :merchantName " +
           "AND (b.expiresAt IS NULL OR b.expiresAt > :now)")
    boolean isBlocked(@Param("merchantName") String merchantName, @Param("now") LocalDateTime now);
}
