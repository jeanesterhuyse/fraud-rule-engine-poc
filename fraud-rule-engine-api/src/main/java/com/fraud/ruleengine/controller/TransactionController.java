package com.fraud.ruleengine.controller;

import com.fraud.ruleengine.domain.entity.TriggeredTransaction;
import com.fraud.ruleengine.service.TriggeredTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TriggeredTransactionService service;

    @GetMapping("/all")
    public ResponseEntity<Page<TriggeredTransaction>> getAll(
        @PageableDefault(size = 20, sort = "triggeredAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TriggeredTransaction> transactions = service.findAll(pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/recent")
    public ResponseEntity<Page<TriggeredTransaction>> getRecent(
        @RequestParam(defaultValue = "24") int hours,
        @PageableDefault(size = 50, sort = "triggeredAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(service.findRecent(hours, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TriggeredTransaction>> search(
        @RequestParam String query,
        @PageableDefault(size = 20, sort = "triggeredAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(service.search(query, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TriggeredTransaction> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
