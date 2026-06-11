package com.fraud.ruleengine.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a blocked merchant.
 *
 * Merchants on this list are instantly blocked from all transactions
 * via the MERCHANT_BLOCKLIST rule type.
 */
@Entity
@Table(name = "blocked_merchants", indexes = {
    @Index(name = "idx_blocked_merchants_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedMerchant {

    @Id
    @Column(name = "merchant_name", length = 255)
    @NotBlank(message = "Merchant name is required")
    @Size(max = 255, message = "Merchant name must not exceed 255 characters")
    private String merchantName;

    @Column(name = "blocked_at", nullable = false)
    private LocalDateTime blockedAt;

    @Column(name = "blocked_by", length = 100)
    @Size(max = 100, message = "Blocked by must not exceed 100 characters")
    private String blockedBy;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Reason is required")
    private String reason;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (blockedAt == null) {
            blockedAt = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockedMerchant that)) return false;
        return merchantName != null && merchantName.equals(that.merchantName);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "BlockedMerchant{" +
                "merchantName='" + merchantName + '\'' +
                ", blockedAt=" + blockedAt +
                ", reason='" + reason + '\'' +
                '}';
    }
}
