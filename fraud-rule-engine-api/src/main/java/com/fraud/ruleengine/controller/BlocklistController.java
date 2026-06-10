package com.fraud.ruleengine.controller;

import com.fraud.ruleengine.domain.entity.BlockedCustomer;
import com.fraud.ruleengine.domain.entity.BlockedMerchant;
import com.fraud.ruleengine.dto.BlockCustomerRequest;
import com.fraud.ruleengine.dto.BlockMerchantRequest;
import com.fraud.ruleengine.service.BlocklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * REST API for managing customer and merchant blocklists.
 */
@RestController
@RequestMapping("/api/v1/blocklists")
@RequiredArgsConstructor
@Slf4j
public class BlocklistController {

    private final BlocklistService blocklistService;

    // Customer blocklist endpoints

    @GetMapping("/customers")
    public CompletableFuture<ResponseEntity<List<BlockedCustomer>>> getBlockedCustomers() {
        return blocklistService.getAllBlockedCustomers()
            .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/customers")
    public CompletableFuture<ResponseEntity<BlockedCustomer>> blockCustomer(@Valid @RequestBody BlockCustomerRequest request) {
        return blocklistService.blockCustomer(request)
            .thenApply(blocked -> ResponseEntity.status(HttpStatus.CREATED).body(blocked));
    }

    @DeleteMapping("/customers/{customerId}")
    public CompletableFuture<ResponseEntity<Void>> unblockCustomer(@PathVariable String customerId) {
        return blocklistService.unblockCustomer(customerId)
            .thenApply(v -> ResponseEntity.noContent().<Void>build());
    }

    // Merchant blocklist endpoints

    @GetMapping("/merchants")
    public CompletableFuture<ResponseEntity<List<BlockedMerchant>>> getBlockedMerchants() {
        return blocklistService.getAllBlockedMerchants()
            .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/merchants")
    public CompletableFuture<ResponseEntity<BlockedMerchant>> blockMerchant(@Valid @RequestBody BlockMerchantRequest request) {
        return blocklistService.blockMerchant(request)
            .thenApply(blocked -> ResponseEntity.status(HttpStatus.CREATED).body(blocked));
    }

    @DeleteMapping("/merchants/{merchantName}")
    public CompletableFuture<ResponseEntity<Void>> unblockMerchant(@PathVariable String merchantName) {
        return blocklistService.unblockMerchant(merchantName)
            .thenApply(v -> ResponseEntity.noContent().<Void>build());
    }
}
