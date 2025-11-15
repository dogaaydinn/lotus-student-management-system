package com.lotus.lotusSPM.service;

import com.lotus.lotusSPM.dao.MessagesDao;
import com.lotus.lotusSPM.model.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessagesServiceImpl
 * Tests all CRUD operations and custom queries
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessagesService Tests")
class MessagesServiceImplTest {

    @Mock
    private MessagesDao messagesDao;

    @InjectMocks
    private MessagesServiceImpl messagesService;

    private Messages testMessage;

    @BeforeEach
    void setUp() {
        testMessage = new Messages();
        testMessage.setId(1L);
        testMessage.setFrom("student@example.com");
        testMessage.setTo("instructor@example.com");
        testMessage.setTitle("Test Message");
        testMessage.setText("This is a test message");
        testMessage.setDate(LocalDate.now());
        testMessage.setTime(LocalTime.now());
    }

    @Test
    @DisplayName("Should create message successfully")
    void testCreateMessage() {
        // Given
        when(messagesDao.save(any(Messages.class))).thenReturn(testMessage);

        // When
        Messages result = messagesService.createMessage(testMessage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFrom()).isEqualTo("student@example.com");
        assertThat(result.getTo()).isEqualTo("instructor@example.com");
        verify(messagesDao, times(1)).save(testMessage);
    }

    @Test
    @DisplayName("Should get all messages")
    void testGetMessages() {
        // Given
        Messages message2 = new Messages();
        message2.setId(2L);
        message2.setFrom("instructor@example.com");
        message2.setTo("student@example.com");
        List<Messages> messagesList = Arrays.asList(testMessage, message2);

        when(messagesDao.findAll()).thenReturn(messagesList);

        // When
        List<Messages> results = messagesService.getMessages();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).containsExactly(testMessage, message2);
        verify(messagesDao, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find message by ID")
    void testFindById() {
        // Given
        when(messagesDao.findById(1L)).thenReturn(Optional.of(testMessage));

        // When
        Messages result = messagesService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Message");
        verify(messagesDao, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when message not found by ID")
    void testFindByIdNotFound() {
        // Given
        when(messagesDao.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> messagesService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Message not found with id: 999");
        verify(messagesDao, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should find messages by recipient (to)")
    void testFindByTo() {
        // Given
        Messages message2 = new Messages();
        message2.setTo("instructor@example.com");
        List<Messages> messages = Arrays.asList(testMessage, message2);

        when(messagesDao.findByTo("instructor@example.com")).thenReturn(messages);

        // When
        List<Messages> results = messagesService.findByTo("instructor@example.com");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(m -> m.getTo().equals("instructor@example.com"));
        verify(messagesDao, times(1)).findByTo("instructor@example.com");
    }

    @Test
    @DisplayName("Should find messages by sender (from)")
    void testFindByFrom() {
        // Given
        Messages message2 = new Messages();
        message2.setFrom("student@example.com");
        List<Messages> messages = Arrays.asList(testMessage, message2);

        when(messagesDao.findByFrom("student@example.com")).thenReturn(messages);

        // When
        List<Messages> results = messagesService.findByFrom("student@example.com");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(m -> m.getFrom().equals("student@example.com"));
        verify(messagesDao, times(1)).findByFrom("student@example.com");
    }

    @Test
    @DisplayName("Should delete message by ID")
    void testDeleteMessage() {
        // Given
        doNothing().when(messagesDao).deleteById(1L);

        // When
        messagesService.deleteMessage(1L);

        // Then
        verify(messagesDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should handle empty result when finding by recipient")
    void testFindByToEmpty() {
        // Given
        when(messagesDao.findByTo("nonexistent@example.com")).thenReturn(Arrays.asList());

        // When
        List<Messages> results = messagesService.findByTo("nonexistent@example.com");

        // Then
        assertThat(results).isEmpty();
        verify(messagesDao, times(1)).findByTo("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should handle empty result when finding by sender")
    void testFindByFromEmpty() {
        // Given
        when(messagesDao.findByFrom("nonexistent@example.com")).thenReturn(Arrays.asList());

        // When
        List<Messages> results = messagesService.findByFrom("nonexistent@example.com");

        // Then
        assertThat(results).isEmpty();
        verify(messagesDao, times(1)).findByFrom("nonexistent@example.com");
    }
}
