package com.example.warehouse_inventory.service;

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
}
