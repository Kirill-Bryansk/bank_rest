package com.example.bankcards.service;


import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Операции с картами: создание, блокировка, поиск.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;

    /**
     * Создаёт карту с зашифрованным номером и нулевым балансом.
     */
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

    /**
     * Возвращает карты пользователя с пагинацией и опциональной фильтрацией по статусу.
     */
    public Page<Card> getCardsByUser(User user, Pageable pageable, CardStatus status) {
        if (status != null) {
            log.debug("Поиск карт пользователя {} по статусу {}, pageable={}", user.getUsername(), status, pageable);
            return cardRepository.findByUserAndStatus(user, status, pageable);
        }
        log.debug("Поиск карт пользователя {}, pageable={}", user.getUsername(), pageable);
        return cardRepository.findByUser(user, pageable);
    }

    /**
     * Блокировка карты пользователем. Проверяет срок действия и владельца.
     */
    @Transactional
    public Card blockCard(Long cardId, User user) {
        Card card = getCardByIdAndUser(cardId, user);

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardOperationException("Карта уже заблокирована: id=" + cardId);
        }

        card.setStatus(CardStatus.BLOCKED);
        log.debug("Карта id={} заблокирована пользователем {}", cardId, user.getUsername());
        return cardRepository.save(card);
    }

    /**
     * Поиск карты по id и владельцу. Автоматически выставляет EXPIRED если срок истёк.
     */
    public Card getCardByIdAndUser(Long cardId, User user) {
        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена или не принадлежит пользователю: id=" + cardId));

        // Автоматическая проверка срока действия
        if (card.getStatus() == CardStatus.ACTIVE && card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
            log.debug("Карта id={} автоматически переведена в EXPIRED", cardId);
        }

        return card;
    }
}