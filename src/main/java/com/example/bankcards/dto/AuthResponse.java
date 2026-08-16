package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Ответ с JWT-токеном после входа. */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
}