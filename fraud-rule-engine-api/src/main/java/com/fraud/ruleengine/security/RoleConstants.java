package com.fraud.ruleengine.security;

/**
 * Constants for role-based access control.
 * Centralizes role definitions to ensure consistency across the application.
 */
public final class RoleConstants {

    private RoleConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * SpEL expression for checking if user has fraud analyst or admin role.
     * Used in @PreAuthorize annotations for write operations.
     */
    public static final String HAS_WRITE_ROLE = "hasRole('FRAUD_ANALYST') or hasRole('ADMIN')";

    /**
     * Role name for fraud analysts (full access).
     */
    public static final String FRAUD_ANALYST = "FRAUD_ANALYST";

    /**
     * Role name for fraud viewers (read-only access).
     */
    public static final String FRAUD_VIEWER = "FRAUD_VIEWER";

    /**
     * Role name for administrators (full access).
     */
    public static final String ADMIN = "ADMIN";
}
