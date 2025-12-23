package com.example.warehouse_inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.warehouse_inventory.dto.CreateOrderItemRequest;
import com.example.warehouse_inventory.dto.CreateOrderRequest;
import com.example.warehouse_inventory.dto.OrderResponse;
import com.example.warehouse_inventory.entity.Order;
import com.example.warehouse_inventory.entity.OrderItem;
import com.example.warehouse_inventory.entity.OrderStatus;
import com.example.warehouse_inventory.entity.StockMovementType;
import com.example.warehouse_inventory.entity.Variant;
import com.example.warehouse_inventory.exception.DataAlreadyExistsException;
import com.example.warehouse_inventory.exception.NotFoundException;
import com.example.warehouse_inventory.mapper.OrderMapper;
import com.example.warehouse_inventory.repository.OrderItemRepository;
import com.example.warehouse_inventory.repository.OrderRepository;
import com.example.warehouse_inventory.repository.VariantRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VariantRepository variantRepository;
    private final StockMovementService stockMovementService;
    private final StockService stockService;

    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        if (req == null || req.items() == null || req.items().isEmpty()) {
            throw new DataAlreadyExistsException("Order items are required");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setStatus(OrderStatus.NEW);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        Map<Long, String> skuByVariantId = new LinkedHashMap<>();
        for (CreateOrderItemRequest itemReq : req.items()) {
            Variant variant = variantRepository.findById(itemReq.variantId())
                    .orElseThrow(() -> new NotFoundException("Variant not found"));
            int availableQty = stockService.getQuantityOrZero(variant.getId());
            if (itemReq.quantity() > availableQty) {
                throw new DataAlreadyExistsException("Stock is not enough for SKU " + variant.getSku());
            }
            BigDecimal price = variant.getPrice();
            int quantity = itemReq.quantity();
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(quantity)));
            skuByVariantId.putIfAbsent(variant.getId(), variant.getSku());

            OrderItem item = new OrderItem();
            item.setVariantId(variant.getId());
            item.setQuantity(quantity);
            item.setPriceAtPurchase(price);
            items.add(item);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : items) {
            item.setOrderId(savedOrder.getId());
        }
        List<OrderItem> savedItems = orderItemRepository.saveAll(items);
        return OrderMapper.toResponse(savedOrder, savedItems, skuByVariantId);
    }

    @Transactional
    public OrderResponse markPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        if (order.getStatus() == OrderStatus.PAID) {
            return OrderMapper.toResponse(order, items, loadSkuByVariantId(items));
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new DataAlreadyExistsException("Order already cancelled");
        }

        for (OrderItem item : items) {
            stockMovementService.create(
                    item.getVariantId(),
                    item.getQuantity(),
                    StockMovementType.OUT,
                    order.getOrderNo());
        }

        order.setStatus(OrderStatus.PAID);
        Order saved = orderRepository.save(order);
        return OrderMapper.toResponse(saved, items, loadSkuByVariantId(items));
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return OrderMapper.toResponse(order, items, loadSkuByVariantId(items));
        }

        if (order.getStatus() == OrderStatus.PAID) {
            for (OrderItem item : items) {
                stockMovementService.create(
                        item.getVariantId(),
                        item.getQuantity(),
                        StockMovementType.IN,
                        order.getOrderNo());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        return OrderMapper.toResponse(saved, items, loadSkuByVariantId(items));
    }

    private String generateOrderNo() {
        String base = "order_" + LocalDateTime.now().format(ORDER_NO_FORMAT);
        if (!orderRepository.existsByOrderNo(base)) {
            return base;
        }
        int counter = 1;
        String candidate = base + "_" + counter;
        while (orderRepository.existsByOrderNo(candidate)) {
            counter++;
            candidate = base + "_" + counter;
        }
        return candidate;
    }

    private Map<Long, String> loadSkuByVariantId(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        List<Long> variantIds = items.stream()
                .map(OrderItem::getVariantId)
                .distinct()
                .toList();
        return variantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(
                        Variant::getId,
                        Variant::getSku,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }
}
