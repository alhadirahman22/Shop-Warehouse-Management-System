package com.example.warehouse_inventory.exception;

import com.example.warehouse_inventory.response.ApiResponse;
import com.example.warehouse_inventory.response.ApiStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        return ResponseEntity.status(ApiStatus.UNKNOWN_ERROR.httpStatus())
                .body(ApiResponse.withMessage(ApiStatus.UNKNOWN_ERROR, "Unexpected error", null, null));
    }
}

// how to use
// import com.example.warehouse_inventory.exception.NotFoundException;
// @GetMapping("/{id}")
// public ResponseEntity<ApiResponse<String>> get(@PathVariable String id) {
// // misal tidak ketemu
// throw new NotFoundException("Item not found");
// }
