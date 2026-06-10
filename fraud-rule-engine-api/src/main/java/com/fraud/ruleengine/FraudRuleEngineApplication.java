package com.fraud.ruleengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Fraud Rule Engine API.
 *
 * This application provides a REST API for managing fraud detection rules
 * and consuming transactions from Kafka for real-time fraud detection.
 *
 * Key Features:
 * - Kafka-driven transaction ingestion
 * - Database-driven rule engine (Strategy Pattern)
 * - REST API for rule management and fraud investigation
 * - OAuth2/OIDC authentication via Keycloak
 * - Dead Letter Queue for failed messages
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableKafka
@EnableScheduling
public class FraudRuleEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudRuleEngineApplication.class, args);
    }

}
