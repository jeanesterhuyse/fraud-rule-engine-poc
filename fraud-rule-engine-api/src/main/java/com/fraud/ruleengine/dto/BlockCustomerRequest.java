package com.fraud.ruleengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for blocking a customer.
 */
public record BlockCustomerRequest(
    @NotBlank(message = "Customer ID is required")
    @Size(max = 100, message = "Customer ID must not exceed 100 characters")
    String customerId,

    @NotBlank(message = "Reason is required")
    String reason,

    String blockedBy,

    LocalDateTime expiresAt
) {
}
