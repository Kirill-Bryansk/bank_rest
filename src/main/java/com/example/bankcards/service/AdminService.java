package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public Card createCardForUser(User user, CardRequest request) {
        Card card = new Card();
        card.setUser(user);
        card.setCardNumber(request.getCardNumber());
        card.setOwnerName(request.getOwnerName());
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    public Card activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    public Card blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User changeUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(Role.valueOf(role.toUpperCase()));
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}