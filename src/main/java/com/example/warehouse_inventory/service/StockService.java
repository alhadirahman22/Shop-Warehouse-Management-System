package com.example.warehouse_inventory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.warehouse_inventory.entity.Stock;
import com.example.warehouse_inventory.dto.FilterRequest;
import com.example.warehouse_inventory.dto.StockResponse;
import com.example.warehouse_inventory.mapper.StockMapper;
import com.example.warehouse_inventory.repository.StockRepository;
import com.example.warehouse_inventory.response.PaginatedResponse;
import com.example.warehouse_inventory.response.PaginationMeta;
import com.example.warehouse_inventory.util.FilterFieldResolver;
import com.example.warehouse_inventory.util.FilterPredicateBuilder;
import com.example.warehouse_inventory.util.FilterValueParser;
import com.example.warehouse_inventory.util.OffsetBasedPageRequest;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("id", "variantId"),
            Map.entry("variantid", "variantId"),
            Map.entry("variant_id", "variantId"),
            Map.entry("quantity", "quantity"),
            Map.entry("sku", "sku"),
            Map.entry("updatedat", "updatedAt"),
            Map.entry("updated_at", "updatedAt"));
    private static final Set<String> STRING_FIELDS = Set.of("sku");

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
    public PaginatedResponse<StockResponse> getAll(
            int offset,
            int limit,
            String search,
            String sortBy,
            String sortDirection,
            List<FilterRequest> filters) {
        String sortField = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        String normalizedSort = sortField.trim().toLowerCase();
        String safeSort = switch (normalizedSort) {
            case "id", "variantid", "variant_id" -> "variantId";
            case "quantity" -> "quantity";
            case "sku" -> "variant.sku";
            case "updatedat", "updated_at" -> "updatedAt";
            default -> "variantId";
        };
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection).orElse(Sort.Direction.ASC);
        Sort sort = Sort.by(direction, safeSort);

        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(offset, limit, sort);
        Specification<Stock> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                Join<Object, Object> variantJoin = root.join("variant", JoinType.LEFT);
                return cb.or(
                        cb.like(cb.lower(variantJoin.get("sku")), pattern),
                        cb.like(cb.lower(variantJoin.get("variantName")), pattern));
            });
        }
        spec = spec.and(buildFilterSpec(filters));
        Page<Stock> page = stockRepository.findAll(spec, pageable);

        List<StockResponse> stocks = page.getContent().stream()
                .map(StockMapper::toResponse)
                .toList();
        long total = page.getTotalElements();
        long count = stocks.size();
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
        return new PaginatedResponse<>(stocks, meta);
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

    private Specification<Stock> buildFilterSpec(List<FilterRequest> filters) {
        if (filters == null || filters.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (FilterRequest filter : filters) {
                if (filter == null || filter.field() == null) {
                    continue;
                }
                String field = FilterFieldResolver.resolve(FIELD_MAP, filter.field());
                if (field == null) {
                    continue;
                }
                String operator = (filter.operator() == null || filter.operator().isBlank())
                        ? "="
                        : filter.operator().trim().toLowerCase();
                String rawValue = filter.value();
                if (rawValue == null || rawValue.isBlank()) {
                    continue;
                }

                if ("sku".equals(field)) {
                    Join<Object, Object> variantJoin = root.join("variant", JoinType.LEFT);
                    String value = rawValue.trim();
                    switch (operator) {
                        case "=" -> predicates.add(cb.equal(variantJoin.get("sku"), value));
                        case "!=" -> predicates.add(cb.notEqual(variantJoin.get("sku"), value));
                        case "contains" -> predicates.add(cb.like(cb.lower(variantJoin.get("sku")),
                                "%" + value.toLowerCase() + "%"));
                        default -> {
                        }
                    }
                    continue;
                }

                switch (operator) {
                    case "=" -> {
                        Object value = convertValue(field, rawValue);
                        if (value != null) {
                            predicates.add(cb.equal(root.get(field), value));
                        }
                    }
                    case "!=" -> {
                        Object value = convertValue(field, rawValue);
                        if (value != null) {
                            predicates.add(cb.notEqual(root.get(field), value));
                        }
                    }
                    case ">", "<", ">=", "<=" -> FilterPredicateBuilder.addComparablePredicate(
                            predicates,
                            root.get(field),
                            operator,
                            rawValue,
                            cb,
                            value -> convertValue(field, value));
                    case "contains" -> {
                        if (STRING_FIELDS.contains(field)) {
                            String pattern = "%" + rawValue.toLowerCase() + "%";
                            predicates.add(cb.like(cb.lower(root.get(field)), pattern));
                        }
                    }
                    case "in" -> {
                        List<Object> values = FilterValueParser.parseValues(
                                rawValue,
                                value -> convertValue(field, value));
                        if (!values.isEmpty()) {
                            predicates.add(root.get(field).in(values));
                        }
                    }
                    case "between" -> {
                        List<Object> values = FilterValueParser.parseValues(
                                rawValue,
                                value -> convertValue(field, value));
                        if (values.size() == 2
                                && values.get(0) instanceof Comparable
                                && values.get(1) instanceof Comparable) {
                            predicates.add(cb.between(
                                    root.get(field).as(Comparable.class),
                                    (Comparable) values.get(0),
                                    (Comparable) values.get(1)));
                        }
                    }
                    default -> {
                    }
                }
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Object convertValue(String field, String rawValue) {
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return switch (field) {
                case "variantId" -> Long.parseLong(trimmed);
                case "quantity" -> Integer.parseInt(trimmed);
                case "updatedAt" -> Instant.parse(trimmed);
                default -> trimmed;
            };
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
