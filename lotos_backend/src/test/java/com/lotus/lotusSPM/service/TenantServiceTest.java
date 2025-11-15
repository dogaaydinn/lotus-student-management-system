package com.lotus.lotusSPM.service;

import com.lotus.lotusSPM.dao.TenantDao;
import com.lotus.lotusSPM.multitenant.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantService Tests")
class TenantServiceTest {

    @Mock
    private TenantDao tenantDao;

    @InjectMocks
    private TenantServiceImpl tenantService;

    private Tenant sampleTenant;

    @BeforeEach
    void setUp() {
        sampleTenant = new Tenant();
        sampleTenant.setId(1L);
        sampleTenant.setName("Test University");
        sampleTenant.setSubdomain("test-university");
        sampleTenant.setStatus("ACTIVE");
        sampleTenant.setplan("PROFESSIONAL");
        sampleTenant.setMaxUsers(500);
        sampleTenant.setMaxStorageGb(50);
        sampleTenant.setContactEmail("admin@test-university.edu");
        sampleTenant.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create a new tenant successfully")
    void testCreateTenant() {
        // Given
        Tenant newTenant = new Tenant();
        newTenant.setName("New University");
        newTenant.setSubdomain("new-university");
        newTenant.setContactEmail("admin@new.edu");

        when(tenantDao.existsBySubdomain("new-university")).thenReturn(false);
        when(tenantDao.save(any(Tenant.class))).thenReturn(sampleTenant);

        // When
        Tenant created = tenantService.createTenant(newTenant);

        // Then
        assertThat(created).isNotNull();
        verify(tenantDao, times(1)).save(any(Tenant.class));
        verify(tenantDao, times(1)).existsBySubdomain("new-university");
    }

    @Test
    @DisplayName("Should throw exception when subdomain already exists")
    void testCreateTenantWithDuplicateSubdomain() {
        // Given
        Tenant newTenant = new Tenant();
        newTenant.setSubdomain("existing-subdomain");

        when(tenantDao.existsBySubdomain("existing-subdomain")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> tenantService.createTenant(newTenant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subdomain already exists");

        verify(tenantDao, never()).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Should set default values when creating tenant")
    void testCreateTenantWithDefaults() {
        // Given
        Tenant newTenant = new Tenant();
        newTenant.setSubdomain("test-subdomain");
        newTenant.setContactEmail("test@test.com");

        when(tenantDao.existsBySubdomain(anyString())).thenReturn(false);
        when(tenantDao.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant saved = invocation.getArgument(0);
            // Verify defaults were set
            assertThat(saved.getStatus()).isEqualTo("TRIAL");
            assertThat(saved.getPlan()).isEqualTo("BASIC");
            assertThat(saved.getMaxUsers()).isEqualTo(100);
            assertThat(saved.getMaxStorageGb()).isEqualTo(10);
            assertThat(saved.getTrialEndDate()).isNotNull();
            return saved;
        });

        // When
        tenantService.createTenant(newTenant);

        // Then
        verify(tenantDao, times(1)).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Should find tenant by ID")
    void testFindById() {
        // Given
        when(tenantDao.findById(1L)).thenReturn(Optional.of(sampleTenant));

        // When
        Tenant found = tenantService.findById(1L);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getName()).isEqualTo("Test University");
    }

    @Test
    @DisplayName("Should throw exception when tenant not found")
    void testFindByIdNotFound() {
        // Given
        when(tenantDao.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tenantService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    @DisplayName("Should find tenant by subdomain")
    void testFindBySubdomain() {
        // Given
        when(tenantDao.findBySubdomain("test-university")).thenReturn(Optional.of(sampleTenant));

        // When
        Tenant found = tenantService.findBySubdomain("test-university");

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getSubdomain()).isEqualTo("test-university");
    }

    @Test
    @DisplayName("Should find all tenants with pagination")
    void testFindAll() {
        // Given
        List<Tenant> tenants = Arrays.asList(sampleTenant);
        Page<Tenant> page = new PageImpl<>(tenants);
        Pageable pageable = PageRequest.of(0, 20);

        when(tenantDao.findAll(pageable)).thenReturn(page);

        // When
        Page<Tenant> result = tenantService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should update tenant")
    void testUpdateTenant() {
        // Given
        Tenant updates = new Tenant();
        updates.setName("Updated Name");
        updates.setMaxUsers(1000);

        when(tenantDao.findById(1L)).thenReturn(Optional.of(sampleTenant));
        when(tenantDao.save(any(Tenant.class))).thenReturn(sampleTenant);

        // When
        Tenant updated = tenantService.updateTenant(1L, updates);

        // Then
        assertThat(updated).isNotNull();
        verify(tenantDao, times(1)).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Should delete tenant")
    void testDeleteTenant() {
        // When
        tenantService.deleteTenant(1L);

        // Then
        verify(tenantDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should suspend tenant")
    void testSuspendTenant() {
        // Given
        when(tenantDao.findById(1L)).thenReturn(Optional.of(sampleTenant));
        when(tenantDao.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant saved = invocation.getArgument(0);
            assertThat(saved.getStatus()).isEqualTo("SUSPENDED");
            return saved;
        });

        // When
        Tenant suspended = tenantService.suspendTenant(1L);

        // Then
        assertThat(suspended).isNotNull();
        verify(tenantDao, times(1)).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Should activate tenant")
    void testActivateTenant() {
        // Given
        sampleTenant.setStatus("SUSPENDED");
        when(tenantDao.findById(1L)).thenReturn(Optional.of(sampleTenant));
        when(tenantDao.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant saved = invocation.getArgument(0);
            assertThat(saved.getStatus()).isEqualTo("ACTIVE");
            assertThat(saved.getSubscriptionStartDate()).isNotNull();
            assertThat(saved.getSubscriptionEndDate()).isNotNull();
            return saved;
        });

        // When
        Tenant activated = tenantService.activateTenant(1L);

        // Then
        assertThat(activated).isNotNull();
        verify(tenantDao, times(1)).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Should upgrade tenant plan")
    void testUpgradePlan() {
        // Given
        when(tenantDao.findById(1L)).thenReturn(Optional.of(sampleTenant));
        when(tenantDao.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant saved = invocation.getArgument(0);
            assertThat(saved.getPlan()).isEqualTo("ENTERPRISE");
            assertThat(saved.getMaxUsers()).isEqualTo(10000);
            assertThat(saved.getMaxStorageGb()).isEqualTo(500);
            return saved;
        });

        // When
        Tenant upgraded = tenantService.upgradePlan(1L, "ENTERPRISE");

        // Then
        assertThat(upgraded).isNotNull();
        verify(tenantDao, times(1)).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid plan")
    void testUpgradeToInvalidPlan() {
        // Given
        when(tenantDao.findById(1L)).thenReturn(Optional.of(sampleTenant));

        // When/Then
        assertThatThrownBy(() -> tenantService.upgradePlan(1L, "INVALID_PLAN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid plan");
    }

    @Test
    @DisplayName("Should check subdomain availability")
    void testIsSubdomainAvailable() {
        // Given
        when(tenantDao.existsBySubdomain("available")).thenReturn(false);
        when(tenantDao.existsBySubdomain("taken")).thenReturn(true);

        // When
        boolean available = tenantService.isSubdomainAvailable("available");
        boolean taken = tenantService.isSubdomainAvailable("taken");

        // Then
        assertThat(available).isTrue();
        assertThat(taken).isFalse();
    }

    @Test
    @DisplayName("Should get tenant statistics")
    void testGetTenantStatistics() {
        // Given
        when(tenantDao.count()).thenReturn(100L);
        when(tenantDao.countActiveTenants()).thenReturn(70L);
        when(tenantDao.countTrialTenants()).thenReturn(20L);
        when(tenantDao.findByPlan("BASIC")).thenReturn(Arrays.asList(sampleTenant));
        when(tenantDao.findByPlan("PROFESSIONAL")).thenReturn(Arrays.asList(sampleTenant, sampleTenant));
        when(tenantDao.findByPlan("ENTERPRISE")).thenReturn(Arrays.asList());

        // When
        Map<String, Object> stats = tenantService.getTenantStatistics();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalTenants")).isEqualTo(100L);
        assertThat(stats.get("activeTenants")).isEqualTo(70L);
        assertThat(stats.get("trialTenants")).isEqualTo(20L);
        assertThat(stats).containsKey("byPlan");
    }

    @Test
    @DisplayName("Should find expiring trials")
    void testFindExpiringTrials() {
        // Given
        List<Tenant> expiringTenants = Arrays.asList(sampleTenant);
        when(tenantDao.findExpiringTrials(any(LocalDateTime.class))).thenReturn(expiringTenants);

        // When
        List<Tenant> result = tenantService.findExpiringTrials(7);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should find expiring subscriptions")
    void testFindExpiringSubscriptions() {
        // Given
        List<Tenant> expiringTenants = Arrays.asList(sampleTenant);
        when(tenantDao.findExpiringSubscriptions(any(LocalDateTime.class))).thenReturn(expiringTenants);

        // When
        List<Tenant> result = tenantService.findExpiringSubscriptions(30);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should update last accessed timestamp")
    void testUpdateLastAccessed() {
        // Given
        when(tenantDao.findById(1L)).thenReturn(Optional.of(sampleTenant));
        when(tenantDao.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant saved = invocation.getArgument(0);
            assertThat(saved.getLastAccessedAt()).isNotNull();
            return saved;
        });

        // When
        tenantService.updateLastAccessed(1L);

        // Then
        verify(tenantDao, times(1)).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Should provision a new tenant")
    void testProvisionTenant() {
        // Given
        Tenant newTenant = new Tenant();
        newTenant.setName("Provisioned University");
        newTenant.setSubdomain("provisioned");
        newTenant.setContactEmail("admin@provisioned.edu");

        when(tenantDao.existsBySubdomain(anyString())).thenReturn(false);
        when(tenantDao.save(any(Tenant.class))).thenReturn(sampleTenant);

        // When
        Tenant provisioned = tenantService.provisionTenant(newTenant);

        // Then
        assertThat(provisioned).isNotNull();
        verify(tenantDao, times(1)).save(any(Tenant.class));
    }
}
