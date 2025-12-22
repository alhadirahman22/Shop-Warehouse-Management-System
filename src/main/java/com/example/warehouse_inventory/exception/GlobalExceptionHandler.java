package com.example.warehouse_inventory.exception;

import com.example.warehouse_inventory.response.ApiResponse;
import com.example.warehouse_inventory.response.ApiStatus;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(ApiStatus.DATA_NOT_FOUND.httpStatus())
                .body(ApiResponse.withMessage(ApiStatus.DATA_NOT_FOUND, ex.getMessage(), null, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> a,
                        LinkedHashMap::new));
        return ResponseEntity.status(ApiStatus.VALIDATION_ERROR.httpStatus())
                .body(ApiResponse.error(ApiStatus.VALIDATION_ERROR, errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String field = ife.getPath().get(0).getFieldName();
            String expected = ife.getTargetType() != null ? ife.getTargetType().getSimpleName() : "";
            String message = "Invalid value for " + field;
            if (!expected.isBlank()) {
                message = message + " (expected " + expected + ")";
            }
            errors.put(field, message);
        }
        Object errorBody = errors.isEmpty() ? "Invalid JSON payload" : errors;
        return ResponseEntity.status(ApiStatus.VALIDATION_ERROR.httpStatus())
                .body(ApiResponse.error(ApiStatus.VALIDATION_ERROR, errorBody));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        return ResponseEntity.status(ApiStatus.UNKNOWN_ERROR.httpStatus())
                .body(ApiResponse.withMessage(ApiStatus.UNKNOWN_ERROR, "Unexpected error", null, null));
    }

    @ExceptionHandler(DataAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DataAlreadyExistsException ex) {
        return ResponseEntity.status(ApiStatus.DATA_ALREADY_EXISTS.httpStatus())
                .body(ApiResponse.withMessage(ApiStatus.DATA_ALREADY_EXISTS, ex.getMessage(), null, null));
    }
}

// how to use
// import com.example.warehouse_inventory.exception.NotFoundException;
// @GetMapping("/{id}")
// public ResponseEntity<ApiResponse<String>> get(@PathVariable String id) {
// // misal tidak ketemu
// throw new NotFoundException("Item not found");
// }
