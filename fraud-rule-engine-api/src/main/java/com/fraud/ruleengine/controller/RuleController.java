package com.fraud.ruleengine.controller;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.service.RuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    public ResponseEntity<Page<Rule>> getRules(
        @RequestParam(required = false) RuleType ruleType,
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(required = false) Integer minPriority,
        @PageableDefault(size = 20, sort = "priority", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Rule> rules = ruleService.findByFilters(ruleType, enabled, minPriority, pageable);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rule> getRuleById(@PathVariable Long id) {
        Rule rule = ruleService.findById(id);
        return ResponseEntity.ok(rule);
    }

    @PostMapping
    public ResponseEntity<Rule> createRule(@Valid @RequestBody Rule rule) {
        Rule createdRule = ruleService.create(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rule> updateRule(
        @PathVariable Long id,
        @Valid @RequestBody Rule rule
    ) {
        Rule updatedRule = ruleService.update(id, rule);
        return ResponseEntity.ok(updatedRule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Rule> enableRule(@PathVariable Long id) {
        Rule rule = ruleService.enable(id);
        return ResponseEntity.ok(rule);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Rule> disableRule(@PathVariable Long id) {
        Rule rule = ruleService.disable(id);
        return ResponseEntity.ok(rule);
    }
}
