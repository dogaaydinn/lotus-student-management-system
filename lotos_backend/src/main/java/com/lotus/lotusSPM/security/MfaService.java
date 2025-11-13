package com.lotus.lotusSPM.security;

import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Multi-Factor Authentication Service using TOTP (Time-based One-Time Password)
 * RFC 6238 compliant implementation
 */
@Service
public class MfaService {

    private static final int SECRET_SIZE = 20; // 160 bits
    private static final int TIME_STEP = 30; // seconds
    private static final int CODE_DIGITS = 6;

    /**
     * Generate a new MFA secret for a user
     */
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Generate TOTP code for verification
     */
    public String generateCode(String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        long time = System.currentTimeMillis() / 1000L / TIME_STEP;
        return generateCode(secret, time);
    }

    /**
     * Generate TOTP code for specific time
     */
    private String generateCode(String secret, long time) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] key = Base64.getDecoder().decode(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(time).array();

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                     ((hash[offset + 1] & 0xFF) << 16) |
                     ((hash[offset + 2] & 0xFF) << 8) |
                     (hash[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, CODE_DIGITS);
        return String.format("%0" + CODE_DIGITS + "d", otp);
    }

    /**
     * Verify TOTP code with time window
     */
    public boolean verifyCode(String secret, String code) {
        try {
            long time = System.currentTimeMillis() / 1000L / TIME_STEP;

            // Check current time window and ±1 window for clock skew
            for (int i = -1; i <= 1; i++) {
                String generated = generateCode(secret, time + i);
                if (generated.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generate QR code URL for Google Authenticator
     */
    public String getQrCodeUrl(String secret, String username, String issuer) {
        String encodedSecret = secret.replace("=", "");
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s",
            issuer, username, encodedSecret, issuer
        );
    }
}
