package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;

    public Transaction transfer(Long fromCardId, Long toCardId, BigDecimal amount, User user, String description) {
        log.debug("Перевод: from={}, to={}, amount={}, user={}", fromCardId, toCardId, amount, user.getUsername());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardOperationException("Сумма перевода должна быть положительной");
        }

        // Проверяем, что обе карты принадлежат текущему пользователю
        Card fromCard = cardService.getCardByIdAndUser(fromCardId, user);
        Card toCard = cardService.getCardByIdAndUser(toCardId, user);

        if (fromCard.getStatus() != com.example.bankcards.entity.CardStatus.ACTIVE) {
            throw new CardOperationException("Карта отправителя не активна: id=" + fromCardId);
        }

        if (toCard.getStatus() != com.example.bankcards.entity.CardStatus.ACTIVE) {
            throw new CardOperationException("Карта получателя не активна: id=" + toCardId);
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Недостаточно средств на карте id=" + fromCardId);
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription(description);

        log.debug("Перевод выполнен: from={}, to={}, amount={}", fromCardId, toCardId, amount);
        return transactionRepository.save(transaction);
    }
}