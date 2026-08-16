package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Поиск пользователей по id или имени.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** Поиск по имени. */
    public User getByUsername(String username) {
        log.debug("Поиск пользователя по имени: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + username));
    }

    /** Поиск по id. */
    public User getById(Long id) {
        log.debug("Поиск пользователя по id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден с id: " + id));
    }
}