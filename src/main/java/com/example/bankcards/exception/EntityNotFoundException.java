package com.example.bankcards.exception;

/**
 * Бросается, когда запрошенная сущность (карта, пользователь) не найдена.
 * Обрабатывается в 404 Not Found.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
