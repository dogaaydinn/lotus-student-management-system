package com.lotus.lotusSPM.websocket;

import com.lotus.lotusSPM.events.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;

/**
 * WebSocket Controller for real-time messaging.
 *
 * Enterprise Pattern: Real-Time Event Broadcasting
 *
 * Message Mappings:
 * - /app/message: Send messages
 * - /topic/public: Public broadcast
 * - /user/queue/notifications: User-specific notifications
 *
 * Features:
 * - Real-time message delivery
 * - User-specific messaging
 * - Broadcast messaging
 * - Presence detection
 * - Typing indicators
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming messages and broadcast to all subscribers.
     *
     * @MessageMapping: Handles messages sent to /app/message
     * @SendTo: Broadcasts response to /topic/public
     */
    @MessageMapping("/message")
    @SendTo("/topic/public")
    public Map<String, Object> sendMessage(@Payload Map<String, Object> message,
                                            SimpMessageHeaderAccessor headerAccessor) {

        String username = headerAccessor.getUser() != null
            ? headerAccessor.getUser().getName()
            : "Anonymous";

        log.info("WebSocket message received from {}: {}", username, message);

        message.put("sender", username);
        message.put("timestamp", Instant.now().toString());

        return message;
    }

    /**
     * Send notification to specific user.
     */
    public void sendNotificationToUser(String username, NotificationEvent notification) {
        log.info("Sending notification to user {}: {}", username, notification.getMessage());

        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            notification
        );
    }

    /**
     * Broadcast notification to all connected users.
     */
    public void broadcastNotification(NotificationEvent notification) {
        log.info("Broadcasting notification: {}", notification.getMessage());

        messagingTemplate.convertAndSend(
            "/topic/notifications",
            notification
        );
    }

    /**
     * Send opportunity update to subscribed users.
     */
    public void sendOpportunityUpdate(Map<String, Object> opportunity) {
        log.info("Broadcasting opportunity update: {}", opportunity.get("title"));

        messagingTemplate.convertAndSend(
            "/topic/opportunities",
            opportunity
        );
    }

    /**
     * Send message notification to user.
     */
    public void sendMessageNotification(String recipientUsername, Map<String, Object> message) {
        log.info("Sending message notification to {}", recipientUsername);

        messagingTemplate.convertAndSendToUser(
            recipientUsername,
            "/queue/messages",
            message
        );
    }

    /**
     * Send application status update to student.
     */
    public void sendApplicationStatusUpdate(String studentUsername, Map<String, Object> application) {
        log.info("Sending application status update to {}", studentUsername);

        messagingTemplate.convertAndSendToUser(
            studentUsername,
            "/queue/applications",
            application
        );
    }

    /**
     * Broadcast system announcement.
     */
    public void broadcastSystemAnnouncement(String message, String severity) {
        log.info("Broadcasting system announcement: {}", message);

        Map<String, Object> announcement = Map.of(
            "message", message,
            "severity", severity,
            "timestamp", Instant.now().toString()
        );

        messagingTemplate.convertAndSend(
            "/topic/announcements",
            announcement
        );
    }
}
