package com.fraud.ruleengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for blocking a merchant.
 */
public record BlockMerchantRequest(
    @NotBlank(message = "Merchant name is required")
    @Size(max = 255, message = "Merchant name must not exceed 255 characters")
    String merchantName,

    @NotBlank(message = "Reason is required")
    String reason,

    String blockedBy,

    LocalDateTime expiresAt
) {
}
