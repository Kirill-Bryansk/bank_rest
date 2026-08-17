package com.example.bankcards.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private final EncryptionService encryptionService = new EncryptionService(
            "9b3d1f4a7c8e5f2d6a0b3c4d5e6f7a8b",
            "0f8a2b6c4e9d1a3f"
    );

    @Test
    void encrypt_decrypt_round_trip() {
        String original = "4111111111111111";
        String encrypted = encryptionService.encrypt(original);
        String decrypted = encryptionService.decrypt(encrypted);

        assertEquals(original, decrypted);
        assertNotEquals(original, encrypted);
    }

    @Test
    void same_input_same_output() {
        String cardNumber = "4111111111111111";
        String encrypted1 = encryptionService.encrypt(cardNumber);
        String encrypted2 = encryptionService.encrypt(cardNumber);

        // Детерминированное шифрование — одинаковые входные данные дают одинаковый результат
        assertEquals(encrypted1, encrypted2);
    }
}
