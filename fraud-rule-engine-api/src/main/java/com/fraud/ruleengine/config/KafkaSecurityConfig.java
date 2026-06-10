package com.fraud.ruleengine.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka security configuration for AWS MSK IAM authentication.
 *
 * This configuration is automatically applied when using port 9098 (AWS MSK with IAM).
 * For local Docker Kafka on port 9092, this configuration is skipped.
 *
 * Authentication:
 * - Uses aws-msk-iam-auth library for IAM-based authentication
 * - Reads AWS credentials from ~/.aws/credentials or environment variables
 * - No username/password needed - all handled via IAM
 *
 * Required AWS permissions:
 * - kafka:DescribeCluster
 * - kafka:GetBootstrapBrokers
 * - kafka-cluster:Connect
 * - kafka-cluster:DescribeTopic
 * - kafka-cluster:ReadData
 * - kafka-cluster:WriteData
 */
@Configuration
@ConditionalOnProperty(
    value = "app.kafka.msk-iam-auth.enabled",
    havingValue = "true",
    matchIfMissing = true  // Enabled by default for 'local' profile
)
public class KafkaSecurityConfig {

    /**
     * Adds AWS MSK IAM authentication properties to Kafka configuration.
     */
    private Map<String, Object> addIAMProperties(Map<String, Object> props) {
        Map<String, Object> config = new HashMap<>(props);

        // AWS MSK IAM Authentication
        config.put("security.protocol", "SASL_SSL");
        config.put("sasl.mechanism", "AWS_MSK_IAM");
        config.put("sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;");
        config.put("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler");

        return config;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);
        props = addIAMProperties(props);

        // ErrorHandlingDeserializer is already configured in application.yml
        // This ensures deserialization errors are caught and sent to error handler
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties(null);
        props = addIAMProperties(props);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
