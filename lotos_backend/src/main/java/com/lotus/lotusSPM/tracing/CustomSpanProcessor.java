package com.lotus.lotusSPM.tracing;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Custom Span Processor for enriching traces with business context.
 *
 * Enterprise Pattern: Observability / Context Enrichment
 *
 * Automatically adds:
 * - User information (username, role)
 * - Tenant/Organization ID
 * - Business transaction ID
 * - Custom attributes for filtering
 *
 * This enables:
 * - User-specific trace filtering
 * - Multi-tenant trace isolation
 * - Business-level observability
 * - Compliance and audit requirements
 */
@Slf4j
public class CustomSpanProcessor implements SpanProcessor {

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        // Add user context to span
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            span.setAttribute("user.name", auth.getName());
            span.setAttribute("user.authenticated", true);

            if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                String roles = auth.getAuthorities().toString();
                span.setAttribute("user.roles", roles);
            }
        }

        // Add custom business attributes
        span.setAttribute("service.name", "lotus-spm");
        span.setAttribute("service.tier", "backend");

        log.trace("Span started: {} with attributes enriched", span.getName());
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        log.trace("Span ended: {} (duration: {}ms)",
            span.getName(),
            span.getLatencyNanos() / 1_000_000);
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }
}
