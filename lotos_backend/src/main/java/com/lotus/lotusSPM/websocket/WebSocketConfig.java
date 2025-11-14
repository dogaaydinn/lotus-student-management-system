package com.lotus.lotusSPM.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket Configuration for Real-Time Communication.
 *
 * Enterprise Pattern: Real-Time Bidirectional Communication
 *
 * Use Cases:
 * - Real-time notifications
 * - Live updates (opportunity posts, message arrivals)
 * - Collaborative features
 * - Live dashboards
 * - Chat functionality
 * - Progress tracking
 *
 * Protocol: STOMP over WebSocket
 * - Simple Text Oriented Messaging Protocol
 * - Pub/Sub model
 * - Frame-based protocol
 * - Built on WebSocket
 *
 * Message Flow:
 * 1. Client connects via WebSocket
 * 2. Client subscribes to topics (/topic/notifications)
 * 3. Server publishes to topics
 * 4. Subscribed clients receive updates
 *
 * Endpoints:
 * - /ws: WebSocket connection endpoint
 * - /app/*: Application destination prefix
 * - /topic/*: Broadcast to all subscribers
 * - /queue/*: Point-to-point messaging
 * - /user/*: User-specific messaging
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Register STOMP endpoints for WebSocket connections.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*") // Configure based on CORS policy
            .withSockJS(); // Fallback for browsers without WebSocket support
    }

    /**
     * Configure message broker for routing messages.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple broker for in-memory message handling
        // Production: Use external broker (RabbitMQ, ActiveMQ)
        registry.enableSimpleBroker(
            "/topic",  // Broadcast topics
            "/queue",  // Point-to-point queues
            "/user"    // User-specific destinations
        );

        // Uncomment for external RabbitMQ broker
        // registry.enableStompBrokerRelay("/topic", "/queue", "/user")
        //     .setRelayHost("localhost")
        //     .setRelayPort(61613)
        //     .setClientLogin("guest")
        //     .setClientPasscode("guest");

        // Set application destination prefix
        registry.setApplicationDestinationPrefixes("/app");

        // User destination prefix
        registry.setUserDestinationPrefix("/user");
    }
}
