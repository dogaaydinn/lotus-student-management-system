package com.lotus.lotusSPM.featureflags;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

/**
 * Feature Flags for Progressive Feature Rollout.
 *
 * Enterprise Pattern: Feature Toggle / Feature Flags
 *
 * Benefits:
 * - Deploy code without enabling features
 * - A/B testing and experimentation
 * - Gradual rollout (canary releases)
 * - Emergency kill switches
 * - Role-based feature access
 * - Testing in production
 * - Reduce branch complexity
 *
 * Types of Toggles:
 * - Release Toggles: Hide incomplete features
 * - Experiment Toggles: A/B testing
 * - Ops Toggles: Control operational aspects
 * - Permission Toggles: Premium features
 *
 * Best Practices:
 * - Remove old feature flags
 * - Document flag lifecycle
 * - Monitor flag usage
 * - Have rollback strategy
 */
public enum FeatureFlags implements Feature {

    @Label("Enable GraphQL API")
    @EnabledByDefault
    GRAPHQL_API,

    @Label("Enable Advanced Search (Elasticsearch)")
    @EnabledByDefault
    ADVANCED_SEARCH,

    @Label("Enable Real-time Notifications (WebSocket)")
    @EnabledByDefault
    REALTIME_NOTIFICATIONS,

    @Label("Enable Event Sourcing")
    EVENT_SOURCING,

    @Label("Enable CQRS Pattern")
    CQRS_ENABLED,

    @Label("Enable AI Features (Chatbot, Resume Analysis)")
    @EnabledByDefault
    AI_FEATURES,

    @Label("Enable Multi-Factor Authentication")
    @EnabledByDefault
    MFA_ENABLED,

    @Label("Enable OAuth2 Authentication")
    OAUTH2_ENABLED,

    @Label("Enable Rate Limiting")
    @EnabledByDefault
    RATE_LIMITING,

    @Label("Enable Distributed Tracing")
    @EnabledByDefault
    DISTRIBUTED_TRACING,

    @Label("Enable Advanced Analytics")
    @EnabledByDefault
    ADVANCED_ANALYTICS,

    @Label("Enable Document OCR")
    DOCUMENT_OCR,

    @Label("Enable Email Notifications")
    @EnabledByDefault
    EMAIL_NOTIFICATIONS,

    @Label("Enable SMS Notifications")
    SMS_NOTIFICATIONS,

    @Label("Enable Push Notifications")
    PUSH_NOTIFICATIONS,

    @Label("Enable Bulk Operations")
    @EnabledByDefault
    BULK_OPERATIONS,

    @Label("Enable Data Export")
    @EnabledByDefault
    DATA_EXPORT,

    @Label("Enable Audit Logging")
    @EnabledByDefault
    AUDIT_LOGGING,

    @Label("Enable Performance Monitoring")
    @EnabledByDefault
    PERFORMANCE_MONITORING,

    @Label("Enable Beta Features")
    BETA_FEATURES,

    @Label("Enable Experimental Features")
    EXPERIMENTAL_FEATURES;

    /**
     * Check if feature is enabled.
     */
    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
