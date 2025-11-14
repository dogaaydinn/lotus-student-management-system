package com.lotus.lotusSPM.service;

import com.lotus.lotusSPM.dao.CoordinatorDao;
import com.lotus.lotusSPM.model.Coordinator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoordinatorServiceTest {

    @Mock
    private CoordinatorDao coordinatorDao;

    @InjectMocks
    private CoordinatorServiceImpl coordinatorService;

    private Coordinator testCoordinator;

    @BeforeEach
    void setUp() {
        testCoordinator = new Coordinator();
        testCoordinator.setId(1L);
        testCoordinator.setUsername("testcoordinator");
        testCoordinator.setPassword("hashedPassword");
        testCoordinator.setName("Test");
        testCoordinator.setSurname("Coordinator");
        testCoordinator.setEmail("coordinator@university.edu");
        testCoordinator.setFaculty("Engineering");
        testCoordinator.setDepartment("Computer Science");
    }

    @Test
    void testGetAllCoordinators() {
        // Given
        List<Coordinator> coordinators = Arrays.asList(testCoordinator, new Coordinator());
        when(coordinatorDao.findAll()).thenReturn(coordinators);

        // When
        List<Coordinator> result = coordinatorService.findCoordinators();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(coordinatorDao, times(1)).findAll();
    }

    @Test
    void testGetCoordinatorById() {
        // Given
        when(coordinatorDao.findById(1L)).thenReturn(Optional.of(testCoordinator));

        // When
        Coordinator result = coordinatorService.findCoordinatorById(1L);

        // Then
        assertNotNull(result);
        assertEquals("testcoordinator", result.getUsername());
        assertEquals("Engineering", result.getFaculty());
        verify(coordinatorDao, times(1)).findById(1L);
    }

    @Test
    void testGetCoordinatorById_NotFound() {
        // Given
        when(coordinatorDao.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(Exception.class, () -> {
            coordinatorService.findCoordinatorById(999L);
        });
        verify(coordinatorDao, times(1)).findById(999L);
    }

    @Test
    void testCreateCoordinator() {
        // Given
        Coordinator newCoordinator = new Coordinator();
        newCoordinator.setUsername("newcoordinator");
        newCoordinator.setPassword("hashedPassword");
        newCoordinator.setName("New");
        newCoordinator.setSurname("Coordinator");
        newCoordinator.setEmail("new@coordinator.com");

        when(coordinatorDao.save(any(Coordinator.class))).thenReturn(newCoordinator);

        // When
        Coordinator result = coordinatorService.createCoordinator(newCoordinator);

        // Then
        assertNotNull(result);
        verify(coordinatorDao, times(1)).save(any(Coordinator.class));
    }

    @Test
    void testUpdateCoordinator() {
        // Given
        Coordinator updatedCoordinator = new Coordinator();
        updatedCoordinator.setId(1L);
        updatedCoordinator.setUsername("updatedcoordinator");
        updatedCoordinator.setName("Updated");
        updatedCoordinator.setDepartment("Updated Department");

        when(coordinatorDao.save(any(Coordinator.class))).thenReturn(updatedCoordinator);

        // When
        Coordinator result = coordinatorService.save(updatedCoordinator);

        // Then
        assertNotNull(result);
        assertEquals("Updated", result.getName());
        verify(coordinatorDao, times(1)).save(any(Coordinator.class));
    }

    @Test
    void testDeleteCoordinator() {
        // Given
        when(coordinatorDao.existsById(1L)).thenReturn(true);
        doNothing().when(coordinatorDao).deleteById(1L);

        // When
        coordinatorService.deleteCoordinator(1L);

        // Then
        verify(coordinatorDao, times(1)).deleteById(1L);
    }

    @Test
    void testFindByUsername() {
        // Given
        when(coordinatorDao.findByUsername("testcoordinator")).thenReturn(testCoordinator);

        // When
        Coordinator result = coordinatorService.findByUsername("testcoordinator");

        // Then
        assertNotNull(result);
        assertEquals("testcoordinator", result.getUsername());
        verify(coordinatorDao, times(1)).findByUsername("testcoordinator");
    }

    @Test
    void testFindByUsername_NotFound() {
        // Given
        when(coordinatorDao.findByUsername("nonexistent")).thenReturn(null);

        // When
        Coordinator result = coordinatorService.findByUsername("nonexistent");

        // Then
        assertNull(result);
        verify(coordinatorDao, times(1)).findByUsername("nonexistent");
    }

}
