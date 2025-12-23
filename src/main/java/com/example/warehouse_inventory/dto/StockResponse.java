package com.example.warehouse_inventory.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;

public record StockResponse(
                Long variantId,
                String sku,
                Integer quantity,
                Instant updatedAt,
                String price,
                JsonNode attributes,
                String variantName) {
}
