package com.example.bankcards.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class CardRequest {
    @NotBlank
    @Size(min = 16, max = 19)
    private String cardNumber;

    @NotNull
    private LocalDate expiryDate;

    private Long userId; // для ADMIN
}