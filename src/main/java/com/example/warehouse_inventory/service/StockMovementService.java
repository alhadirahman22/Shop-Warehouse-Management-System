package com.example.warehouse_inventory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.warehouse_inventory.dto.StockMovementResponse;
import com.example.warehouse_inventory.entity.StockMovement;
import com.example.warehouse_inventory.entity.StockMovementType;
import com.example.warehouse_inventory.exception.DataAlreadyExistsException;
import com.example.warehouse_inventory.exception.NotFoundException;
import com.example.warehouse_inventory.mapper.StockMovementMapper;
import com.example.warehouse_inventory.repository.StockMovementRepository;
import com.example.warehouse_inventory.repository.VariantRepository;
import com.example.warehouse_inventory.response.PaginatedResponse;
import com.example.warehouse_inventory.response.PaginationMeta;
import com.example.warehouse_inventory.util.OffsetBasedPageRequest;

import lombok.RequiredArgsConstructor;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import java.util.List;

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

    @Transactional(readOnly = true)
    public PaginatedResponse<StockMovementResponse> getAll(
            int offset,
            int limit,
            String search,
            String sortBy,
            String sortDirection) {
        String sortField = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        String normalizedSort = sortField.trim().toLowerCase();
        String safeSort = switch (normalizedSort) {
            case "id" -> "id";
            case "variantid", "variant_id" -> "variantId";
            case "movementtype", "movement_type" -> "movementType";
            case "changeqty", "change_qty" -> "changeQty";
            case "referenceid", "reference_id" -> "referenceId";
            case "createdat", "created_at" -> "createdAt";
            default -> "id";
        };
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection).orElse(Sort.Direction.DESC);
        Sort sort = Sort.by(direction, safeSort);

        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(offset, limit, sort);
        Specification<StockMovement> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                Join<Object, Object> variantJoin = root.join("variant", JoinType.LEFT);
                return cb.or(
                        cb.like(cb.lower(variantJoin.get("sku")), pattern),
                        cb.like(cb.lower(root.get("referenceId")), pattern));
            });
        }
        Page<StockMovement> page = stockMovementRepository.findAll(spec, pageable);

        List<StockMovementResponse> movements = page.getContent().stream()
                .map(StockMovementMapper::toResponse)
                .toList();
        long total = page.getTotalElements();
        long count = movements.size();
        long showingFrom = count > 0 ? (long) offset + 1 : 0;
        long showingTo = (long) offset + count;

        PaginationMeta meta = new PaginationMeta(
                offset,
                limit,
                total,
                (offset + count) < total,
                offset > 0,
                offset,
                showingFrom,
                showingTo);
        return new PaginatedResponse<>(movements, meta);
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
