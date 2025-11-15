package com.lotus.lotusSPM.service;

import com.lotus.lotusSPM.dao.OpportunitiesDao;
import com.lotus.lotusSPM.model.Opportunities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OpportunitiesServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OpportunitiesService Tests")
class OpportunitiesServiceImplTest {

    @Mock
    private OpportunitiesDao opportunitiesDao;

    @InjectMocks
    private OpportunitiesServiceImpl opportunitiesService;

    private Opportunities testOpportunity;

    @BeforeEach
    void setUp() {
        testOpportunity = new Opportunities();
        testOpportunity.setId(1L);
        testOpportunity.setCompanyName("Tech Corp");
        testOpportunity.setTitle("Software Engineer Intern");
        testOpportunity.setLocation("Istanbul, Turkey");
        testOpportunity.setDeadline(LocalDate.now().plusDays(30));
        testOpportunity.setUrl("https://techcorp.com/careers/intern");
    }

    @Test
    @DisplayName("Should create opportunity successfully")
    void testCreateOpportunity() {
        // Given
        when(opportunitiesDao.save(any(Opportunities.class))).thenReturn(testOpportunity);

        // When
        Opportunities result = opportunitiesService.createOpportunity(testOpportunity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(result.getTitle()).isEqualTo("Software Engineer Intern");
        verify(opportunitiesDao, times(1)).save(testOpportunity);
    }

    @Test
    @DisplayName("Should get all opportunities")
    void testGetOpportunities() {
        // Given
        Opportunities opp2 = new Opportunities();
        opp2.setCompanyName("Startup Inc");
        List<Opportunities> opportunitiesList = Arrays.asList(testOpportunity, opp2);

        when(opportunitiesDao.findAll()).thenReturn(opportunitiesList);

        // When
        List<Opportunities> results = opportunitiesService.getOpportunities();

        // Then
        assertThat(results).hasSize(2);
        verify(opportunitiesDao, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find opportunity by ID")
    void testFindById() {
        // Given
        when(opportunitiesDao.findById(1L)).thenReturn(Optional.of(testOpportunity));

        // When
        Opportunities result = opportunitiesService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(opportunitiesDao, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when opportunity not found")
    void testFindByIdNotFound() {
        // Given
        when(opportunitiesDao.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> opportunitiesService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Opportunity not found");
        verify(opportunitiesDao, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should update opportunity")
    void testUpdateOpportunity() {
        // Given
        testOpportunity.setTitle("Senior Software Engineer");
        when(opportunitiesDao.save(any(Opportunities.class))).thenReturn(testOpportunity);

        // When
        Opportunities result = opportunitiesService.updateOpportunity(testOpportunity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Senior Software Engineer");
        verify(opportunitiesDao, times(1)).save(testOpportunity);
    }

    @Test
    @DisplayName("Should delete opportunity by ID")
    void testDeleteOpportunity() {
        // Given
        doNothing().when(opportunitiesDao).deleteById(1L);

        // When
        opportunitiesService.deleteOpportunity(1L);

        // Then
        verify(opportunitiesDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should handle expired opportunities")
    void testExpiredOpportunity() {
        // Given
        Opportunities expiredOpp = new Opportunities();
        expiredOpp.setDeadline(LocalDate.now().minusDays(1));

        // When
        boolean isExpired = expiredOpp.getDeadline().isBefore(LocalDate.now());

        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("Should handle valid URL")
    void testValidUrl() {
        // Given/When
        String url = testOpportunity.getUrl();

        // Then
        assertThat(url).startsWith("https://");
        assertThat(url).contains("techcorp.com");
    }
}
