package com.bkeuty.order.util;

import org.springframework.data.domain.Sort;

public final class OrderSortUtils {
    private OrderSortUtils() {
    }

    public static Sort parseSort(String sort) {
        Sort sortObj = Sort.by(Sort.Direction.ASC, "id");
        if (sort == null || sort.isEmpty()) {
            return sortObj;
        }

        return switch (sort) {
            case "default", "id_asc" -> Sort.by(Sort.Direction.ASC, "id");
            case "id_desc" -> Sort.by(Sort.Direction.DESC, "id");
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "orderDate");
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "orderDate");
            case "total_asc" -> Sort.by(Sort.Direction.ASC, "total");
            case "total_desc" -> Sort.by(Sort.Direction.DESC, "total");
            default -> sortObj;
        };
    }
}
