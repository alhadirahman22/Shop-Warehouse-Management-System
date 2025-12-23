package com.example.warehouse_inventory.dto;

import java.time.Instant;

import com.example.warehouse_inventory.entity.StockMovementType;

public record StockMovementResponse(
        Long id,
        Long variantId,
        Integer changeQty,
        StockMovementType movementType,
        String referenceId,
        Instant createdAt) {
}
