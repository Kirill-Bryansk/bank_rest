package com.example.bankcards.exception;

/**
 * Бизнес-ошибка при работе с картой: карта уже заблокирована,
    карта не активна, карта не принадлежит пользователю и т.п.
 * Обрабатывается в 400 Bad Request.
 */
public class CardOperationException extends RuntimeException {

    public CardOperationException(String message) {
        super(message);
    }
}
