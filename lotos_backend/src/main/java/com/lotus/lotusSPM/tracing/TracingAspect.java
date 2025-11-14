package com.lotus.lotusSPM.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Aspect for automatic tracing of service methods.
 *
 * Enterprise Pattern: Aspect-Oriented Programming (AOP) for Cross-Cutting Concerns
 *
 * Automatically traces:
 * - Service layer methods
 * - Repository operations
 * - External API calls
 * - Business logic execution
 *
 * Captures:
 * - Method execution time
 * - Input parameters
 * - Return values
 * - Exceptions
 * - Custom annotations
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TracingAspect {

    private final Tracer tracer;

    /**
     * Trace all service methods automatically.
     */
    @Around("execution(* com.lotus.lotusSPM.service..*(..))")
    public Object traceServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String spanName = className + "." + methodName;

        Span span = tracer.spanBuilder(spanName)
            .setSpanKind(io.opentelemetry.api.trace.SpanKind.INTERNAL)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Add method parameters as attributes
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] != null && isPrimitive(args[i])) {
                        span.setAttribute("method.param." + i, args[i].toString());
                    }
                }
            }

            span.setAttribute("method.class", className);
            span.setAttribute("method.name", methodName);

            log.debug("Tracing method: {}", spanName);

            Object result = joinPoint.proceed();

            span.setStatus(StatusCode.OK);
            return result;

        } catch (Throwable e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            log.error("Exception in traced method: {}", spanName, e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Trace repository operations.
     */
    @Around("execution(* com.lotus.lotusSPM.dao..*(..))")
    public Object traceRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String repoName = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String spanName = "db." + repoName + "." + methodName;

        Span span = tracer.spanBuilder(spanName)
            .setSpanKind(io.opentelemetry.api.trace.SpanKind.CLIENT)
            .setAttribute("db.system", "mysql")
            .setAttribute("db.operation", methodName)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            Object result = joinPoint.proceed();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Throwable e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Helper to check if object is primitive/string for tracing.
     */
    private boolean isPrimitive(Object obj) {
        return obj instanceof String ||
               obj instanceof Number ||
               obj instanceof Boolean ||
               obj.getClass().isPrimitive();
    }
}
