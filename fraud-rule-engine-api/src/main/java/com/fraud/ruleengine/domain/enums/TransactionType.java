package com.fraud.ruleengine.domain.enums;

/**
 * Enumeration of transaction types supported by the fraud detection system.
 */
public enum TransactionType {

    /**
     * Purchase transaction at a merchant (card present or card not present)
     */
    PURCHASE,

    /**
     * ATM or over-the-counter cash withdrawal
     */
    WITHDRAWAL,

    /**
     * Fund transfer between accounts (internal or external)
     */
    TRANSFER,

    /**
     * Refund or credit to customer account
     */
    REFUND,

    /**
     * Bill payment or scheduled payment
     */
    PAYMENT,

    /**
     * Cash or check deposit
     */
    DEPOSIT
}
