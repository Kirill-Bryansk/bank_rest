package com.example.bankcards.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений.
 * Перехватывает все ошибки контроллеров и приводит их к единому формату {@link ErrorResponse}.
 * Каждая ветка логирует произошедшую ошибку для удобства отладки.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Сущность не найдена -> 404 */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        log.debug("Сущность не найдена: {} | path={}", ex.getMessage(), req.getRequestURI());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    /** Бизнес-ошибка карты -> 400 */
    @ExceptionHandler(CardOperationException.class)
    public ResponseEntity<ErrorResponse> handleCardOperation(CardOperationException ex, HttpServletRequest req) {
        log.debug("Ошибка операции с картой: {} | path={}", ex.getMessage(), req.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /** Недостаточно средств -> 400 */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex, HttpServletRequest req) {
        log.debug("Недостаточно средств: {} | path={}", ex.getMessage(), req.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /** Пользователь уже существует -> 409 */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest req) {
        log.debug("Пользователь уже существует: {} | path={}", ex.getMessage(), req.getRequestURI());
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    /** Неизвестная роль -> 400 */
    @ExceptionHandler(IllegalRoleException.class)
    public ResponseEntity<ErrorResponse> handleIllegalRole(IllegalRoleException ex, HttpServletRequest req) {
        log.debug("Неверная роль: {} | path={}", ex.getMessage(), req.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /** Неверный логин/пароль -> 401 */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        log.debug("Неверные учётные данные | path={}", req.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, "Неверное имя пользователя или пароль", req);
    }

    /** Общая ошибка аутентификации (в т.ч. невалидный JWT) -> 401 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        log.debug("Ошибка аутентификации: {} | path={}", ex.getMessage(), req.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    /** Нет прав на ресурс -> 403 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.debug("Доступ запрещён | path={}", req.getRequestURI());
        return build(HttpStatus.FORBIDDEN, "Доступ запрещён", req);
    }

    /** Ошибка валидации @Valid тела запроса -> 400 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.debug("Ошибка валидации: {} | path={}", details, req.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, details, req);
    }

    /** Неверный тип параметра (например, нечисловой id) -> 400 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Неверное значение параметра '" + ex.getName() + "': " + ex.getValue();
        log.debug("Неверный тип параметра: {} | path={}", msg, req.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    /** Некорректный аргумент (например, Role.valueOf для неизвестной роли) -> 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        log.debug("Некорректный аргумент: {} | path={}", ex.getMessage(), req.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /** Любая другая непредвиденная ошибка -> 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Непредвиденная ошибка | path={}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", req);
    }

    // ------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }
}
