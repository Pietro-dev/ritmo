package app.ritmo.aplicacaoritmo.infra.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class EmailEncryptionConverter implements AttributeConverter<String, String> {
    private static final String PREFIX = "enc:v1:";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String convertToDatabaseColumn(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        if (email.startsWith(PREFIX)) {
            return email;
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv).put(encrypted);
            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível criptografar o e-mail.", ex);
        }
    }

    @Override
    public String convertToEntityAttribute(String encryptedEmail) {
        if (encryptedEmail == null || encryptedEmail.isBlank()) {
            return encryptedEmail;
        }
        if (!encryptedEmail.startsWith(PREFIX)) {
            return encryptedEmail;
        }

        try {
            byte[] payload = Base64.getDecoder().decode(encryptedEmail.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível descriptografar o e-mail.", ex);
        }
    }

    private SecretKey secretKey() {
        String configuredKey = System.getenv("EMAIL_ENCRYPTION_KEY");
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new IllegalStateException(
                    "Configure a variável de ambiente EMAIL_ENCRYPTION_KEY."
            );
        }
        try {
            byte[] key = MessageDigest.getInstance("SHA-256")
                    .digest(configuredKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(key, "AES");
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível carregar a chave de criptografia.", ex);
        }
    }
}
