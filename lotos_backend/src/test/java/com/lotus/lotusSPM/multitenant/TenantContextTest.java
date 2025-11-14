package com.lotus.lotusSPM.multitenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        // Clean up tenant context after each test
        TenantContext.clear();
    }

    @Test
    void testSetAndGetCurrentTenant() {
        // Given
        String tenantId = "tenant123";

        // When
        TenantContext.setCurrentTenant(tenantId);
        String result = TenantContext.getCurrentTenant();

        // Then
        assertEquals(tenantId, result);
    }

    @Test
    void testGetCurrentTenant_WhenNotSet() {
        // When
        String result = TenantContext.getCurrentTenant();

        // Then
        assertNull(result, "Tenant should be null when not set");
    }

    @Test
    void testClearTenant() {
        // Given
        TenantContext.setCurrentTenant("tenant123");

        // When
        TenantContext.clear();
        String result = TenantContext.getCurrentTenant();

        // Then
        assertNull(result, "Tenant should be null after clear");
    }

    @Test
    void testMultipleTenantChanges() {
        // Given & When
        TenantContext.setCurrentTenant("tenant1");
        assertEquals("tenant1", TenantContext.getCurrentTenant());

        TenantContext.setCurrentTenant("tenant2");
        assertEquals("tenant2", TenantContext.getCurrentTenant());

        TenantContext.setCurrentTenant("tenant3");
        assertEquals("tenant3", TenantContext.getCurrentTenant());

        // Then
        assertEquals("tenant3", TenantContext.getCurrentTenant(),
            "Should have the last set tenant");
    }

    @Test
    void testThreadIsolation() throws InterruptedException {
        // Given
        String mainThreadTenant = "main-tenant";
        String[] otherThreadTenant = new String[1];

        TenantContext.setCurrentTenant(mainThreadTenant);

        // When
        Thread otherThread = new Thread(() -> {
            TenantContext.setCurrentTenant("other-tenant");
            otherThreadTenant[0] = TenantContext.getCurrentTenant();
        });

        otherThread.start();
        otherThread.join();

        // Then
        assertEquals(mainThreadTenant, TenantContext.getCurrentTenant(),
            "Main thread tenant should not be affected by other thread");
        assertEquals("other-tenant", otherThreadTenant[0],
            "Other thread should have its own tenant");
    }

    @Test
    void testSetNullTenant() {
        // Given
        TenantContext.setCurrentTenant("tenant123");

        // When
        TenantContext.setCurrentTenant(null);
        String result = TenantContext.getCurrentTenant();

        // Then
        assertNull(result, "Should be able to set null tenant");
    }

    @Test
    void testSetEmptyStringTenant() {
        // When
        TenantContext.setCurrentTenant("");
        String result = TenantContext.getCurrentTenant();

        // Then
        assertEquals("", result, "Should be able to set empty string tenant");
    }

    @Test
    void testTenantContextLifecycle() {
        // Simulate request lifecycle
        assertNull(TenantContext.getCurrentTenant(), "Should start with no tenant");

        // Request arrives
        TenantContext.setCurrentTenant("tenant-abc");
        assertEquals("tenant-abc", TenantContext.getCurrentTenant());

        // Request processing
        String tenantDuringProcessing = TenantContext.getCurrentTenant();
        assertEquals("tenant-abc", tenantDuringProcessing);

        // Request ends
        TenantContext.clear();
        assertNull(TenantContext.getCurrentTenant(), "Should clear after request");
    }
}
