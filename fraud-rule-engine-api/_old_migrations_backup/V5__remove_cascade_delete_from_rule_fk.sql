-- Migration: Remove CASCADE DELETE from rule foreign key
-- Purpose: Prevent triggered transactions from being deleted when a rule is deleted
-- Date: 2026-06-09

-- Drop the existing foreign key constraint that has ON DELETE CASCADE
ALTER TABLE triggered_transactions
    DROP CONSTRAINT IF EXISTS fk_triggered_rule;

-- Recreate the foreign key constraint without CASCADE
-- (When a rule is deleted, rule_id will simply become NULL)
ALTER TABLE triggered_transactions
    ADD CONSTRAINT fk_triggered_rule
    FOREIGN KEY (rule_id)
    REFERENCES rules(id)
    ON DELETE SET NULL;  -- Set to NULL instead of cascade delete

-- Add comment
COMMENT ON CONSTRAINT fk_triggered_rule ON triggered_transactions IS 'Foreign key to rules table. ON DELETE SET NULL preserves transactions for audit trail when rules are deleted.';
