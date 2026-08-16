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
import com.example.bankcards.util.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Операции администратора: управление картами и пользователями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    /** Создаёт карту с зашифрованным номером. */
    public Card createCardForUser(User user, CardRequest request) {
        String encryptedNumber = encryptionService.encrypt(request.getCardNumber());

        if (cardRepository.existsByCardNumber(encryptedNumber)) {
            throw new IllegalArgumentException("Карта с таким номером уже существует");
        }

        Card card = new Card();
        card.setUser(user);
        card.setCardNumber(encryptedNumber);
        card.setOwnerName(user.getUsername());
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(CardStatus.ACTIVE);
        log.debug("Создание карты для пользователя id={}", user.getId());
        return cardRepository.save(card);
    }

    /** Активирует карту по id. */
    public Card activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена с id: " + cardId));
        card.setStatus(CardStatus.ACTIVE);
        log.debug("Карта id={} активирована", cardId);
        return cardRepository.save(card);
    }

    /** Блокирует карту по id. */
    public Card blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена с id: " + cardId));
        card.setStatus(CardStatus.BLOCKED);
        log.debug("Карта id={} заблокирована администратором", cardId);
        return cardRepository.save(card);
    }

    /** Удаляет карту по id. */
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new EntityNotFoundException("Карта не найдена с id: " + cardId);
        }
        log.debug("Удаление карты id={}", cardId);
        cardRepository.deleteById(cardId);
    }

    /** Возвращает все карты с пагинацией. */
    public Page<Card> getAllCards(Pageable pageable) {
        log.debug("Запрос всех карт, pageable={}", pageable);
        return cardRepository.findAll(pageable);
    }

    /** Возвращает всех пользователей с пагинацией. */
    public Page<User> getAllUsers(Pageable pageable) {
        log.debug("Запрос всех пользователей, pageable={}", pageable);
        return userRepository.findAll(pageable);
    }

    /** Меняет роль пользователя. */
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

    /** Удаляет пользователя по id. */
    // TODO: проверять, нет ли карт у пользователя перед удалением
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь не найден с id: " + userId);
        }
        log.debug("Удаление пользователя id={}", userId);
        userRepository.deleteById(userId);
    }
}