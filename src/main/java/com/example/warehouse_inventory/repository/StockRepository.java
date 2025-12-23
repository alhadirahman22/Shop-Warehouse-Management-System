package com.example.warehouse_inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.warehouse_inventory.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
