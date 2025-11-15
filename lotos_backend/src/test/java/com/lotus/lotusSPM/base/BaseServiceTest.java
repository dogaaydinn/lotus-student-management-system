package com.lotus.lotusSPM.base;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for service unit tests with Mockito support.
 * Provides common setup and utilities for testing service layer in isolation.
 *
 * Usage: Extend this class in your service tests
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseServiceTest {

    /**
     * Common setup for all service tests
     */
    protected void setUp() {
        // Override in subclasses if needed
    }

    /**
     * Common teardown for all service tests
     */
    protected void tearDown() {
        // Override in subclasses if needed
    }
}
