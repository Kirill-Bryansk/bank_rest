package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        card = new Card();
        card.setId(1L);
        card.setCardNumber("4111111111111111");
        card.setOwnerName("testuser");
        card.setExpiryDate(LocalDate.now().plusYears(1));
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);
        card.setBalance(java.math.BigDecimal.valueOf(1000));
    }

    @Test
    void blockCard_success() {
        when(cardRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        Card result = cardService.blockCard(1L, user);

        assertEquals(CardStatus.BLOCKED, result.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_alreadyBlocked() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(card));

        assertThrows(CardOperationException.class, () -> cardService.blockCard(1L, user));
    }

    @Test
    void blockCard_notFound() {
        when(cardRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cardService.blockCard(1L, user));
    }
}
