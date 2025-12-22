package com.example.warehouse_inventory.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
// import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
// @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant updatedAt;
}
