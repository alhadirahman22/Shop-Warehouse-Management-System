package com.example.warehouse_inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stock")
public class Stock {
    @Id
    @Column(name = "variant_id")
    private Long variantId;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "updated_at", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant updatedAt;
}
