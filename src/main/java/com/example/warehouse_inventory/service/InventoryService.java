package com.example.warehouse_inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.warehouse_inventory.dto.StockAdjustRequest;
import com.example.warehouse_inventory.dto.StockInRequest;
import com.example.warehouse_inventory.dto.StockMovementResponse;
import com.example.warehouse_inventory.dto.StockOutRequest;
import com.example.warehouse_inventory.entity.StockMovement;
import com.example.warehouse_inventory.entity.StockMovementType;
import com.example.warehouse_inventory.mapper.StockMovementMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final StockMovementService stockMovementService;

    @Transactional
    public StockMovementResponse stockIn(StockInRequest req) {
        StockMovement movement = stockMovementService.create(
                req.variantId(),
                req.quantity(),
                StockMovementType.IN,
                req.referenceId());
        return StockMovementMapper.toResponse(movement);
    }

    @Transactional
    public StockMovementResponse stockOut(StockOutRequest req) {
        StockMovement movement = stockMovementService.create(
                req.variantId(),
                req.quantity(),
                StockMovementType.OUT,
                req.referenceId());
        return StockMovementMapper.toResponse(movement);
    }

    @Transactional
    public StockMovementResponse stockAdjust(StockAdjustRequest req) {
        StockMovement movement = stockMovementService.create(
                req.variantId(),
                req.changeQty(),
                StockMovementType.ADJUST,
                req.referenceId());
        return StockMovementMapper.toResponse(movement);
    }
}
