package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Переводы между картами одного пользователя.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;

    // TODO: добавить пессимистичную блокировку карт (@Lock(PESSIMISTIC_WRITE)) для конкурентных переводов
    /**
     * Перевод средств между двумя картами пользователя.
     * Создаёт запись со статусом PENDING, при успехе — COMPLETED, при ошибке — FAILED.
     */
    public Transaction transfer(Long fromCardId, Long toCardId, BigDecimal amount, User user, String description) {
        log.debug("Перевод: from={}, to={}, amount={}, user={}", fromCardId, toCardId, amount, user.getUsername());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardOperationException("Сумма перевода должна быть положительной");
        }

        Card fromCard = cardService.getCardByIdAndUser(fromCardId, user);
        Card toCard = cardService.getCardByIdAndUser(toCardId, user);

        // Создаём запись о транзакции со статусом PENDING
        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription(description);
        transactionRepository.save(transaction);

        try {
            validateCards(fromCard, toCard, fromCardId, toCardId);
            checkBalance(fromCard, amount, fromCardId);

            fromCard.setBalance(fromCard.getBalance().subtract(amount));
            toCard.setBalance(toCard.getBalance().add(amount));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            transaction.setStatus(TransactionStatus.COMPLETED);
            log.debug("Перевод выполнен: from={}, to={}, amount={}", fromCardId, toCardId, amount);
            return transactionRepository.save(transaction);

        } catch (RuntimeException ex) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            log.debug("Перевод не выполнен: {}", ex.getMessage());
            throw ex;
        }
    }

    private void validateCards(Card fromCard, Card toCard, Long fromCardId, Long toCardId) {
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("Карта отправителя не активна: id=" + fromCardId);
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("Карта получателя не активна: id=" + toCardId);
        }
    }

    private void checkBalance(Card fromCard, BigDecimal amount, Long fromCardId) {
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Недостаточно средств на карте id=" + fromCardId);
        }
    }
}