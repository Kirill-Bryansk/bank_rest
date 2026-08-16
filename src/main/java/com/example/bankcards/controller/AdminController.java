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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CardService cardService;
    private final UserService userService;

    @PostMapping("/cards")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest request) {
        User user = userService.getById(request.getUserId());
        Card card = adminService.createCardForUser(user, request);
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @PutMapping("/cards/{id}/activate")
    public ResponseEntity<CardResponse> activateCard(@PathVariable Long id) {
        Card card = adminService.activateCard(id);
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @PutMapping("/cards/{id}/block")
    public ResponseEntity<CardResponse> blockCard(@PathVariable Long id) {
        Card card = adminService.blockCard(id);
        return ResponseEntity.ok(CardResponse.fromEntity(card));
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        adminService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponse>> getAllCards(@PageableDefault(size = 10) Pageable pageable) {
        Page<Card> cards = adminService.getAllCards(pageable);
        Page<CardResponse> response = cards.map(CardResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {
        Page<User> users = adminService.getAllUsers(pageable);
        Page<UserResponse> response = users.map(UserResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Long id, @RequestParam String role) {
        User user = adminService.changeUserRole(id, role);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}