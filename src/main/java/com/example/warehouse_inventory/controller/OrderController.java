package com.example.warehouse_inventory.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.warehouse_inventory.dto.CreateOrderRequest;
import com.example.warehouse_inventory.dto.OrderResponse;
import com.example.warehouse_inventory.response.ApiResponse;
import com.example.warehouse_inventory.response.ApiStatus;
import com.example.warehouse_inventory.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequestMapping("/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrderResponse>> create(@RequestBody @Valid CreateOrderRequest req) {
        OrderResponse created = orderService.create(req);
        return ResponseEntity.status(ApiStatus.CREATED.httpStatus()).body(ApiResponse.created(created));
    }

    @PatchMapping("/pay/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> markPaid(@PathVariable Long id) {
        OrderResponse updated = orderService.markPaid(id);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus()).body(ApiResponse.success(updated));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable Long id) {
        OrderResponse updated = orderService.cancel(id);
        return ResponseEntity.status(ApiStatus.SUCCESS.httpStatus()).body(ApiResponse.success(updated));
    }
}
