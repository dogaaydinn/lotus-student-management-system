package com.lotus.lotusSPM.config;

import com.thoughtworks.xstream.XStream;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SimpleQueryBus;
import org.axonframework.queryhandling.DefaultQueryGateway;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CQRS Configuration - Sets up Axon Framework for CQRS and Event Sourcing.
 *
 * Enterprise Patterns Configured:
 * - Command Bus: Routes commands to handlers
 * - Query Bus: Routes queries to handlers
 * - Event Bus: Publishes events to subscribers
 * - Event Store: Persists events for event sourcing
 * - Command Gateway: Simplifies command dispatch
 * - Query Gateway: Simplifies query dispatch
 *
 * Production Consideration: Replace InMemoryEventStorageEngine with
 * JPA, MongoDB, or AxonServer for persistence.
 */
@Configuration
public class CqrsConfig {

    /**
     * Command Bus for routing commands to command handlers.
     */
    @Bean
    public CommandBus commandBus() {
        return SimpleCommandBus.builder().build();
    }

    /**
     * Query Bus for routing queries to query handlers.
     */
    @Bean
    public QueryBus queryBus() {
        return SimpleQueryBus.builder().build();
    }

    /**
     * Command Gateway for simplified command dispatch.
     */
    @Bean
    public CommandGateway commandGateway(CommandBus commandBus) {
        return DefaultCommandGateway.builder()
                .commandBus(commandBus)
                .build();
    }

    /**
     * Query Gateway for simplified query dispatch.
     */
    @Bean
    public QueryGateway queryGateway(QueryBus queryBus) {
        return DefaultQueryGateway.builder()
                .queryBus(queryBus)
                .build();
    }

    /**
     * Event Store for persisting domain events.
     *
     * NOTE: In-memory storage for development.
     * Production: Use JPA/MongoDB Event Store or AxonServer.
     */
    @Bean
    public EventStore eventStore() {
        return EmbeddedEventStore.builder()
                .storageEngine(new InMemoryEventStorageEngine())
                .build();
    }

    /**
     * Configure event processing for async handling.
     */
    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        // Configure tracking event processors for async processing
        configurer.registerTrackingEventProcessor("student-projection");
    }

    /**
     * XStream serializer configuration with security settings.
     */
    @Bean
    public Serializer serializer() {
        XStream xStream = new XStream();
        xStream.allowTypesByWildcard(new String[] {
            "com.lotus.lotusSPM.cqrs.**"
        });
        return XStreamSerializer.builder()
                .xStream(xStream)
                .build();
    }
}
