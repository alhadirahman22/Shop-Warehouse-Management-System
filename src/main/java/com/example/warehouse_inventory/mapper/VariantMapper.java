package com.example.warehouse_inventory.mapper;

import com.example.warehouse_inventory.dto.CreateVariantRequest;
import com.example.warehouse_inventory.dto.VariantResponse;
import com.example.warehouse_inventory.entity.Variant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

public class VariantMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private VariantMapper() {
    }

    public static Variant toEntity(CreateVariantRequest req) {
        Variant variant = new Variant();
        variant.setItemId(req.itemId());
        variant.setVariantName(req.variantName());
        variant.setAttributes(req.attributes());
        variant.setPrice(req.price());
        variant.setActive(req.active());
        return variant;
    }

    public static VariantResponse toResponse(Variant variant) {
        return new VariantResponse(
                variant.getId(),
                variant.getItemId(),
                variant.getSku(),
                variant.getVariantName(),
                parseAttributes(variant.getAttributes()),
                variant.getPrice(),
                variant.getActive());
    }

    private static JsonNode parseAttributes(String attributes) {
        if (attributes == null || attributes.isBlank()) {
            return NullNode.getInstance();
        }
        try {
            return OBJECT_MAPPER.readTree(attributes);
        } catch (Exception ex) {
            return NullNode.getInstance();
        }
    }
}
