package com.example.warehouse_inventory.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class FilterValueParser {
    private FilterValueParser() {
    }

    public static List<Object> parseValues(String rawValue, Function<String, Object> converter) {
        if (rawValue == null || rawValue.isBlank()) {
            return List.of();
        }
        return Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(converter)
                .filter(Objects::nonNull)
                .toList();
    }

    public static Boolean parseBoolean(String value) {
        if ("1".equals(value)) {
            return true;
        }
        if ("0".equals(value)) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
