package com.fraud.ruleengine.service;

import com.fraud.ruleengine.domain.entity.TriggeredTransaction;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.exception.TransactionNotFoundException;
import com.fraud.ruleengine.repository.TriggeredTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TriggeredTransactionService {

    private final TriggeredTransactionRepository repository;

    @Transactional(readOnly = true)
    public Page<TriggeredTransaction> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public TriggeredTransaction findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<TriggeredTransaction> findByFilters(
        RuleType ruleType,
        String customerId,
        String accountId,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        return repository.findByFilters(ruleType, customerId, accountId, minAmount, maxAmount, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TriggeredTransaction> search(String searchTerm, Pageable pageable) {
        return repository.search(searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public Page<TriggeredTransaction> findRecent(int hours, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return repository.findByTriggeredAtAfter(since, pageable);
    }
}
