package com.example.bankcards.exception;

/**
 * Передана неизвестная роль пользователя.
 * Обрабатывается в 400 Bad Request.
 */
public class IllegalRoleException extends RuntimeException {

    public IllegalRoleException(String message) {
        super(message);
    }
}
