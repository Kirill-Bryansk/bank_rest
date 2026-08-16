package com.example.bankcards.service;


import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;

    @Transactional
    public Card createCard(User user, String ownerName, String cardNumber, LocalDate expiryDate) {
        String encryptedNumber = encryptionService.encrypt(cardNumber);

        Card card = new Card();
        card.setUser(user);
        card.setOwnerName(ownerName);
        card.setCardNumber(encryptedNumber);
        card.setExpiryDate(expiryDate);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        log.debug("Создание карты для пользователя id={}", user.getId());
        return cardRepository.save(card);
    }

    /** Возвращает карты пользователя с пагинацией. */
    public Page<Card> getCardsByUser(User user, Pageable pageable) {
        log.debug("Поиск карт пользователя {}, pageable={}", user.getUsername(), pageable);
        return cardRepository.findByUser(user, pageable);
    }

    @Transactional
    public Card blockCard(Long cardId, User user) {
        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена или не принадлежит пользователю: id=" + cardId));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardOperationException("Карта уже заблокирована: id=" + cardId);
        }

        card.setStatus(CardStatus.BLOCKED);
        log.debug("Карта id={} заблокирована пользователем {}", cardId, user.getUsername());
        return cardRepository.save(card);
    }

    public Card getCardByIdAndUser(Long cardId, User user) {
        log.debug("Поиск карты id={} для пользователя {}", cardId, user.getUsername());
        return cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена или не принадлежит пользователю: id=" + cardId));
    }
}