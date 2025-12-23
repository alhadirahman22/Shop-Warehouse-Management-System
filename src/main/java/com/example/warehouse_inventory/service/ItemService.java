package com.example.warehouse_inventory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.warehouse_inventory.dto.CreateItemRequest;
import com.example.warehouse_inventory.dto.FilterRequest;
import com.example.warehouse_inventory.dto.ItemResponse;
import com.example.warehouse_inventory.dto.UpdateItemRequest;
import com.example.warehouse_inventory.entity.Item;
import com.example.warehouse_inventory.exception.DataAlreadyExistsException;
import com.example.warehouse_inventory.exception.NotFoundException;
import com.example.warehouse_inventory.mapper.ItemMapper;
import com.example.warehouse_inventory.repository.ItemRepository;
import com.example.warehouse_inventory.response.PaginatedResponse;
import com.example.warehouse_inventory.response.PaginationMeta;
import com.example.warehouse_inventory.util.FilterFieldResolver;
import com.example.warehouse_inventory.util.FilterPredicateBuilder;
import com.example.warehouse_inventory.util.FilterValueParser;
import com.example.warehouse_inventory.util.OffsetBasedPageRequest;

import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    // public ItemService(ItemRepository itemRepository) {
    // this.itemRepository = itemRepository;
    // }

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "id",
            "name", "name",
            "description", "description",
            "active", "active",
            "createdat", "createdAt",
            "created_at", "createdAt",
            "updatedat", "updatedAt",
            "updated_at", "updatedAt");
    private static final Set<String> STRING_FIELDS = Set.of("name", "description");

    @Transactional
    public ItemResponse create(CreateItemRequest req) {
        if (itemRepository.existsByNameIgnoreCase(req.name())) {
            throw new DataAlreadyExistsException("Item name already exists");
        }

        Item item = ItemMapper.toEntity(req);
        if (item.getActive() == null) {
            item.setActive(true);
        }

        Item saved = itemRepository.save(item);
        return ItemMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new NotFoundException("Item not found");
        }
        if (itemRepository.existsVariantByItemId(id) > 0) {
            throw new DataAlreadyExistsException("Item has variants");
        }
        itemRepository.deleteById(id);
    }

    @Transactional
    public ItemResponse update(Long id, UpdateItemRequest req) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (itemRepository.existsByNameIgnoreCaseAndIdNot(req.name(), id)) {
            throw new DataAlreadyExistsException("Item name already exists");
        }

        item.setName(req.name());
        item.setDescription(req.description());
        item.setActive(req.active());

        Item saved = itemRepository.save(item);
        return ItemMapper.toResponse(saved);
    }

    @Transactional
    public ItemResponse updateActive(Long id, Boolean active) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        item.setActive(active);
        Item saved = itemRepository.save(item);
        return ItemMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ItemResponse> getAll(
            int offset,
            int limit,
            String search,
            String sortBy,
            String sortDirection,
            List<FilterRequest> filters) {
        String sortField = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        String safeSort = switch (sortField) {
            case "id", "name", "description", "active", "createdAt", "updatedAt" -> sortField;
            default -> "id";
        };
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection).orElse(Sort.Direction.ASC);
        Sort sort = Sort.by(direction, safeSort);

        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(offset, limit, sort);
        Specification<Item> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.isBlank()) {
            String lowerPattern = "%" + search.toLowerCase() + "%";
            String rawPattern = "%" + search + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    // cb.like(cb.lower(root.get("name")), lowerPattern)
                    // // cb.like(cb.lower(root.get("description")), pattern)));
                    // cb.like(root.get("description"), rawPattern)));
                    cb.like(cb.lower(root.get("name")), lowerPattern)));
        }
        spec = spec.and(buildFilterSpec(filters));
        Page<Item> page = itemRepository.findAll(spec, pageable);

        List<ItemResponse> items = page.getContent().stream()
                .map(ItemMapper::toResponse)
                .toList();
        long total = page.getTotalElements();
        long count = items.size();
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
        return new PaginatedResponse<>(items, meta);
    }

    @Transactional(readOnly = true)
    public ItemResponse findById(Long id) {
        Item result = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Item not found"));
        return ItemMapper.toResponse(result);
    }

    private Specification<Item> buildFilterSpec(List<FilterRequest> filters) {
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
                case "id" -> Long.parseLong(trimmed);
                case "active" -> FilterValueParser.parseBoolean(trimmed);
                case "createdAt", "updatedAt" -> Instant.parse(trimmed);
                default -> trimmed;
            };
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
