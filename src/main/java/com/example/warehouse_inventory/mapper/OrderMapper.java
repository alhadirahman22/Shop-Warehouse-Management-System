package com.example.warehouse_inventory.mapper;

import com.example.warehouse_inventory.dto.OrderItemResponse;
import com.example.warehouse_inventory.dto.OrderResponse;
import com.example.warehouse_inventory.entity.Order;
import com.example.warehouse_inventory.entity.OrderItem;
import java.util.List;
import java.util.Map;

public class OrderMapper {
    private OrderMapper() {
    }

    public static OrderResponse toResponse(
            Order order,
            List<OrderItem> items,
            Map<Long, String> skuByVariantId) {
        Map<Long, String> safeSkuMap = skuByVariantId == null ? Map.of() : skuByVariantId;
        List<OrderItemResponse> itemResponses = items == null
                ? List.of()
                : items.stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getVariantId(),
                                safeSkuMap.get(item.getVariantId()),
                                item.getQuantity(),
                                item.getPriceAtPurchase()))
                        .toList();
        return new OrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                itemResponses);
    }
}
