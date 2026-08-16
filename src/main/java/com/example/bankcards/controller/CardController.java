package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final TransactionService transactionService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getMyCards(
            Authentication auth,
            @PageableDefault(size = 10) Pageable pageable) {

        User user = userService.getByUsername(auth.getName());
        Page<Card> cards = cardService.getCardsByUser(user, pageable);
        Page<CardResponse> response = cards.map(CardResponse::fromEntity);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(
            Authentication auth,
            @RequestBody TransferRequest request) {

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

    @PutMapping("/{id}/block")
    public ResponseEntity<CardResponse> blockCard(
            Authentication auth,
            @PathVariable Long id) {

        User user = userService.getByUsername(auth.getName());
        Card card = cardService.blockCard(id, user);

        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }
}