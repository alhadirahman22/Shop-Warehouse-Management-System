package com.example.warehouse_inventory.mapper;

import com.example.warehouse_inventory.dto.StockMovementResponse;
import com.example.warehouse_inventory.entity.StockMovement;

public class StockMovementMapper {
    private StockMovementMapper() {
    }

    public static StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getVariantId(),
                movement.getChangeQty(),
                movement.getMovementType(),
                movement.getReferenceId(),
                movement.getCreatedAt());
    }
}
