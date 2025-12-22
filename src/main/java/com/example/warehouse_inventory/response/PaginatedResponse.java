package com.example.warehouse_inventory.response;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> data,
        PaginationMeta pagination
) {
}
