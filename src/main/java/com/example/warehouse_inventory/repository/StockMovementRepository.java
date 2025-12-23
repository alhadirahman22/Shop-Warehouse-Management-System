package com.example.warehouse_inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.warehouse_inventory.entity.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}
