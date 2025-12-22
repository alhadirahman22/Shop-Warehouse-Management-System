package com.example.warehouse_inventory.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PaginationMeta(
        long offset,
        int limit,
        long total,
        boolean hasNext,
        boolean hasPrevious,
        long currentOffset,
        long showingFrom,
        long showingTo
) {
}
