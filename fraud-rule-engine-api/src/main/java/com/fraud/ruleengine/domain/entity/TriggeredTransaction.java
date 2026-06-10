package com.fraud.ruleengine.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a transaction that triggered a fraud rule.
 *
 * Design: One record per rule match. If a transaction triggers 3 rules,
 * there will be 3 separate TriggeredTransaction records with the same
 * transaction_id but different rule_id values.
 *
 * Denormalization: rule_name and rule_type are denormalized for historical
 * accuracy and query performance. If a rule is renamed or deleted, historical
 * records remain accurate.
 */
@Entity
@Table(name = "triggered_transactions", indexes = {
    @Index(name = "idx_triggered_rule_id", columnList = "rule_id"),
    @Index(name = "idx_triggered_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_triggered_customer_id", columnList = "customer_id"),
    @Index(name = "idx_triggered_account_id", columnList = "account_id"),
    @Index(name = "idx_triggered_at", columnList = "triggered_at"),
    @Index(name = "idx_triggered_rule_type", columnList = "rule_type"),
    @Index(name = "idx_triggered_timestamp", columnList = "transaction_timestamp"),
    @Index(name = "idx_triggered_rule_id_at", columnList = "rule_id, triggered_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggeredTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship to Rule
    // NOTE: Optional to allow historical transactions when rules are deleted

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "rule_id", nullable = true,
                foreignKey = @ForeignKey(name = "fk_triggered_rule"))
    private Rule rule;

    // Transaction core information

    @Column(name = "transaction_id", nullable = false, length = 100)
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;

    @Column(name = "account_id", nullable = false, length = 100)
    @NotBlank(message = "Account ID is required")
    @Size(max = 100, message = "Account ID must not exceed 100 characters")
    private String accountId;

    @Column(name = "customer_id", nullable = false, length = 100)
    @NotBlank(message = "Customer ID is required")
    @Size(max = 100, message = "Customer ID must not exceed 100 characters")
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters (ISO 4217)")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be 3 uppercase letters")
    private String currency;

    // Transaction details

    @Column(name = "merchant_name", length = 255)
    @Size(max = 255, message = "Merchant name must not exceed 255 characters")
    private String merchantName;

    @Column(name = "merchant_category", length = 100)
    @Size(max = 100, message = "Merchant category must not exceed 100 characters")
    private String merchantCategory;

    @Column(name = "transaction_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Column(name = "transaction_timestamp", nullable = false)
    @NotNull(message = "Transaction timestamp is required")
    @PastOrPresent(message = "Transaction timestamp cannot be in the future")
    private LocalDateTime transactionTimestamp;

    @Column(name = "country_code", length = 3)
    @Size(min = 3, max = 3, message = "Country code must be exactly 3 characters")
    @Pattern(regexp = "[A-Z]{3}", message = "Country code must be 3 uppercase letters")
    private String countryCode;

    @Column(name = "device_id", length = 100)
    @Size(max = 100, message = "Device ID must not exceed 100 characters")
    private String deviceId;

    @Column(name = "ip_address", length = 45)
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    private String ipAddress;

    @Column(name = "card_last_four", length = 4)
    @Size(min = 4, max = 4, message = "Card last four must be exactly 4 characters")
    @Pattern(regexp = "\\d{4}", message = "Card last four must be 4 digits")
    private String cardLastFour;

    // Rule match information (denormalized for performance and historical accuracy)

    @Column(name = "match_reason", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Match reason is required")
    @Size(max = 2000, message = "Match reason must not exceed 2000 characters")
    private String matchReason;

    @Column(name = "rule_name", nullable = false, length = 255)
    @NotBlank(message = "Rule name is required")
    @Size(max = 255, message = "Rule name must not exceed 255 characters")
    private String ruleName;

    @Column(name = "rule_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Rule type is required")
    private RuleType ruleType;

    @Column(name = "triggered_at", nullable = false)
    @NotNull(message = "Triggered timestamp is required")
    private LocalDateTime triggeredAt;

    @Column(name = "risk_score")
    @Min(value = 0, message = "Risk score must be at least 0")
    @Max(value = 100, message = "Risk score must not exceed 100")
    private Integer riskScore;

    // Lifecycle callbacks

    @PrePersist
    protected void onCreate() {
        if (triggeredAt == null) {
            triggeredAt = LocalDateTime.now();
        }
    }

    // equals, hashCode, toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TriggeredTransaction that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "TriggeredTransaction{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", ruleType=" + ruleType +
                ", triggeredAt=" + triggeredAt +
                ", riskScore=" + riskScore +
                '}';
    }
}
