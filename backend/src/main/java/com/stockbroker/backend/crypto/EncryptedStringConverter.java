package com.stockbroker.backend.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256/GCM field-level encryption for sensitive columns (PAN, bank
 * account number) - implements SRS FR15 "AES-256 encryption at rest for
 * all sensitive data fields". Key is a 256-bit Base64 secret read from
 * application.properties (security.encryption.key) - never hardcoded.
 *
 * Note: since every encryption uses a fresh random IV, ciphertext for the
 * same plaintext differs on every save. A DB-level UNIQUE constraint on an
 * encrypted column therefore cannot enforce plaintext-level uniqueness -
 * application-level duplicate checks on these fields are not implemented
 * here (documented simplification).
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final SecretKeySpec keySpec;

    public EncryptedStringConverter(
            @Value("${security.encryption.key}") String base64Key) {

        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {

        if (attribute == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec,
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] cipherText = cipher.doFinal(
                    attribute.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt field", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {

        if (dbData == null) {
            return null;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(dbData);

            byte[] iv = new byte[IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);

            byte[] cipherText = new byte[combined.length - IV_LENGTH_BYTES];
            System.arraycopy(combined, IV_LENGTH_BYTES, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec,
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt field", e);
        }
    }
}
