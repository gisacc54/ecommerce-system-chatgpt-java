package com.ecommerce.ecommerce.exception;

import com.ecommerce.ecommerce.dto.RegisterResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler to convert validation errors to JSON responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle @Valid validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        RegisterResponse resp = new RegisterResponse(false, null, errors);
        return new ResponseEntity<>(resp, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // Fallback for IllegalArgumentException (used by service for business validation)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        RegisterResponse resp = new RegisterResponse(false, null, ex.getMessage());
        return ResponseEntity.badRequest().body(resp);
    }
}