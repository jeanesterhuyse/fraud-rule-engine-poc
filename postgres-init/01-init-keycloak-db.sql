-- PostgreSQL Initialization Script
-- Creates the Keycloak database and schema automatically when the container first starts
-- This ensures Keycloak can start without manual database setup

-- Create keycloak database if it doesn't exist
SELECT 'CREATE DATABASE keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

-- Connect to keycloak database to create schema
\c keycloak

-- Create keycloak schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS keycloak AUTHORIZATION fraud_user;

-- Grant all privileges to fraud_user
GRANT ALL PRIVILEGES ON DATABASE keycloak TO fraud_user;
GRANT ALL PRIVILEGES ON SCHEMA keycloak TO fraud_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA keycloak TO fraud_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA keycloak TO fraud_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL PRIVILEGES ON TABLES TO fraud_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL PRIVILEGES ON SEQUENCES TO fraud_user;

-- Log completion
\echo 'Keycloak database and schema created successfully'
