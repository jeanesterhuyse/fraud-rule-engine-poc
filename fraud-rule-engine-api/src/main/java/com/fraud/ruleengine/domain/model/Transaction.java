package com.fraud.ruleengine.domain.model;

import com.fraud.ruleengine.domain.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record representing a transaction consumed from Kafka.
 *
 * This is a DTO (Data Transfer Object) used for Kafka deserialization and
 * rule evaluation. It is NOT a JPA entity - transactions are not persisted
 * unless they trigger a rule.
 *
 * Design: Java record for immutability and concise syntax.
 */
public record Transaction(

    @NotBlank(message = "Transaction ID is required")
    String transactionId,

    @NotBlank(message = "Account ID is required")
    String accountId,

    @NotBlank(message = "Customer ID is required")
    String customerId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    BigDecimal amount,

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be 3 uppercase letters (ISO 4217)")
    String currency,

    String merchantName,

    String merchantCategory,

    @NotNull(message = "Transaction type is required")
    TransactionType transactionType,

    @NotNull(message = "Transaction timestamp is required")
    @PastOrPresent(message = "Transaction timestamp cannot be in the future")
    LocalDateTime transactionTimestamp,

    @Pattern(regexp = "[A-Z]{3}", message = "Country code must be 3 uppercase letters")
    String countryCode,

    String deviceId,

    String ipAddress,

    @Pattern(regexp = "\\d{4}", message = "Card last four must be 4 digits")
    String cardLastFour

) {

    /**
     * Creates a Transaction with required fields only.
     * Useful for testing.
     */
    public static Transaction minimal(
        String transactionId,
        String accountId,
        String customerId,
        BigDecimal amount,
        String currency,
        TransactionType transactionType,
        LocalDateTime transactionTimestamp
    ) {
        return new Transaction(
            transactionId,
            accountId,
            customerId,
            amount,
            currency,
            null,  // merchantName
            null,  // merchantCategory
            transactionType,
            transactionTimestamp,
            null,  // countryCode
            null,  // deviceId
            null,  // ipAddress
            null   // cardLastFour
        );
    }

    /**
     * Returns a formatted string representation for logging.
     */
    public String toLogString() {
        return String.format(
            "Transaction[id=%s, customer=%s, amount=%s %s, type=%s, timestamp=%s]",
            transactionId,
            customerId,
            amount,
            currency,
            transactionType,
            transactionTimestamp
        );
    }
}
