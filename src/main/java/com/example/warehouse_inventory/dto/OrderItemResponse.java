package com.example.warehouse_inventory.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long variantId,
        String sku,
        Integer quantity,
        BigDecimal priceAtPurchase) {
}
