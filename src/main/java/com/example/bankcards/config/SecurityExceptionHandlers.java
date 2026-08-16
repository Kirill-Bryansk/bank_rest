package com.example.bankcards.config;

import com.example.bankcards.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Обработчики ошибок Spring Security.
 * Приводят ответы 401/403 к единному JSON-формату {@link ErrorResponse},
 * чтобы в Postman всегда приходил структурированный ответ, а не HTML-страница.
 * Логгирует контекст (user, роли, URL) для отладки.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityExceptionHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /** 401 — нет токена / невалидный токен / не аутентифицирован */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        String userInfo = getAuthInfo();
        log.debug("401 | {} | path={} | method={} | auth={}",
                ex.getClass().getSimpleName(), request.getRequestURI(), request.getMethod(), userInfo);
        write(response, HttpStatus.UNAUTHORIZED, "Требуется аутентификация", request.getRequestURI());
    }

    /** 403 — аутентифицирован, но недостаточно прав */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        String userInfo = getAuthInfo();
        log.debug("403 | {} | path={} | method={} | user={}",
                ex.getClass().getSimpleName(), request.getRequestURI(), request.getMethod(), userInfo);
        write(response, HttpStatus.FORBIDDEN, "Доступ запрещён", request.getRequestURI());
    }

    /** Извлекает информацию об аутентификации из SecurityContext. */
    private String getAuthInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "anonymous";
        }
        String username = auth.getName();
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        return String.format("%s [%s]", username, roles);
    }

    private void write(HttpServletResponse response, HttpStatus status, String message, String path)
            throws IOException {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
