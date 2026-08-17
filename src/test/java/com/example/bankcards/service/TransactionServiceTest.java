package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(1000));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setUser(user);

        toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setUser(user);
    }

    @Test
    void transfer_success() {
        when(cardService.getCardByIdAndUser(1L, user)).thenReturn(fromCard);
        when(cardService.getCardByIdAndUser(2L, user)).thenReturn(toCard);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.transfer(1L, 2L, BigDecimal.valueOf(100), user, "test");

        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(900, fromCard.getBalance().intValue());
        assertEquals(600, toCard.getBalance().intValue());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void transfer_insufficientBalance() {
        when(cardService.getCardByIdAndUser(1L, user)).thenReturn(fromCard);
        when(cardService.getCardByIdAndUser(2L, user)).thenReturn(toCard);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> transactionService.transfer(1L, 2L, BigDecimal.valueOf(2000), user, "test"));

        assertTrue(ex.getMessage().contains("Недостаточно средств"));
    }
}
