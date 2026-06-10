package com.fraud.ruleengine.service;

import com.fraud.ruleengine.controller.dto.DashboardSummary;
import com.fraud.ruleengine.repository.TriggeredTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TriggeredTransactionRepository triggeredTransactionRepository;
    private final RuleService ruleService;

    @Transactional(readOnly = true)
    public DashboardSummary getSummary() {
        long total = triggeredTransactionRepository.countTotal();
        long activeRules = ruleService.countActiveRules();

        LocalDateTime now = LocalDateTime.now();
        long last24h = triggeredTransactionRepository.countByTriggeredAtBetween(now.minusHours(24), now);
        long last7d = triggeredTransactionRepository.countByTriggeredAtBetween(now.minusDays(7), now);

        Double avgRisk = triggeredTransactionRepository.calculateAverageRiskScore();
        Integer maxRisk = triggeredTransactionRepository.findMaxRiskScore();
        var totalAmount = triggeredTransactionRepository.calculateTotalFlaggedAmount();

        return new DashboardSummary(
            total,
            activeRules,
            last24h,
            last7d,
            avgRisk,
            maxRisk,
            totalAmount,
            "ZAR"
        );
    }

    @Transactional(readOnly = true)
    public List<Object[]> getTopTriggeredRules(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return triggeredTransactionRepository.findTopTriggeredRules(since, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Object[]> getTopRiskCustomers(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return triggeredTransactionRepository.findTopRiskCustomers(since, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Object[]> getRuleTypeDistribution() {
        return triggeredTransactionRepository.countByRuleTypeGrouped();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getTrends(int hours) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(hours);
        return triggeredTransactionRepository.findTrendsByHour(start, end);
    }
}
