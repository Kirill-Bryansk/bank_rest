package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardResponse {
    private Long id;
    private String cardNumber;
    private String ownerName;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;

    public static CardResponse fromEntity(Card card) {
        CardResponse dto = new CardResponse();
        dto.setId(card.getId());
        dto.setCardNumber(maskCardNumber(card.getCardNumber()));
        dto.setOwnerName(card.getOwnerName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }

    private static String maskCardNumber(String encryptedNumber) {
        // TODO: расшифровать номер и замаскировать
        return "**** **** **** " + encryptedNumber.substring(encryptedNumber.length() - 4);
    }
}