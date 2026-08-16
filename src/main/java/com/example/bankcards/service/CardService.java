package com.example.bankcards.service;


import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;

    @Transactional
    public Card createCard(User user, String ownerName, String cardNumber, LocalDate expiryDate) {
        Card card = new Card();
        card.setUser(user);
        card.setOwnerName(ownerName);
        card.setCardNumber(cardNumber);
        card.setExpiryDate(expiryDate);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        return cardRepository.save(card);
    }

    @Transactional
    public Card blockCard(Long cardId, User user) {
        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new RuntimeException("Card not found or not owned by user"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new RuntimeException("Card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    public Card getCardByIdAndUser(Long cardId, User user) {
            return cardRepository.findByIdAndUser(cardId, user)
                    .orElseThrow(() -> new RuntimeException("Card not found or not owned by user"));
    }
}