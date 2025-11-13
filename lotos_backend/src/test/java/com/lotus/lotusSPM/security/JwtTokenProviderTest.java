package com.lotus.lotusSPM.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D635166546A576E5A7234743777217A25432A");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000);
    }

    @Test
    void testGenerateToken() {
        // Given
        UserPrincipal userPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", "password", "STUDENT");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", 1L, "STUDENT");

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testGetUserIdFromToken() {
        // Given
        Long userId = 123L;
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", userId, "STUDENT");

        // When
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testGetUsernameFromToken() {
        // Given
        String username = "testuser";
        String token = jwtTokenProvider.generateTokenFromUsername(username, 1L, "STUDENT");

        // When
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetRoleFromToken() {
        // Given
        String role = "STUDENT";
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", 1L, role);

        // When
        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        // Then
        assertEquals(role, extractedRole);
    }
}
