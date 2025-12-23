package com.example.warehouse_inventory.mapper;

import com.example.warehouse_inventory.dto.StockMovementResponse;
import com.example.warehouse_inventory.entity.StockMovement;
import com.example.warehouse_inventory.entity.Variant;

public class StockMovementMapper {
    private StockMovementMapper() {
    }

    public static StockMovementResponse toResponse(StockMovement movement) {
        Variant variant = movement.getVariant();
        String sku = variant != null ? variant.getSku() : null;
        String variantName = variant != null ? variant.getVariantName() : null;
        return new StockMovementResponse(
                movement.getId(),
                movement.getVariantId(),
                sku,
                variantName,
                movement.getChangeQty(),
                movement.getMovementType(),
                movement.getReferenceId(),
                movement.getCreatedAt());
    }
}
