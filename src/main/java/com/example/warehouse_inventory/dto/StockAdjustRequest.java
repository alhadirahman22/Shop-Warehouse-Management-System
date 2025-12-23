package com.example.warehouse_inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockAdjustRequest(
        @NotNull(message = "Variant ID is required")
        Long variantId,

        @NotNull(message = "Change quantity is required")
        Integer changeQty,

        @Size(max = 64, message = "Reference ID max 64 chars")
        String referenceId) {
}
