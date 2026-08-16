package com.example.bankcards.exception;

/**
 * Недостаточно средств на карте для перевода.
 * Обрабатывается в 400 Bad Request.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
