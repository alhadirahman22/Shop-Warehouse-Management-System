package com.example.warehouse_inventory.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.warehouse_inventory.dto.StockMovementResponse;
import com.example.warehouse_inventory.response.ApiResponse;
import com.example.warehouse_inventory.response.ApiStatus;
import com.example.warehouse_inventory.response.PaginatedResponse;
import com.example.warehouse_inventory.service.StockMovementService;
import io.swagger.v3.oas.annotations.Parameter;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/stock-movements")
@RestController
@RequiredArgsConstructor
public class StockMovementController {
        private final StockMovementService stockMovementService;

        @GetMapping("/getList")
        public ResponseEntity<ApiResponse<PaginatedResponse<StockMovementResponse>>> getList(
                        @RequestParam(defaultValue = "0") int offset,
                        @RequestParam(defaultValue = "10") int limit,
                        @Parameter(description = "fill sku or order id", example = "SKU-123") @RequestParam(required = false) String search,
                        @RequestParam(name = "sort_by", defaultValue = "id") String sortBy,
                        @RequestParam(name = "sort_direction", defaultValue = "desc") String sortDirection) {
                PaginatedResponse<StockMovementResponse> result = stockMovementService.getAll(offset, limit, search,
                                sortBy,
                                sortDirection);
                return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                                .body(ApiResponse.success(result));
        }
}
