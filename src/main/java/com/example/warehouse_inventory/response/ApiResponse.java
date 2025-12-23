package com.example.warehouse_inventory.response;

import java.time.Instant;

public record ApiResponse<T>(
        String code,
        String message,
        T data,
        Object errors,
        Instant timestamp) {

    public static <T> ApiResponse<T> of(ApiStatus status, T data, Object errors) {
        return new ApiResponse<>(status.code(), status.message(), data, errors, Instant.now());
    }

    public static <T> ApiResponse<T> withMessage(ApiStatus status, String message, T data, Object errors) {
        return new ApiResponse<>(status.code(), message, data, errors, Instant.now());
    }

    public static ApiResponse<Void> error(ApiStatus status, Object errors) {
        return of(status, null, errors);
    }

    public static <T> ApiResponse<T> success(T data) {
        return of(ApiStatus.SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(ApiStatus.CREATED, data, null);
    }

}
