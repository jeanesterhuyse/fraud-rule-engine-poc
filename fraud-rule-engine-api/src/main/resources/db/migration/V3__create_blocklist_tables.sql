-- V3: Create blocklist tables for customer and merchant blocking
-- Purpose: Support CUSTOMER_BLOCKLIST and MERCHANT_BLOCKLIST rule types

-- Blocked customers table
CREATE TABLE blocked_customers (
    customer_id VARCHAR(100) PRIMARY KEY,
    blocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    blocked_by VARCHAR(100),
    reason TEXT NOT NULL,
    expires_at TIMESTAMP  -- Optional: auto-expire blocks
);

-- Index for expiry-based queries
CREATE INDEX idx_blocked_customers_expires
ON blocked_customers(expires_at)
WHERE expires_at IS NOT NULL;

-- Blocked merchants table
CREATE TABLE blocked_merchants (
    merchant_name VARCHAR(255) PRIMARY KEY,
    blocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    blocked_by VARCHAR(100),
    reason TEXT NOT NULL,
    expires_at TIMESTAMP  -- Optional: auto-expire blocks
);

-- Index for expiry-based queries
CREATE INDEX idx_blocked_merchants_expires
ON blocked_merchants(expires_at)
WHERE expires_at IS NOT NULL;

-- Seed with test blocked customer for demo
INSERT INTO blocked_customers (customer_id, blocked_by, reason)
VALUES
    ('CUST-BLOCKED-001', 'system', 'Test blocked customer for POC demo - known fraudster'),
    ('CUST-BLOCKED-002', 'fraud_analyst', 'Suspicious activity pattern detected');

-- Seed with test blocked merchant for demo
INSERT INTO blocked_merchants (merchant_name, blocked_by, reason)
VALUES
    ('Suspicious Electronics Ltd', 'system', 'Test blocked merchant - high fraud rate'),
    ('Fake Travel Agency', 'fraud_analyst', 'Confirmed fraudulent merchant');

-- Comments
COMMENT ON TABLE blocked_customers IS 'List of customers blocked from performing transactions';
COMMENT ON TABLE blocked_merchants IS 'List of merchants where transactions should be blocked';
COMMENT ON COLUMN blocked_customers.expires_at IS 'Optional expiration timestamp for temporary blocks';
COMMENT ON COLUMN blocked_merchants.expires_at IS 'Optional expiration timestamp for temporary blocks';
