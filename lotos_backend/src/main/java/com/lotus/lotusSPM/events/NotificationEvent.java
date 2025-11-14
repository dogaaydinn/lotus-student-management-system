package com.lotus.lotusSPM.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Notification Event for user notifications.
 *
 * Enterprise Pattern: Event-Driven Notifications
 *
 * Triggers various notification channels:
 * - In-app notifications
 * - Email notifications
 * - SMS notifications
 * - Push notifications
 * - WebSocket real-time updates
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {

    private String eventId = UUID.randomUUID().toString();
    private String userId;
    private String notificationType; // EMAIL, SMS, PUSH, IN_APP
    private String title;
    private String message;
    private String link;
    private Instant occurredOn = Instant.now();
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    public NotificationEvent(String userId, String notificationType, String title, String message) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
    }
}
