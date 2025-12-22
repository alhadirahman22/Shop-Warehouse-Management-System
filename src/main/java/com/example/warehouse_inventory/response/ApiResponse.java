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

    public static <T> ApiResponse<T> success(T data) {
        return of(ApiStatus.SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(ApiStatus.CREATED, data, null);
    }

}

// how to use
// @GetMapping("/{id}")
// public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
// UserDto user = service.getUser(id);
// return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
// .body(ApiResponse.success(user));
// }

// @PostMapping
// public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody @Valid
// CreateUserRequest req) {
// UserDto created = service.create(req);
// URI location = URI.create("/users/" + created.id());
// return ResponseEntity.created(location)
// .body(ApiResponse.created(created));
// }