package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByUser(User user, Pageable pageable);
    Optional<Card> findByIdAndUser(Long id, User user);
    boolean existsByCardNumber(String cardNumber);

    /** Фильтрация карт пользователя по статусу. */
    Page<Card> findByUserAndStatus(User user, CardStatus status, Pageable pageable);
}
