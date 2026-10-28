package com.nandubodra.sitesentinel.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(basePackages = "com.nandubodra.sitesentinel.api")
public class ApiExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class})
    ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", sanitize(ex.getMessage()), request.getRequestURI(), Instant.now()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    ResponseEntity<ErrorResponse> validation(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Validation failed", request.getRequestURI(), Instant.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> forbidden(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("FORBIDDEN", "Forbidden", request.getRequestURI(), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Internal error", request.getRequestURI(), Instant.now()));
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return "Invalid request";
        }
        String sanitized = message.replaceAll("[\r\n]", " ").trim();
        return sanitized.length() > 180 ? sanitized.substring(0, 180) : sanitized;
    }

    record ErrorResponse(String code, String message, String path, Instant timestamp) {}
}
