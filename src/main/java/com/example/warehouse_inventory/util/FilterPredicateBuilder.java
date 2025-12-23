package com.example.warehouse_inventory.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.List;
import java.util.function.Function;

public final class FilterPredicateBuilder {
    private FilterPredicateBuilder() {
    }

    public static void addComparablePredicate(
            List<Predicate> predicates,
            Path<?> path,
            String operator,
            String rawValue,
            CriteriaBuilder cb,
            Function<String, Object> converter) {
        if (rawValue == null || rawValue.isBlank()) {
            return;
        }
        Object value = converter.apply(rawValue);
        if (!(value instanceof Comparable)) {
            return;
        }
        Comparable comparableValue = (Comparable) value;
        Predicate predicate = switch (operator) {
            case ">" -> cb.greaterThan(path.as(Comparable.class), comparableValue);
            case ">=" -> cb.greaterThanOrEqualTo(path.as(Comparable.class), comparableValue);
            case "<" -> cb.lessThan(path.as(Comparable.class), comparableValue);
            case "<=" -> cb.lessThanOrEqualTo(path.as(Comparable.class), comparableValue);
            default -> null;
        };
        if (predicate != null) {
            predicates.add(predicate);
        }
    }
}
