package com.example.warehouse_inventory.util;

import java.util.Map;

public final class FilterFieldResolver {
    private FilterFieldResolver() {
    }

    public static String resolve(Map<String, String> fieldMap, String field) {
        if (field == null) {
            return null;
        }
        String normalized = field.trim().toLowerCase();
        return fieldMap.get(normalized);
    }
}
