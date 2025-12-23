package com.example.warehouse_inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.warehouse_inventory.entity.Order;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByOrderNo(String orderNo);

    Optional<Order> findByOrderNo(String orderNo);
}
