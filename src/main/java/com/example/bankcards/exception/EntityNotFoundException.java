package com.example.bankcards.exception;

/** Сущность не найдена (404). */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
