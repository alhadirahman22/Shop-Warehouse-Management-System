package com.example.warehouse_inventory.dto;

public record ItemResponse(
        Long id,
        String name,
        String description,
        Boolean active) {
}