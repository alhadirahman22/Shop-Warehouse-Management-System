package com.example.warehouse_inventory.util;

import com.example.warehouse_inventory.dto.FilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FilterParamParser {
    private static final Pattern FILTER_PARAM_PATTERN = Pattern
            .compile("^filters\\[(\\d+)]\\[(field|operator|value)]$");

    private FilterParamParser() {
    }

    public static List<FilterRequest> parse(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return List.of();
        }

        Map<Integer, FilterParts> byIndex = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Matcher matcher = FILTER_PARAM_PATTERN.matcher(entry.getKey());
            if (!matcher.matches()) {
                continue;
            }
            int index = Integer.parseInt(matcher.group(1));
            String key = matcher.group(2);
            FilterParts parts = byIndex.computeIfAbsent(index, i -> new FilterParts());
            switch (key) {
                case "field" -> parts.field = entry.getValue();
                case "operator" -> parts.operator = entry.getValue();
                case "value" -> parts.value = entry.getValue();
                default -> {
                }
            }
        }

        List<FilterRequest> filters = new ArrayList<>();
        for (FilterParts parts : byIndex.values()) {
            if (parts.field == null || parts.field.isBlank()) {
                continue;
            }
            filters.add(new FilterRequest(parts.field, parts.operator, parts.value));
        }
        return filters;
    }

    private static final class FilterParts {
        private String field;
        private String operator;
        private String value;
    }
}
