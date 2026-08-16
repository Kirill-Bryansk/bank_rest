package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.AdminService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CardService cardService;
    private final UserService userService;

    @PostMapping("/cards")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest request) {
        log.debug("ADMIN: создание карты для userId={}", request.getUserId());
        User user = userService.getById(request.getUserId());
        Card card = adminService.createCardForUser(user, request);
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @PutMapping("/cards/{id}/activate")
    public ResponseEntity<CardResponse> activateCard(@PathVariable Long id) {
        log.debug("ADMIN: активация карты id={}", id);
        Card card = adminService.activateCard(id);
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @PutMapping("/cards/{id}/block")
    public ResponseEntity<CardResponse> blockCard(@PathVariable Long id) {
        log.debug("ADMIN: блокировка карты id={}", id);
        Card card = adminService.blockCard(id);
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        log.debug("ADMIN: удаление карты id={}", id);
        adminService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponse>> getAllCards(@PageableDefault(size = 10) Pageable pageable) {
        log.debug("ADMIN: запрос всех карт, pageable={}", pageable);
        Page<Card> cards = adminService.getAllCards(pageable);
        Page<CardResponse> response = cards.map(CardResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {
        log.debug("ADMIN: запрос всех пользователей, pageable={}", pageable);
        Page<User> users = adminService.getAllUsers(pageable);
        Page<UserResponse> response = users.map(UserResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Long id, @RequestParam String role) {
        log.debug("ADMIN: смена роли пользователя id={} на {}", id, role);
        User user = adminService.changeUserRole(id, role);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.debug("ADMIN: удаление пользователя id={}", id);
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}