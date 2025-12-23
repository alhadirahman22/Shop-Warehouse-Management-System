package com.example.warehouse_inventory.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.warehouse_inventory.dto.CreateItemRequest;
import com.example.warehouse_inventory.dto.FilterRequest;
import com.example.warehouse_inventory.dto.ItemResponse;
import com.example.warehouse_inventory.dto.UpdateItemActiveRequest;
import com.example.warehouse_inventory.dto.UpdateItemRequest;
import com.example.warehouse_inventory.response.ApiResponse;
import com.example.warehouse_inventory.response.ApiStatus;
import com.example.warehouse_inventory.response.PaginatedResponse;
import com.example.warehouse_inventory.service.ItemService;
import com.example.warehouse_inventory.util.FilterParamParser;

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

import io.swagger.v3.oas.annotations.Parameter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ItemResponse>> create(@RequestBody @Valid CreateItemRequest req) {
        ItemResponse saved = itemService.create(req);

        return ResponseEntity.status(ApiStatus.CREATED.httpStatus()).body(ApiResponse.created(saved));
    }

    @GetMapping("/getList")
    public ResponseEntity<ApiResponse<PaginatedResponse<ItemResponse>>> getList(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(name = "sort_by", defaultValue = "id") String sortBy,
            @RequestParam(name = "sort_direction", defaultValue = "asc") String sortDirection,
            @Parameter(example = "{\"filters[0][field]\":\"active\",\"filters[0][operator]\":\"=\",\"filters[0][value]\":\"false\",\"filters[1][field]\":\"name\",\"filters[1][operator]\":\"!=\",\"filters[1][value]\":\"string\"}") @RequestParam(required = true) Map<String, String> params) {
        List<FilterRequest> filters = FilterParamParser.parse(params);
        PaginatedResponse<ItemResponse> result = itemService.getAll(offset, limit, search, sortBy, sortDirection,
                filters);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                .body(ApiResponse.success(result));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                .body(ApiResponse.withMessage(ApiStatus.SUCCESS, "Item deleted", null, null));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateItemRequest req) {
        ItemResponse updated = itemService.update(id, req);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                .body(ApiResponse.success(updated));
    }

    @PatchMapping("/update-active/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> updateActive(
            @PathVariable Long id,
            @RequestBody @Valid UpdateItemActiveRequest req) {
        ItemResponse updated = itemService.updateActive(id, req.active());
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus())
                .body(ApiResponse.success(updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> findById(@PathVariable Long id) {

        ItemResponse result = itemService.findById(id);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus()).body(ApiResponse.success(result));
    }
}
