package com.fraud.ruleengine.service;

import com.fraud.ruleengine.domain.entity.BlockedCustomer;
import com.fraud.ruleengine.domain.entity.BlockedMerchant;
import com.fraud.ruleengine.dto.BlockCustomerRequest;
import com.fraud.ruleengine.dto.BlockMerchantRequest;
import com.fraud.ruleengine.exception.RuleNotFoundException;
import com.fraud.ruleengine.repository.BlockedCustomerRepository;
import com.fraud.ruleengine.repository.BlockedMerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing customer and merchant blocklists.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlocklistService {

    private final BlockedCustomerRepository blockedCustomerRepository;
    private final BlockedMerchantRepository blockedMerchantRepository;

    // Customer blocklist operations

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<BlockedCustomer>> getAllBlockedCustomers() {
        return CompletableFuture.completedFuture(blockedCustomerRepository.findAll());
    }

    @Async
    @Transactional
    public CompletableFuture<BlockedCustomer> blockCustomer(BlockCustomerRequest request) {
        BlockedCustomer blocked = BlockedCustomer.builder()
            .customerId(request.customerId())
            .reason(request.reason())
            .blockedBy(request.blockedBy())
            .expiresAt(request.expiresAt())
            .blockedAt(LocalDateTime.now())
            .build();

        BlockedCustomer saved = blockedCustomerRepository.save(blocked);
        log.info("Blocked customer: {} by {} - Reason: {}",
            request.customerId(), request.blockedBy(), request.reason());

        return CompletableFuture.completedFuture(saved);
    }

    @Async
    @Transactional
    public CompletableFuture<Void> unblockCustomer(String customerId) {
        if (!blockedCustomerRepository.existsById(customerId)) {
            throw new RuleNotFoundException("Blocked customer not found with id: " + customerId);
        }

        blockedCustomerRepository.deleteById(customerId);
        log.info("Unblocked customer: {}", customerId);

        return CompletableFuture.completedFuture(null);
    }

    // Merchant blocklist operations

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<BlockedMerchant>> getAllBlockedMerchants() {
        return CompletableFuture.completedFuture(blockedMerchantRepository.findAll());
    }

    @Async
    @Transactional
    public CompletableFuture<BlockedMerchant> blockMerchant(BlockMerchantRequest request) {
        BlockedMerchant blocked = BlockedMerchant.builder()
            .merchantName(request.merchantName())
            .reason(request.reason())
            .blockedBy(request.blockedBy())
            .expiresAt(request.expiresAt())
            .blockedAt(LocalDateTime.now())
            .build();

        BlockedMerchant saved = blockedMerchantRepository.save(blocked);
        log.info("Blocked merchant: {} by {} - Reason: {}",
            request.merchantName(), request.blockedBy(), request.reason());

        return CompletableFuture.completedFuture(saved);
    }

    @Async
    @Transactional
    public CompletableFuture<Void> unblockMerchant(String merchantName) {
        if (!blockedMerchantRepository.existsById(merchantName)) {
            throw new RuleNotFoundException("Blocked merchant not found with name: " + merchantName);
        }

        blockedMerchantRepository.deleteById(merchantName);
        log.info("Unblocked merchant: {}", merchantName);

        return CompletableFuture.completedFuture(null);
    }
}
