package com.example.warehouse_inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.warehouse_inventory.dto.CreateVariantRequest;
import com.example.warehouse_inventory.dto.UpdateVariantRequest;
import com.example.warehouse_inventory.dto.VariantResponse;
import com.example.warehouse_inventory.entity.Variant;
import com.example.warehouse_inventory.exception.DataAlreadyExistsException;
import com.example.warehouse_inventory.exception.NotFoundException;
import com.example.warehouse_inventory.mapper.VariantMapper;
import com.example.warehouse_inventory.repository.ItemRepository;
import com.example.warehouse_inventory.repository.VariantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VariantService {
    private final VariantRepository variantRepository;
    private final ItemRepository itemRepository;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        return VariantMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!variantRepository.existsById(id)) {
            throw new NotFoundException("Variant not found");
        }
        if (variantRepository.existsStockByVariantId(id) > 0) {
            throw new DataAlreadyExistsException("Variant has stock");
        }
        if (variantRepository.existsStockMovementByVariantId(id) > 0) {
            throw new DataAlreadyExistsException("Variant has stock movements");
        }
        if (variantRepository.existsOrderItemByVariantId(id) > 0) {
            throw new DataAlreadyExistsException("Variant has order items");
        }
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
}
