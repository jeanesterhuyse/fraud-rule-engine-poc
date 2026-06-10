package com.fraud.ruleengine.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a blocked customer.
 *
 * Customers on this list are instantly blocked from all transactions
 * via the CUSTOMER_BLOCKLIST rule type.
 */
@Entity
@Table(name = "blocked_customers", indexes = {
    @Index(name = "idx_blocked_customers_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedCustomer {

    @Id
    @Column(name = "customer_id", length = 100)
    @NotBlank(message = "Customer ID is required")
    @Size(max = 100, message = "Customer ID must not exceed 100 characters")
    private String customerId;

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
        if (!(o instanceof BlockedCustomer that)) return false;
        return customerId != null && customerId.equals(that.customerId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "BlockedCustomer{" +
                "customerId='" + customerId + '\'' +
                ", blockedAt=" + blockedAt +
                ", reason='" + reason + '\'' +
                '}';
    }
}
