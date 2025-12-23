package com.example.warehouse_inventory.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.warehouse_inventory.dto.CreateVariantRequest;
import com.example.warehouse_inventory.dto.FilterRequest;
import com.example.warehouse_inventory.dto.UpdateVariantActiveRequest;
import com.example.warehouse_inventory.dto.UpdateVariantRequest;
import com.example.warehouse_inventory.dto.VariantResponse;
import com.example.warehouse_inventory.response.ApiResponse;
import com.example.warehouse_inventory.response.ApiStatus;
import com.example.warehouse_inventory.response.PaginatedResponse;
import com.example.warehouse_inventory.service.VariantService;
import com.example.warehouse_inventory.util.FilterParamParser;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@RequestMapping("/variant")
@RestController
@RequiredArgsConstructor
public class VariantController {
    private final VariantService variantService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<VariantResponse>> create(@RequestBody @Valid CreateVariantRequest req) {
        VariantResponse saved = variantService.create(req);
        return ResponseEntity.status(ApiStatus.CREATED.httpStatus()).body(ApiResponse.created(saved));
    }

    @GetMapping("/getList")
    public ResponseEntity<ApiResponse<PaginatedResponse<VariantResponse>>> getList(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(name = "sort_by", defaultValue = "id") String sortBy,
            @RequestParam(name = "sort_direction", defaultValue = "asc") String sortDirection,
            @Parameter(example = "{\"filters[0][field]\":\"attributes\",\"filters[0][operator]\":\"=\",\"filters[0][value]\":\"{\\\"brand\\\":\\\"Belden\\\",\\\"length\\\":\\\"10m\\\"}\"}") @RequestParam(required = true) Map<String, String> params) {
        List<FilterRequest> filters = FilterParamParser.parse(params);
        PaginatedResponse<VariantResponse> result = variantService.getAll(offset, limit, search, sortBy, sortDirection,
                filters);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                .body(ApiResponse.success(result));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        variantService.delete(id);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                .body(ApiResponse.withMessage(ApiStatus.SUCCESS, "Variant deleted", null, null));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<VariantResponse>> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateVariantRequest req) {
        VariantResponse updated = variantService.update(id, req);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                .body(ApiResponse.success(updated));
    }

    @PatchMapping("/update-active/{id}")
    public ResponseEntity<ApiResponse<VariantResponse>> updateActive(
            @PathVariable Long id,
            @RequestBody @Valid UpdateVariantActiveRequest req) {
        VariantResponse updated = variantService.updateActive(id, req.active());
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                .body(ApiResponse.success(updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VariantResponse>> findById(@PathVariable Long id) {
        VariantResponse result = variantService.findById(id);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                .body(ApiResponse.success(result));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<VariantResponse>> findBySku(@PathVariable String sku) {
        VariantResponse result = variantService.findBySku(sku);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                .body(ApiResponse.success(result));
    }
}
