package com.example.warehouse_inventory.response;

import org.springframework.http.HttpStatus;

public enum ApiStatus {

    SUCCESS(HttpStatus.OK, "Success"),
    CREATED(HttpStatus.CREATED, "Created"),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "Data Not Found"),
    VALIDATION_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "Validation Error"),
    DATA_ALREADY_EXISTS(HttpStatus.CONFLICT, "Data Already Exists"),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown Error");

    private final HttpStatus httpStatus;
    private final String message;

    ApiStatus(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String code() {
        return String.valueOf(httpStatus.value());
    }

    public String message() {
        return message;
    }

}
