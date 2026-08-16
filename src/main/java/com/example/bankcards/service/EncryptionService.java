package com.example.bankcards.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Сервис симметричного шифрования номеров банковских карт (AES/CBC/PKCS5Padding).
 * <p>
 * Использует фиксированный IV из конфигурации, чтобы шифрование было
 * детерминированным — одинаковые номера карт дают одинаковый шифротекст.
 * Это позволяет проверять уникальность номера через БД (existsByCardNumber).
 */
@Slf4j
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private final SecretKeySpec secretKey;
    private final IvParameterSpec ivParameterSpec;

    public EncryptionService(
            @Value("${encryption.secret}") String secret,
            @Value("${encryption.iv}") String iv
    ) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        this.ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        log.debug("EncryptionService инициализирован");
    }

    /**
     * Шифрует plaintext (номер карты) и возвращает Base64-строку.
     */
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования", e);
        }
    }

    /**
     * Расшифровывает Base64-строку и возвращает plaintext (номер карты).
     */
    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка расшифрования", e);
        }
    }
}
