package com.lotus.lotusSPM.cloud;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.Map;

/**
 * Cloud Notification Service using AWS SNS.
 *
 * Enterprise Pattern: Multi-Channel Notification Service
 *
 * Channels:
 * - Email
 * - SMS
 * - Push notifications (mobile)
 * - HTTP/HTTPS endpoints
 *
 * Features:
 * - Topic-based pub/sub
 * - Message filtering
 * - Delivery retries
 * - Dead letter queues
 * - Message attributes
 * - Fan-out pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SnsClient snsClient;

    @Value("${aws.sns.topic.email:}")
    private String emailTopicArn;

    @Value("${aws.sns.topic.sms:}")
    private String smsTopicArn;

    /**
     * Send email notification via SNS.
     *
     * @param email Recipient email
     * @param subject Email subject
     * @param message Email body
     */
    public void sendEmail(String email, String subject, String message) {
        try {
            PublishRequest request = PublishRequest.builder()
                .topicArn(emailTopicArn)
                .subject(subject)
                .message(message)
                .messageAttributes(Map.of(
                    "email", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(email)
                        .build()
                ))
                .build();

            PublishResponse response = snsClient.publish(request);

            log.info("Email sent via SNS. MessageId: {}", response.messageId());

        } catch (Exception e) {
            log.error("Failed to send email via SNS", e);
        }
    }

    /**
     * Send SMS notification via SNS.
     *
     * @param phoneNumber Recipient phone number (E.164 format)
     * @param message SMS message
     */
    public void sendSMS(String phoneNumber, String message) {
        try {
            PublishRequest request = PublishRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .messageAttributes(Map.of(
                    "AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("Transactional") // or "Promotional"
                        .build()
                ))
                .build();

            PublishResponse response = snsClient.publish(request);

            log.info("SMS sent via SNS. MessageId: {}", response.messageId());

        } catch (Exception e) {
            log.error("Failed to send SMS via SNS", e);
        }
    }

    /**
     * Publish message to SNS topic (pub/sub pattern).
     *
     * @param topicArn SNS topic ARN
     * @param message Message to publish
     */
    public void publishToTopic(String topicArn, String message) {
        try {
            PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .build();

            PublishResponse response = snsClient.publish(request);

            log.info("Published to SNS topic. MessageId: {}", response.messageId());

        } catch (Exception e) {
            log.error("Failed to publish to SNS topic", e);
        }
    }

    /**
     * Subscribe email to topic.
     *
     * @param topicArn SNS topic ARN
     * @param email Email to subscribe
     */
    public void subscribeEmailToTopic(String topicArn, String email) {
        try {
            SubscribeRequest request = SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("email")
                .endpoint(email)
                .build();

            SubscribeResponse response = snsClient.subscribe(request);

            log.info("Subscribed {} to topic. SubscriptionArn: {}",
                email, response.subscriptionArn());

        } catch (Exception e) {
            log.error("Failed to subscribe email to SNS topic", e);
        }
    }

    /**
     * Unsubscribe from topic.
     *
     * @param subscriptionArn Subscription ARN
     */
    public void unsubscribe(String subscriptionArn) {
        try {
            UnsubscribeRequest request = UnsubscribeRequest.builder()
                .subscriptionArn(subscriptionArn)
                .build();

            snsClient.unsubscribe(request);

            log.info("Unsubscribed from topic: {}", subscriptionArn);

        } catch (Exception e) {
            log.error("Failed to unsubscribe from SNS topic", e);
        }
    }
}
