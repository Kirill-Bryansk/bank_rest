package com.example.bankcards.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Единый формат тела ошибки для всех ответов REST API.
 * Используется глобальным обработчиком исключений.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;       // короткое название ошибки (HTTP reason)
    private String message;     // человекочитаемое сообщение
    private String path;        // URI запроса, на котором произошла ошибка
}
