package com.lotus.lotusSPM.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Event Publisher for Event-Driven Architecture.
 *
 * Enterprise Pattern: Event-Driven Architecture / Message-Driven Microservices
 *
 * Supports multiple message brokers:
 * - RabbitMQ: For enterprise message queuing
 * - Kafka: For high-throughput event streaming
 *
 * Benefits:
 * - Loose coupling between services
 * - Asynchronous processing
 * - Event sourcing and CQRS
 * - Scalability and resilience
 * - Event replay capability
 * - Temporal decoupling
 *
 * Event Types:
 * - Domain Events: Business state changes
 * - Integration Events: Cross-service communication
 * - Command Events: Actions to be executed
 * - Notification Events: User notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish event to RabbitMQ exchange.
     *
     * @param exchange RabbitMQ exchange name
     * @param routingKey Routing key for topic/direct exchanges
     * @param event Event object to publish
     */
    public void publishToRabbitMQ(String exchange, String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published event to RabbitMQ - Exchange: {}, RoutingKey: {}, Event: {}",
                exchange, routingKey, event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to publish event to RabbitMQ", e);
            // Implement retry logic or dead letter queue
        }
    }

    /**
     * Publish event to Kafka topic.
     *
     * @param topic Kafka topic name
     * @param key Message key for partitioning
     * @param event Event object to publish
     */
    public void publishToKafka(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event)
                .addCallback(
                    success -> log.info("Published event to Kafka - Topic: {}, Key: {}, Event: {}",
                        topic, key, event.getClass().getSimpleName()),
                    failure -> log.error("Failed to publish event to Kafka", failure)
                );
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka", e);
        }
    }

    /**
     * Publish domain event (student created, updated, etc.)
     */
    public void publishDomainEvent(DomainEvent event) {
        // Publish to both RabbitMQ and Kafka for redundancy
        publishToRabbitMQ("domain-events", event.getEventType(), event);
        publishToKafka("domain-events", event.getAggregateId(), event);
    }

    /**
     * Publish integration event (cross-service communication)
     */
    public void publishIntegrationEvent(IntegrationEvent event) {
        publishToRabbitMQ("integration-events", event.getEventType(), event);
        publishToKafka("integration-events", event.getServiceName(), event);
    }

    /**
     * Publish notification event (user notifications)
     */
    public void publishNotificationEvent(NotificationEvent event) {
        publishToRabbitMQ("notifications", event.getUserId(), event);
    }
}
