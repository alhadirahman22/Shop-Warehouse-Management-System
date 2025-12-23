package com.example.warehouse_inventory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.warehouse_inventory.dto.CreateVariantRequest;
import com.example.warehouse_inventory.dto.FilterRequest;
import com.example.warehouse_inventory.dto.UpdateVariantRequest;
import com.example.warehouse_inventory.dto.VariantResponse;
import com.example.warehouse_inventory.entity.Variant;
import com.example.warehouse_inventory.exception.DataAlreadyExistsException;
import com.example.warehouse_inventory.exception.NotFoundException;
import com.example.warehouse_inventory.mapper.VariantMapper;
import com.example.warehouse_inventory.repository.ItemRepository;
import com.example.warehouse_inventory.repository.VariantRepository;
import com.example.warehouse_inventory.response.PaginatedResponse;
import com.example.warehouse_inventory.response.PaginationMeta;
import com.example.warehouse_inventory.util.FilterFieldResolver;
import com.example.warehouse_inventory.util.FilterPredicateBuilder;
import com.example.warehouse_inventory.util.FilterValueParser;
import com.example.warehouse_inventory.util.OffsetBasedPageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VariantService {
    private final VariantRepository variantRepository;
    private final ItemRepository itemRepository;
    private final StockService stockService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("itemid", "itemId"),
            Map.entry("item_id", "itemId"),
            Map.entry("sku", "sku"),
            Map.entry("variantname", "variantName"),
            Map.entry("variant_name", "variantName"),
            Map.entry("price", "price"),
            Map.entry("active", "active"),
            Map.entry("createdat", "createdAt"),
            Map.entry("created_at", "createdAt"),
            Map.entry("updatedat", "updatedAt"),
            Map.entry("updated_at", "updatedAt"),
            Map.entry("attributes", "attributes"));
    private static final Set<String> STRING_FIELDS = Set.of("sku", "variantName");

    @Transactional
    public VariantResponse create(CreateVariantRequest req) {
        if (!itemRepository.existsById(req.itemId())) {
            throw new NotFoundException("Item not found");
        }

        Variant variant = VariantMapper.toEntity(req);
        variant.setSku(generateSku());
        variant.setAttributes(sanitizeAttributes(req.attributes()));
        if (variant.getActive() == null) {
            variant.setActive(true);
        }

        Variant saved = variantRepository.save(variant);
        stockService.createForVariant(saved.getId());
        return VariantMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<VariantResponse> getAll(
            int offset,
            int limit,
            String search,
            String sortBy,
            String sortDirection,
            List<FilterRequest> filters) {
        String sortField = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        String normalizedSort = sortField.trim().toLowerCase();
        String safeSort = switch (normalizedSort) {
            case "id" -> "id";
            case "sku" -> "sku";
            case "variantname", "variant_name" -> "variantName";
            case "itemid", "item_id" -> "itemId";
            case "price" -> "price";
            case "active" -> "active";
            case "createdat", "created_at" -> "createdAt";
            case "updatedat", "updated_at" -> "updatedAt";
            default -> "id";
        };
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection).orElse(Sort.Direction.ASC);
        Sort sort = Sort.by(direction, safeSort);

        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(offset, limit, sort);
        Specification<Variant> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("sku")), pattern),
                    cb.like(cb.lower(root.get("variantName")), pattern)));
        }
        spec = spec.and(buildFilterSpec(filters));
        Page<Variant> page = variantRepository.findAll(spec, pageable);

        List<VariantResponse> variants = page.getContent().stream()
                .map(VariantMapper::toResponse)
                .toList();
        long total = page.getTotalElements();
        long count = variants.size();
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
        return new PaginatedResponse<>(variants, meta);
    }

    @Transactional
    public void delete(Long id) {
        if (!variantRepository.existsById(id)) {
            throw new NotFoundException("Variant not found");
        }
        int quantity = stockService.getQuantityOrZero(id);
        if (quantity > 0) {
            throw new DataAlreadyExistsException("Variant has stock");
        }
        if (variantRepository.existsStockMovementByVariantId(id) > 0) {
            throw new DataAlreadyExistsException("Variant has stock movements");
        }
        if (variantRepository.existsOrderItemByVariantId(id) > 0) {
            throw new DataAlreadyExistsException("Variant has order items");
        }
        stockService.deleteByVariantId(id);
        variantRepository.deleteById(id);
    }

    @Transactional
    public VariantResponse update(Long id, UpdateVariantRequest req) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Variant not found"));

        variant.setVariantName(req.variantName());
        variant.setAttributes(sanitizeAttributes(req.attributes()));
        variant.setPrice(req.price());
        variant.setActive(req.active());

        Variant saved = variantRepository.save(variant);
        return VariantMapper.toResponse(saved);
    }

    @Transactional
    public VariantResponse updateActive(Long id, Boolean active) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Variant not found"));
        variant.setActive(active);
        Variant saved = variantRepository.save(variant);
        return VariantMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VariantResponse findById(Long id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Variant not found"));
        return VariantMapper.toResponse(variant);
    }

    @Transactional(readOnly = true)
    public VariantResponse findBySku(String sku) {
        Variant variant = variantRepository.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new NotFoundException("Variant not found"));
        return VariantMapper.toResponse(variant);
    }

    private Specification<Variant> buildFilterSpec(List<FilterRequest> filters) {
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

                if ("attributes".equals(field)) {
                    addAttributePredicates(predicates, root.get("attributes"), operator, rawValue, cb);
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

    private String generateSku() {
        String sku = UUID.randomUUID().toString();
        while (variantRepository.existsBySkuIgnoreCase(sku)) {
            sku = UUID.randomUUID().toString();
        }
        return sku;
    }

    private String sanitizeAttributes(String rawAttributes) {
        if (rawAttributes == null || rawAttributes.isBlank()) {
            return null;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(rawAttributes);
            if (!node.isObject()) {
                return null;
            }
            ObjectNode filtered = OBJECT_MAPPER.createObjectNode();
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(name -> {
                String normalized = name.trim().toLowerCase();
                if (Variant.ATTRIBUTE_KEYS.contains(normalized)) {
                    filtered.set(normalized, objectNode.get(name));
                }
            });
            return filtered.size() == 0 ? null : OBJECT_MAPPER.writeValueAsString(filtered);
        } catch (Exception ex) {
            return null;
        }
    }

    private void addAttributePredicates(
            List<Predicate> predicates,
            jakarta.persistence.criteria.Path<String> attributesPath,
            String operator,
            String rawValue,
            jakarta.persistence.criteria.CriteriaBuilder cb) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(rawValue);
            if (!node.isObject()) {
                return;
            }
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(name -> {
                String normalized = name.trim().toLowerCase();
                if (!Variant.ATTRIBUTE_KEYS.contains(normalized)) {
                    return;
                }
                JsonNode valueNode = objectNode.get(name);
                if (valueNode == null || !valueNode.isValueNode()) {
                    return;
                }
                String valueText = valueNode.asText();
                Predicate predicate = switch (operator) {
                    case "=" -> cb.equal(
                            cb.function(
                                    "json_unquote",
                                    String.class,
                                    cb.function(
                                            "json_extract",
                                            String.class,
                                            attributesPath,
                                            cb.literal("$." + normalized))),
                            valueText);
                    case "!=" -> cb.notEqual(
                            cb.function(
                                    "json_unquote",
                                    String.class,
                                    cb.function(
                                            "json_extract",
                                            String.class,
                                            attributesPath,
                                            cb.literal("$." + normalized))),
                            valueText);
                    case "contains" -> cb.like(
                            cb.lower(cb.function(
                                    "json_unquote",
                                    String.class,
                                    cb.function(
                                            "json_extract",
                                            String.class,
                                            attributesPath,
                                            cb.literal("$." + normalized)))),
                            "%" + valueText.toLowerCase() + "%");
                    default -> null;
                };
                if (predicate != null) {
                    predicates.add(predicate);
                }
            });
        } catch (Exception ex) {
            return;
        }
    }

    private Object convertValue(String field, String rawValue) {
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return switch (field) {
                case "id", "itemId" -> Long.parseLong(trimmed);
                case "price" -> new BigDecimal(trimmed);
                case "active" -> FilterValueParser.parseBoolean(trimmed);
                case "createdAt", "updatedAt" -> Instant.parse(trimmed);
                default -> trimmed;
            };
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
