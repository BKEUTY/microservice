package com.bkeuty.order.util;

import org.springframework.data.domain.Sort;

public final class OrderSortUtils {
    private OrderSortUtils() {
    }

    private static final java.util.Set<String> ALLOWED_FIELDS = java.util.Set.of("id", "orderDate", "total", "status");

    public static Sort parseSort(String[] sort) {
        Sort defaultSort = Sort.by(Sort.Direction.ASC, "id");
        if (sort == null || sort.length == 0 || sort[0] == null || sort[0].isBlank()) {
            return defaultSort;
        }

        String firstValue = sort[0].trim();
        if (firstValue.isBlank()) {
            return defaultSort;
        }

        switch (firstValue) {
            case "default", "id_asc" -> { return Sort.by(Sort.Direction.ASC, "id"); }
            case "id_desc" -> { return Sort.by(Sort.Direction.DESC, "id"); }
            case "date_asc" -> { return Sort.by(Sort.Direction.ASC, "orderDate"); }
            case "date_desc" -> { return Sort.by(Sort.Direction.DESC, "orderDate"); }
            case "total_asc" -> { return Sort.by(Sort.Direction.ASC, "total"); }
            case "total_desc" -> { return Sort.by(Sort.Direction.DESC, "total"); }
        }

        int delimiterIndex = firstValue.indexOf(',');
        if (delimiterIndex < 0) {
            delimiterIndex = firstValue.lastIndexOf('_');
        }

        if (delimiterIndex >= 0) {
            String parsedField = firstValue.substring(0, delimiterIndex).trim();
            String parsedDirection = firstValue.substring(delimiterIndex + 1).trim();
            
            String field = mapField(parsedField);
            if (!ALLOWED_FIELDS.contains(field)) {
                return defaultSort;
            }
            Sort.Direction direction = "desc".equalsIgnoreCase(parsedDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
            return Sort.by(direction, field);
        }

        String field = mapField(firstValue);
        if (!ALLOWED_FIELDS.contains(field)) {
            return defaultSort;
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (sort.length > 1 && sort[1] != null && "desc".equalsIgnoreCase(sort[1].trim())) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, field);
    }

    private static String mapField(String field) {
        return switch (field) {
            case "date" -> "orderDate";
            default -> field;
        };
    }
}
