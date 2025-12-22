package com.example.warehouse_inventory.mapper;

import com.example.warehouse_inventory.dto.CreateItemRequest;
import com.example.warehouse_inventory.dto.ItemResponse;
import com.example.warehouse_inventory.entity.Item;

public class ItemMapper {
    private ItemMapper() {
    }

    public static Item toEntity(CreateItemRequest req) {
        Item item = new Item();
        item.setName(req.name());
        item.setDescription(req.description());
        item.setActive(req.active());
        return item;
    }

    public static ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getActive());
    }
}
