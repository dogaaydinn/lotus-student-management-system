package com.lotus.lotusSPM.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @InjectMocks
    private MfaService mfaService;

    @Test
    void testGenerateSecret() {
        // When
        String secret = mfaService.generateSecret();

        // Then
        assertNotNull(secret);
        assertTrue(secret.length() > 0);
        // Base64 encoded 20 bytes should be ~28 characters
        assertTrue(secret.length() >= 20);
    }

    @Test
    void testGenerateQrCodeUrl() {
        // Given
        String secret = "TESTSECRET123456";
        String username = "testuser";

        // When
        String qrUrl = mfaService.generateQrCodeUrl(username, secret);

        // Then
        assertNotNull(qrUrl);
        assertTrue(qrUrl.startsWith("otpauth://totp/"));
        assertTrue(qrUrl.contains(username));
        assertTrue(qrUrl.contains(secret));
        assertTrue(qrUrl.contains("Lotus%20SPM")); // URL encoded issuer
    }

    @Test
    void testVerifyCode_ValidCode() {
        // Given
        String secret = mfaService.generateSecret();
        String validCode = mfaService.generateCurrentCode(secret);

        // When
        boolean isValid = mfaService.verifyCode(secret, validCode);

        // Then
        assertTrue(isValid, "Valid TOTP code should be verified successfully");
    }

    @Test
    void testVerifyCode_InvalidCode() {
        // Given
        String secret = mfaService.generateSecret();
        String invalidCode = "000000";

        // When
        boolean isValid = mfaService.verifyCode(secret, invalidCode);

        // Then
        // This might occasionally pass by chance (1/1000000 probability)
        // but statistically it should fail
        assertFalse(isValid, "Invalid code should not be verified");
    }

    @Test
    void testVerifyCode_EmptyCode() {
        // Given
        String secret = mfaService.generateSecret();

        // When & Then
        assertThrows(Exception.class, () -> {
            mfaService.verifyCode(secret, "");
        });
    }

    @Test
    void testVerifyCode_NullSecret() {
        // When & Then
        assertThrows(Exception.class, () -> {
            mfaService.verifyCode(null, "123456");
        });
    }

    @Test
    void testGenerateTwoSecretsAreDifferent() {
        // When
        String secret1 = mfaService.generateSecret();
        String secret2 = mfaService.generateSecret();

        // Then
        assertNotEquals(secret1, secret2, "Each generated secret should be unique");
    }

    @Test
    void testGenerateCurrentCode() {
        // Given
        String secret = mfaService.generateSecret();

        // When
        String code = mfaService.generateCurrentCode(secret);

        // Then
        assertNotNull(code);
        assertEquals(6, code.length(), "TOTP code should be 6 digits");
        assertTrue(code.matches("\\d{6}"), "TOTP code should contain only digits");
    }
}
