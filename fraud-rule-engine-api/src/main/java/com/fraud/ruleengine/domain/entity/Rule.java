package com.fraud.ruleengine.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fraud.ruleengine.domain.enums.RuleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity representing a fraud detection rule.
 *
 * Rules are stored in the database with typed configuration parameters
 * (no JSON blobs) for queryability, type safety, and indexing performance.
 *
 * Design: Nullable columns support different rule types. Each rule type
 * uses only the parameters relevant to its evaluation logic.
 */
@Entity
@Table(name = "rules", indexes = {
    @Index(name = "idx_rules_enabled", columnList = "enabled"),
    @Index(name = "idx_rules_rule_type", columnList = "rule_type"),
    @Index(name = "idx_rules_priority", columnList = "priority")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Rule name is required")
    @Size(max = 255, message = "Rule name must not exceed 255 characters")
    private String name;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Column(name = "rule_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Rule type is required")
    private RuleType ruleType;

    @Column(nullable = false)
    @NotNull(message = "Enabled status is required")
    private Boolean enabled = true;

    @Column(nullable = false)
    @NotNull(message = "Priority is required")
    @Min(value = 0, message = "Priority must be at least 0")
    @Max(value = 1000, message = "Priority must not exceed 1000")
    private Integer priority = 100;

    // Rule-specific parameters (nullable - only used by specific rule types)

    @Column(name = "threshold_amount", precision = 19, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Threshold amount must be positive")
    private BigDecimal thresholdAmount;

    @Column(name = "threshold_count")
    @Positive(message = "Threshold count must be positive")
    private Integer thresholdCount;

    @Column(name = "time_window_minutes")
    @Positive(message = "Time window must be positive")
    private Integer timeWindowMinutes;

    @Column(name = "merchant_category", length = 100)
    @Size(max = 100, message = "Merchant category must not exceed 100 characters")
    private String merchantCategory;

    @Column(name = "country_code", length = 3)
    @Size(min = 3, max = 3, message = "Country code must be exactly 3 characters (ISO 3166-1 alpha-3)")
    @Pattern(regexp = "[A-Z]{3}", message = "Country code must be 3 uppercase letters")
    private String countryCode;

    @Column(name = "min_amount", precision = 19, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Min amount must be positive")
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 19, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Max amount must be positive")
    private BigDecimal maxAmount;

    // Audit fields

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;

    // Relationship (lazy loaded to avoid N+1 queries)
    // NOTE: No cascade delete - we want to keep triggered transactions for audit history

    @JsonIgnore
    @OneToMany(mappedBy = "rule", fetch = FetchType.LAZY)
    @Builder.Default
    private List<TriggeredTransaction> triggeredTransactions = new ArrayList<>();

    // Custom validation methods

    /**
     * Validates that min_amount does not exceed max_amount when both are set.
     */
    @AssertTrue(message = "Min amount must not exceed max amount")
    private boolean isAmountRangeValid() {
        if (minAmount == null || maxAmount == null) {
            return true;
        }
        return minAmount.compareTo(maxAmount) <= 0;
    }

    /**
     * Validates that AMOUNT_THRESHOLD rules have threshold_amount set.
     */
    @AssertTrue(message = "Amount threshold rule requires threshold_amount")
    private boolean isAmountThresholdRuleValid() {
        if (ruleType == RuleType.AMOUNT_THRESHOLD) {
            return thresholdAmount != null;
        }
        return true;
    }

    /**
     * Validates that VELOCITY rules have required parameters.
     */
    @AssertTrue(message = "Velocity rule requires threshold_count and time_window_minutes")
    private boolean isVelocityRuleValid() {
        if (ruleType == RuleType.VELOCITY) {
            return thresholdCount != null && timeWindowMinutes != null;
        }
        return true;
    }

    /**
     * Validates that GEOGRAPHIC_ANOMALY rules have country_code set.
     */
    @AssertTrue(message = "Geographic anomaly rule requires country_code")
    private boolean isGeographicRuleValid() {
        if (ruleType == RuleType.GEOGRAPHIC_ANOMALY) {
            return countryCode != null && !countryCode.isBlank();
        }
        return true;
    }

    /**
     * Validates that MERCHANT_RISK rules have merchant_category set.
     */
    @AssertTrue(message = "Merchant risk rule requires merchant_category")
    private boolean isMerchantRiskRuleValid() {
        if (ruleType == RuleType.MERCHANT_RISK) {
            return merchantCategory != null && !merchantCategory.isBlank();
        }
        return true;
    }

    /**
     * Validates that AMOUNT_RANGE rules have min_amount and max_amount set.
     */
    @AssertTrue(message = "Amount range rule requires min_amount and max_amount")
    private boolean isAmountRangeRuleValid() {
        if (ruleType == RuleType.AMOUNT_RANGE) {
            return minAmount != null && maxAmount != null;
        }
        return true;
    }

    /**
     * Validates that RAPID_FIRE rules have required parameters.
     */
    @AssertTrue(message = "Rapid fire rule requires threshold_count and time_window_minutes")
    private boolean isRapidFireRuleValid() {
        if (ruleType == RuleType.RAPID_FIRE) {
            return thresholdCount != null && timeWindowMinutes != null;
        }
        return true;
    }

    // Lifecycle callbacks

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // equals, hashCode, toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rule rule)) return false;
        return id != null && id.equals(rule.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Rule{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ruleType=" + ruleType +
                ", enabled=" + enabled +
                ", priority=" + priority +
                '}';
    }
}
