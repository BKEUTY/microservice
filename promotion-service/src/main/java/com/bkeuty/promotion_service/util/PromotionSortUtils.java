package com.bkeuty.promotion_service.util;

import org.springframework.data.domain.Sort;

public final class PromotionSortUtils {
    private PromotionSortUtils() {
    }

    public static Sort parseSort(String[] sort) {
        String sortField = (sort != null && sort.length > 0 && sort[0] != null && !sort[0].isBlank()) ? sort[0] : "id";
        Sort.Direction direction = Sort.Direction.ASC;
        if (sort != null && sort.length > 1 && sort[1] != null) {
            if ("desc".equalsIgnoreCase(sort[1])) {
                direction = Sort.Direction.DESC;
            }
        }
        return Sort.by(direction, sortField);
    }
}
