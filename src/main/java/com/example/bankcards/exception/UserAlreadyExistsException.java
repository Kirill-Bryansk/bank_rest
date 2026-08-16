package com.example.bankcards.exception;

/**
 * Попытка зарегистрировать уже существующее имя пользователя.
 * Обрабатывается в 409 Conflict.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
