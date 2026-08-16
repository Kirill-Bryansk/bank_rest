package com.example.bankcards.exception;

/** Недостаточно средств для перевода (400). */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
