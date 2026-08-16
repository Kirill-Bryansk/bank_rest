package com.example.bankcards.exception;

/** Бизнес-ошибка карты: не активна, уже заблокирована и т.п. (400). */
public class CardOperationException extends RuntimeException {

    public CardOperationException(String message) {
        super(message);
    }
}
