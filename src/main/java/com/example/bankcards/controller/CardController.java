package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.EncryptionService;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * API пользователя: просмотр карт, баланс, блокировка, переводы.
 */
@Slf4j
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final TransactionService transactionService;
    private final UserService userService;
    private final EncryptionService encryptionService;

    /** Просмотр своих карт с пагинацией и фильтрацией по статусу. */
    @GetMapping
    public ResponseEntity<Page<CardResponse>> getMyCards(
            Authentication auth,
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) CardStatus status) {

        log.debug("Запрос карт пользователем {}, status={}, pageable={}", auth.getName(), status, pageable);
        User user = userService.getByUsername(auth.getName());
        Page<Card> cards = cardService.getCardsByUser(user, pageable, status);
        Page<CardResponse> response = cards.map(card -> CardResponse.fromEntity(card, encryptionService));

        return ResponseEntity.ok(response);
    }

    /** Просмотр баланса конкретной карты. */
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            Authentication auth,
            @PathVariable Long id) {

        log.debug("Запрос баланса карты id={} пользователем {}", id, auth.getName());
        User user = userService.getByUsername(auth.getName());
        Card card = cardService.getCardByIdAndUser(id, user);
        return ResponseEntity.ok(card.getBalance());
    }

    // TODO: создать TransactionResponse DTO вместо возврата сущности
    /** Перевод между своими картами. */
    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(
            Authentication auth,
            @Valid @RequestBody TransferRequest request) {

        log.debug("Перевод от пользователя {}: from={}, to={}, amount={}",
                auth.getName(), request.getFromCardId(), request.getToCardId(), request.getAmount());
        User user = userService.getByUsername(auth.getName());
        Transaction transaction = transactionService.transfer(
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount(),
                user,
                request.getDescription()
        );

        return ResponseEntity.ok(transaction);
    }

    /** Блокировка своей карты. */
    @PutMapping("/{id}/block")
    public ResponseEntity<CardResponse> blockCard(
            Authentication auth,
            @PathVariable Long id) {

        log.debug("Блокировка карты id={} пользователем {}", id, auth.getName());
        User user = userService.getByUsername(auth.getName());
        Card card = cardService.blockCard(id, user);

        return ResponseEntity.ok(CardResponse.fromEntity(card, encryptionService));
    }
}