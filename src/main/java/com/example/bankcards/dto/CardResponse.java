package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.util.EncryptionService;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO карты с замаскированным номером. */
@Data
public class CardResponse {
    private Long id;
    private String cardNumber;
    private String ownerName;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;

    /** Создаёт DTO из сущности, расшифровывает и маскирует номер. */
    public static CardResponse fromEntity(Card card, EncryptionService encryptionService) {
        CardResponse dto = new CardResponse();
        dto.setId(card.getId());
        dto.setCardNumber(maskCardNumber(encryptionService.decrypt(card.getCardNumber())));
        dto.setOwnerName(card.getOwnerName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}