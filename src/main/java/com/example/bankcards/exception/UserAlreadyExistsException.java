package com.example.bankcards.exception;

/** Имя пользователя уже занято (409). */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
