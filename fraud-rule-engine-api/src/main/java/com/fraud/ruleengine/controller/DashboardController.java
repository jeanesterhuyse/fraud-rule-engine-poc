package com.fraud.ruleengine.controller;

import com.fraud.ruleengine.controller.dto.DashboardSummary;
import com.fraud.ruleengine.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getSummary() {
        return ResponseEntity.ok(service.getSummary());
    }

    @GetMapping("/top-triggered-rules")
    public ResponseEntity<List<Object[]>> getTopTriggeredRules(
        @RequestParam(defaultValue = "7") int days,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(service.getTopTriggeredRules(days, limit));
    }

    @GetMapping("/customer-risk")
    public ResponseEntity<List<Object[]>> getTopRiskCustomers(
        @RequestParam(defaultValue = "7") int days,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(service.getTopRiskCustomers(days, limit));
    }

    @GetMapping("/rule-type-distribution")
    public ResponseEntity<List<Object[]>> getRuleTypeDistribution() {
        return ResponseEntity.ok(service.getRuleTypeDistribution());
    }

    @GetMapping("/trends")
    public ResponseEntity<List<Object[]>> getTrends(
        @RequestParam(defaultValue = "24") int hours
    ) {
        return ResponseEntity.ok(service.getTrends(hours));
    }
}
