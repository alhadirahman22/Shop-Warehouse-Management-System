package com.example.warehouse_inventory.seed;

import com.example.warehouse_inventory.dto.CreateItemRequest;
import com.example.warehouse_inventory.dto.CreateVariantRequest;
import com.example.warehouse_inventory.dto.StockAdjustRequest;
import com.example.warehouse_inventory.dto.StockInRequest;
import com.example.warehouse_inventory.dto.StockOutRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;

public class DummyDataFactory {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public CreateItemRequest item(String name, String description, boolean active) {
        return new CreateItemRequest(name, description, active);
    }

    public CreateVariantRequest variant(
            Long itemId,
            String variantName,
            Map<String, String> attributes,
            BigDecimal price,
            boolean active) {
        return new CreateVariantRequest(itemId, variantName, toJson(attributes), price, active);
    }

    public StockInRequest stockIn(Long variantId, int quantity, String referenceId) {
        return new StockInRequest(variantId, quantity, referenceId);
    }

    public StockOutRequest stockOut(Long variantId, int quantity, String referenceId) {
        return new StockOutRequest(variantId, quantity, referenceId);
    }

    public StockAdjustRequest stockAdjust(Long variantId, int changeQty, String referenceId) {
        return new StockAdjustRequest(variantId, changeQty, referenceId);
    }

    private String toJson(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attributes);
        } catch (Exception ex) {
            return null;
        }
    }
}
