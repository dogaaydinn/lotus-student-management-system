package com.lotus.lotusSPM.tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Distributed Tracing Configuration using OpenTelemetry.
 *
 * Enterprise Pattern: Distributed Tracing / Observability
 *
 * Enables:
 * - End-to-end request tracking across microservices
 * - Performance bottleneck identification
 * - Dependency mapping
 * - Error tracking and debugging
 * - SLA monitoring
 *
 * Trace Context Propagation:
 * - W3C Trace Context standard
 * - Automatic context propagation across service boundaries
 * - Correlation of logs, metrics, and traces
 *
 * Export to:
 * - Jaeger (trace visualization)
 * - Zipkin (distributed tracing)
 * - Cloud providers (AWS X-Ray, GCP Trace, Azure Monitor)
 *
 * Trace Anatomy:
 * - Trace: Complete journey of a request
 * - Span: Unit of work (e.g., HTTP request, database query)
 * - Attributes: Metadata (user ID, HTTP method, status code)
 * - Events: Point-in-time occurrences
 * - Links: Causal relationships between traces
 */
@Configuration
public class TracingConfig {

    @Value("${spring.application.name:lotus-student-management-system}")
    private String serviceName;

    @Value("${tracing.jaeger.endpoint:http://localhost:14250}")
    private String jaegerEndpoint;

    /**
     * Configure OpenTelemetry SDK with Jaeger exporter.
     */
    @Bean
    public OpenTelemetry openTelemetry() {
        // Resource identifies this service
        Resource resource = Resource.getDefault()
            .merge(Resource.create(Attributes.of(
                ResourceAttributes.SERVICE_NAME, serviceName,
                ResourceAttributes.SERVICE_VERSION, "2.0.0",
                ResourceAttributes.DEPLOYMENT_ENVIRONMENT, "production"
            )));

        // Jaeger exporter for sending traces
        JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
            .setEndpoint(jaegerEndpoint)
            .setTimeout(2, TimeUnit.SECONDS)
            .build();

        // Batch processor for efficient export
        BatchSpanProcessor batchSpanProcessor = BatchSpanProcessor.builder(jaegerExporter)
            .setMaxQueueSize(2048)
            .setMaxExportBatchSize(512)
            .setScheduleDelay(5, TimeUnit.SECONDS)
            .build();

        // Tracer provider
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(batchSpanProcessor)
            .setResource(resource)
            .build();

        // OpenTelemetry instance
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();

        // Shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::close));

        return openTelemetry;
    }

    /**
     * Tracer bean for manual instrumentation.
     */
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, "2.0.0");
    }

    /**
     * Custom span processor for enriching spans with business context.
     */
    @Bean
    public CustomSpanProcessor customSpanProcessor() {
        return new CustomSpanProcessor();
    }
}
