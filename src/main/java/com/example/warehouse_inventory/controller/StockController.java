package com.example.warehouse_inventory.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.warehouse_inventory.dto.FilterRequest;
import com.example.warehouse_inventory.dto.StockResponse;
import com.example.warehouse_inventory.response.ApiResponse;
import com.example.warehouse_inventory.response.ApiStatus;
import com.example.warehouse_inventory.response.PaginatedResponse;
import com.example.warehouse_inventory.service.StockService;
import com.example.warehouse_inventory.util.FilterParamParser;
import io.swagger.v3.oas.annotations.Parameter;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@RequestMapping("/stock")
@RestController
@RequiredArgsConstructor
public class StockController {
        private final StockService stockService;

        @GetMapping("/getList")
        public ResponseEntity<ApiResponse<PaginatedResponse<StockResponse>>> getList(
                        @RequestParam(defaultValue = "0") int offset,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam(required = false) String search,
                        @RequestParam(name = "sort_by", defaultValue = "id") String sortBy,
                        @RequestParam(name = "sort_direction", defaultValue = "asc") String sortDirection,
                        @Parameter(example = "{\"filters[0][field]\":\"sku\",\"filters[0][operator]\":\"=\",\"filters[0][value]\":\"SKU-123\",\"filters[1][field]\":\"variant_id\",\"filters[1][operator]\":\"=\",\"filters[1][value]\":\"10\"}") @RequestParam(required = true) Map<String, String> params) {
                List<FilterRequest> filters = FilterParamParser.parse(params);
                PaginatedResponse<StockResponse> result = stockService.getAll(offset, limit, search, sortBy,
                                sortDirection,
                                filters);
                return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                                .body(ApiResponse.success(result));
        }
}
