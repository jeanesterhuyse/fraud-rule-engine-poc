package com.fraud.ruleengine.controller.dto;

import java.math.BigDecimal;

public record DashboardSummary(
    long totalTriggeredTransactions,
    long totalActiveRules,
    long totalTriggersLast24Hours,
    long totalTriggersLast7Days,
    Double averageRiskScore,
    Integer highestRiskScore,
    BigDecimal totalFlaggedAmount,
    String currency
) {}
