package com.example.warehouse_inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.warehouse_inventory.entity.StockMovement;
import com.example.warehouse_inventory.entity.StockMovementType;
import com.example.warehouse_inventory.exception.DataAlreadyExistsException;
import com.example.warehouse_inventory.exception.NotFoundException;
import com.example.warehouse_inventory.repository.StockMovementRepository;
import com.example.warehouse_inventory.repository.VariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockMovementService {
    private final StockMovementRepository stockMovementRepository;
    private final VariantRepository variantRepository;
    private final StockService stockService;

    @Transactional
    public StockMovement create(Long variantId, int changeQty, StockMovementType movementType, String referenceId) {
        if (!variantRepository.existsById(variantId)) {
            throw new NotFoundException("Variant not found");
        }
        int normalizedQty = normalizeChangeQty(changeQty, movementType);
        int currentQty = stockService.getQuantityOrZero(variantId);
        if (currentQty + normalizedQty < 0) {
            throw new DataAlreadyExistsException("Stock is not enough");
        }
        stockService.applyChange(variantId, normalizedQty);

        StockMovement movement = new StockMovement();
        movement.setVariantId(variantId);
        movement.setChangeQty(normalizedQty);
        movement.setMovementType(movementType);
        movement.setReferenceId(referenceId);
        return stockMovementRepository.save(movement);
    }

    private int normalizeChangeQty(int changeQty, StockMovementType movementType) {
        int absQty = Math.abs(changeQty);
        return switch (movementType) {
            case IN -> absQty;
            case OUT -> -absQty;
            case ADJUST -> changeQty;
        };
    }
}
