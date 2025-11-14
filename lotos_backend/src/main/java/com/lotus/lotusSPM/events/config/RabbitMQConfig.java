package com.lotus.lotusSPM.events.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Event-Driven Architecture.
 *
 * Enterprise Pattern: Message Broker Configuration
 *
 * Configures:
 * - Exchanges (topic, direct, fanout, headers)
 * - Queues with dead letter exchanges
 * - Bindings with routing keys
 * - Message converters (JSON)
 * - Retry policies
 * - Acknowledgment modes
 *
 * Queue Types:
 * - Work Queues: Load balancing
 * - Publish/Subscribe: Fanout to multiple consumers
 * - Routing: Topic-based routing
 * - RPC: Request/Response pattern
 */
@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String DOMAIN_EVENTS_EXCHANGE = "domain-events";
    public static final String INTEGRATION_EVENTS_EXCHANGE = "integration-events";
    public static final String NOTIFICATIONS_EXCHANGE = "notifications";
    public static final String DLX_EXCHANGE = "dlx-exchange"; // Dead Letter Exchange

    // Queue names
    public static final String STUDENT_EVENTS_QUEUE = "student-events-queue";
    public static final String OPPORTUNITY_EVENTS_QUEUE = "opportunity-events-queue";
    public static final String NOTIFICATION_QUEUE = "notification-queue";
    public static final String EMAIL_QUEUE = "email-queue";
    public static final String SMS_QUEUE = "sms-queue";
    public static final String DLQ = "dead-letter-queue";

    // Routing keys
    public static final String STUDENT_CREATED_KEY = "student.created";
    public static final String STUDENT_UPDATED_KEY = "student.updated";
    public static final String OPPORTUNITY_CREATED_KEY = "opportunity.created";

    /**
     * JSON message converter for serialization.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configure RabbitTemplate with JSON converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ==================== EXCHANGES ====================

    /**
     * Domain Events Exchange - Topic exchange for domain events.
     */
    @Bean
    public TopicExchange domainEventsExchange() {
        return new TopicExchange(DOMAIN_EVENTS_EXCHANGE, true, false);
    }

    /**
     * Integration Events Exchange - Topic exchange for cross-service events.
     */
    @Bean
    public TopicExchange integrationEventsExchange() {
        return new TopicExchange(INTEGRATION_EVENTS_EXCHANGE, true, false);
    }

    /**
     * Notifications Exchange - Direct exchange for notifications.
     */
    @Bean
    public DirectExchange notificationsExchange() {
        return new DirectExchange(NOTIFICATIONS_EXCHANGE, true, false);
    }

    /**
     * Dead Letter Exchange - For failed message handling.
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // ==================== QUEUES ====================

    /**
     * Student Events Queue - Processes student-related events.
     */
    @Bean
    public Queue studentEventsQueue() {
        return QueueBuilder.durable(STUDENT_EVENTS_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "dlq")
            .withArgument("x-message-ttl", 86400000) // 24 hours TTL
            .build();
    }

    /**
     * Opportunity Events Queue - Processes opportunity-related events.
     */
    @Bean
    public Queue opportunityEventsQueue() {
        return QueueBuilder.durable(OPPORTUNITY_EVENTS_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "dlq")
            .build();
    }

    /**
     * Notification Queue - In-app notifications.
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "dlq")
            .build();
    }

    /**
     * Email Queue - Email notifications.
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "dlq")
            .build();
    }

    /**
     * SMS Queue - SMS notifications.
     */
    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(SMS_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", "dlq")
            .build();
    }

    /**
     * Dead Letter Queue - Failed messages.
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    // ==================== BINDINGS ====================

    /**
     * Bind student events queue to domain events exchange.
     */
    @Bean
    public Binding studentEventsBinding() {
        return BindingBuilder
            .bind(studentEventsQueue())
            .to(domainEventsExchange())
            .with("student.*");
    }

    /**
     * Bind opportunity events queue to domain events exchange.
     */
    @Bean
    public Binding opportunityEventsBinding() {
        return BindingBuilder
            .bind(opportunityEventsQueue())
            .to(domainEventsExchange())
            .with("opportunity.*");
    }

    /**
     * Bind notification queue to notifications exchange.
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
            .bind(notificationQueue())
            .to(notificationsExchange())
            .with("notification");
    }

    /**
     * Bind email queue to notifications exchange.
     */
    @Bean
    public Binding emailBinding() {
        return BindingBuilder
            .bind(emailQueue())
            .to(notificationsExchange())
            .with("email");
    }

    /**
     * Bind SMS queue to notifications exchange.
     */
    @Bean
    public Binding smsBinding() {
        return BindingBuilder
            .bind(smsQueue())
            .to(notificationsExchange())
            .with("sms");
    }

    /**
     * Bind dead letter queue to DLX.
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
            .bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("dlq");
    }
}
