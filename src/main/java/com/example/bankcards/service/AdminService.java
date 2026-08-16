package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.exception.IllegalRoleException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public Card createCardForUser(User user, CardRequest request) {
        // Проверка уникальности номера карты
        if (cardRepository.existsByCardNumber(request.getCardNumber())) {
            throw new IllegalArgumentException("Карта с таким номером уже существует: " + request.getCardNumber());
        }

        Card card = new Card();
        card.setUser(user);
        card.setCardNumber(request.getCardNumber());
        card.setOwnerName(user.getUsername());
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(CardStatus.ACTIVE);
        log.debug("Создание карты {} для пользователя id={}", request.getCardNumber(), user.getId());
        return cardRepository.save(card);
    }

    public Card activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена с id: " + cardId));
        card.setStatus(CardStatus.ACTIVE);
        log.debug("Карта id={} активирована", cardId);
        return cardRepository.save(card);
    }

    public Card blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена с id: " + cardId));
        card.setStatus(CardStatus.BLOCKED);
        log.debug("Карта id={} заблокирована администратором", cardId);
        return cardRepository.save(card);
    }

    public void deleteCard(Long cardId) {
        log.debug("Удаление карты id={}", cardId);
        cardRepository.deleteById(cardId);
    }

    public Page<Card> getAllCards(Pageable pageable) {
        log.debug("Запрос всех карт, pageable={}", pageable);
        return cardRepository.findAll(pageable);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        log.debug("Запрос всех пользователей, pageable={}", pageable);
        return userRepository.findAll(pageable);
    }

    public User changeUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден с id: " + userId));
        Role newRole;
        try {
            newRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalRoleException("Неизвестная роль: " + role);
        }
        user.setRole(newRole);
        log.debug("Роль пользователя id={} изменена на {}", userId, newRole);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        log.debug("Удаление пользователя id={}", userId);
        userRepository.deleteById(userId);
    }
}