package com.example.warehouse_inventory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.warehouse_inventory.entity.Variant;

import java.util.Optional;

public interface VariantRepository extends JpaRepository<Variant, Long>, JpaSpecificationExecutor<Variant> {
    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    Optional<Variant> findBySkuIgnoreCase(String sku);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM stock WHERE variant_id = :variantId)", nativeQuery = true)
    long existsStockByVariantId(@Param("variantId") Long variantId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM stock_movements WHERE variant_id = :variantId)", nativeQuery = true)
    long existsStockMovementByVariantId(@Param("variantId") Long variantId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM order_items WHERE variant_id = :variantId)", nativeQuery = true)
    long existsOrderItemByVariantId(@Param("variantId") Long variantId);

    Page<Variant> findBySkuContainingIgnoreCaseOrVariantNameContainingIgnoreCase(
            String sku,
            String variantName,
            Pageable pageable);
}
