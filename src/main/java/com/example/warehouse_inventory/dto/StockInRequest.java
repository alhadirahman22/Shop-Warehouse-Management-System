package com.example.warehouse_inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockInRequest(
        @NotNull(message = "Variant ID is required")
        Long variantId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than zero")
        Integer quantity,

        @Size(max = 64, message = "Reference ID max 64 chars")
        String referenceId) {
}
