package com.example.warehouse_inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.warehouse_inventory.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM variants WHERE item_id = :itemId)", nativeQuery = true)
    boolean existsVariantByItemId(@Param("itemId") Long itemId);
}
