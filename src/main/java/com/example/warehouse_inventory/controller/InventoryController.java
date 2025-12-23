package com.example.warehouse_inventory.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.warehouse_inventory.dto.StockAdjustRequest;
import com.example.warehouse_inventory.dto.StockInRequest;
import com.example.warehouse_inventory.dto.StockMovementResponse;
import com.example.warehouse_inventory.dto.StockOutRequest;
import com.example.warehouse_inventory.response.ApiResponse;
import com.example.warehouse_inventory.response.ApiStatus;
import com.example.warehouse_inventory.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequestMapping("/inventory")
@RestController
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/in")
    public ResponseEntity<ApiResponse<StockMovementResponse>> stockIn(@RequestBody @Valid StockInRequest req) {
        StockMovementResponse movement = inventoryService.stockIn(req);
        return ResponseEntity.status(ApiStatus.CREATED.httpStatus())
                .body(ApiResponse.created(movement));
    }

    @PostMapping("/out")
    public ResponseEntity<ApiResponse<StockMovementResponse>> stockOut(@RequestBody @Valid StockOutRequest req) {
        StockMovementResponse movement = inventoryService.stockOut(req);
        return ResponseEntity.status(ApiStatus.CREATED.httpStatus())
                .body(ApiResponse.created(movement));
    }

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<StockMovementResponse>> stockAdjust(@RequestBody @Valid StockAdjustRequest req) {
        StockMovementResponse movement = inventoryService.stockAdjust(req);
        return ResponseEntity.status(ApiStatus.CREATED.httpStatus())
                .body(ApiResponse.created(movement));
    }
}
