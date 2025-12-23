package com.example.warehouse_inventory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.warehouse_inventory.dto.CreateItemRequest;
import com.example.warehouse_inventory.dto.ItemResponse;
import com.example.warehouse_inventory.dto.UpdateItemRequest;
import com.example.warehouse_inventory.entity.Item;
import com.example.warehouse_inventory.exception.DataAlreadyExistsException;
import com.example.warehouse_inventory.exception.NotFoundException;
import com.example.warehouse_inventory.mapper.ItemMapper;
import com.example.warehouse_inventory.repository.ItemRepository;
import com.example.warehouse_inventory.response.PaginatedResponse;
import com.example.warehouse_inventory.response.PaginationMeta;
import com.example.warehouse_inventory.util.OffsetBasedPageRequest;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    // public ItemService(ItemRepository itemRepository) {
    // this.itemRepository = itemRepository;
    // }

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
        if (itemRepository.existsVariantByItemId(id)) {
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
            String sortDirection) {
        String sortField = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        String safeSort = switch (sortField) {
            case "id", "name", "description", "active", "createdAt", "updatedAt" -> sortField;
            default -> "id";
        };
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection).orElse(Sort.Direction.ASC);
        Sort sort = Sort.by(direction, safeSort);

        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(offset, limit, sort);
        Page<Item> page;
        if (search == null || search.isBlank()) {
            page = itemRepository.findAll(pageable);
        } else {
            page = itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    search,
                    search,
                    pageable);
        }

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
}
