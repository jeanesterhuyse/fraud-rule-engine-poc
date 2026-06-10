-- Migration: Make rule_id nullable in triggered_transactions table
-- Purpose: Allow triggered transactions to remain in the database for audit/historical purposes
--          even after the associated rule has been deleted
-- Date: 2026-06-09

-- Step 1: Drop the NOT NULL constraint on rule_id
ALTER TABLE triggered_transactions
    ALTER COLUMN rule_id DROP NOT NULL;

-- Step 2: Update the foreign key constraint to allow NULL
-- (Note: In PostgreSQL, the FK constraint already allows NULL by default once the column is nullable)

-- Add a comment to document the change
COMMENT ON COLUMN triggered_transactions.rule_id IS 'Reference to the rule that triggered this transaction. May be NULL if the rule has been deleted (for historical record keeping).';
