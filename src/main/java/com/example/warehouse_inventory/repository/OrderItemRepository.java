package com.example.warehouse_inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.warehouse_inventory.entity.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}
