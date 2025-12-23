package com.example.warehouse_inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.warehouse_inventory.entity.Stock;
import com.example.warehouse_inventory.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    @Transactional
    public void createForVariant(Long variantId) {
        if (stockRepository.existsById(variantId)) {
            return;
        }
        Stock stock = new Stock();
        stock.setVariantId(variantId);
        stock.setQuantity(0);
        stockRepository.save(stock);
    }

    @Transactional(readOnly = true)
    public int getQuantityOrZero(Long variantId) {
        return stockRepository.findById(variantId)
                .map(Stock::getQuantity)
                .orElse(0);
    }

    @Transactional
    public int applyChange(Long variantId, int changeQty) {
        Stock stock = stockRepository.findById(variantId)
                .orElseGet(() -> {
                    Stock created = new Stock();
                    created.setVariantId(variantId);
                    created.setQuantity(0);
                    return created;
                });
        int updatedQuantity = stock.getQuantity() + changeQty;
        stock.setQuantity(updatedQuantity);
        stockRepository.save(stock);
        return updatedQuantity;
    }

    @Transactional
    public void deleteByVariantId(Long variantId) {
        if (stockRepository.existsById(variantId)) {
            stockRepository.deleteById(variantId);
        }
    }
}
