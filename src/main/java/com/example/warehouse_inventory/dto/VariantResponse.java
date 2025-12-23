package com.example.warehouse_inventory.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

public record VariantResponse(
        Long id,
        Long itemId,
        String sku,
        String variantName,
        JsonNode attributes,
        BigDecimal price,
        Boolean active) {
}
