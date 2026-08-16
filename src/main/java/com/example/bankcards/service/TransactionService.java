package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;

    public Transaction transfer(Long fromCardId, Long toCardId, BigDecimal amount, User user, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        Card fromCard = cardService.getCardByIdAndUser(fromCardId, user);
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new RuntimeException("Target card not found"));

        if (fromCard.getStatus() != com.example.bankcards.entity.CardStatus.ACTIVE) {
            throw new RuntimeException("Source card is not active");
        }

        if (toCard.getStatus() != com.example.bankcards.entity.CardStatus.ACTIVE) {
            throw new RuntimeException("Target card is not active");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
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

        return transactionRepository.save(transaction);
    }
}