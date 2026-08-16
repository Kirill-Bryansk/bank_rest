package com.example.bankcards.exception;

/** Неизвестная роль пользователя (400). */
public class IllegalRoleException extends RuntimeException {

    public IllegalRoleException(String message) {
        super(message);
    }
}
