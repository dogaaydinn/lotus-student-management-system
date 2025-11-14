package com.lotus.lotusSPM.metrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * Aspect for automatic metrics collection.
 *
 * Enterprise Pattern: Cross-Cutting Concerns via AOP
 *
 * Automatically collects:
 * - API endpoint latency
 * - Request counts
 * - Error rates
 * - Method execution times
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsAspect {

    private final CustomMetrics customMetrics;

    /**
     * Measure execution time of all controller methods.
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object measureControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Extract endpoint information
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String endpoint = extractEndpoint(signature);
            String method = extractHttpMethod(signature);

            customMetrics.recordEndpointDuration(endpoint, method, duration);

            return result;

        } catch (Throwable throwable) {
            customMetrics.incrementApiErrors(throwable.getClass().getSimpleName());
            throw throwable;
        }
    }

    /**
     * Measure execution time of repository methods.
     */
    @Around("execution(* com.lotus.lotusSPM.dao..*(..))")
    public Object measureRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            String queryType = joinPoint.getSignature().getName();
            customMetrics.recordDatabaseQueryDuration(queryType, duration);

            return result;

        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    private String extractEndpoint(MethodSignature signature) {
        if (signature.getMethod().isAnnotationPresent(GetMapping.class)) {
            return signature.getMethod().getAnnotation(GetMapping.class).value()[0];
        } else if (signature.getMethod().isAnnotationPresent(PostMapping.class)) {
            return signature.getMethod().getAnnotation(PostMapping.class).value()[0];
        } else if (signature.getMethod().isAnnotationPresent(PutMapping.class)) {
            return signature.getMethod().getAnnotation(PutMapping.class).value()[0];
        } else if (signature.getMethod().isAnnotationPresent(DeleteMapping.class)) {
            return signature.getMethod().getAnnotation(DeleteMapping.class).value()[0];
        } else if (signature.getMethod().isAnnotationPresent(RequestMapping.class)) {
            return signature.getMethod().getAnnotation(RequestMapping.class).value()[0];
        }
        return signature.getName();
    }

    private String extractHttpMethod(MethodSignature signature) {
        if (signature.getMethod().isAnnotationPresent(GetMapping.class)) {
            return "GET";
        } else if (signature.getMethod().isAnnotationPresent(PostMapping.class)) {
            return "POST";
        } else if (signature.getMethod().isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        } else if (signature.getMethod().isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        }
        return "UNKNOWN";
    }
}
