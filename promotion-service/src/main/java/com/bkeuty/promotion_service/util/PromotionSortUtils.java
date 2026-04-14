package com.bkeuty.promotion_service.util;

import org.springframework.data.domain.Sort;

public final class PromotionSortUtils {
    private PromotionSortUtils() {
    }

    private static final java.util.Set<String> ALLOWED_FIELDS = java.util.Set.of("id", "title", "startAt", "endAt", "discountValue", "createdAt");

    public static Sort parseSort(String[] sort) {
        String sortField = "id";
        Sort.Direction direction = Sort.Direction.ASC;

        if (sort != null && sort.length > 0 && sort[0] != null && !sort[0].isBlank()) {
            String firstValue = sort[0].trim();
            if (firstValue.isBlank()) {
                return Sort.by(direction, sortField);
            }
            int commaIndex = firstValue.indexOf(',');

            if (commaIndex >= 0) {
                String parsedField = firstValue.substring(0, commaIndex).trim();
                String parsedDirection = firstValue.substring(commaIndex + 1).trim();

                if (ALLOWED_FIELDS.contains(parsedField)) {
                    sortField = parsedField;
                }
                if ("desc".equalsIgnoreCase(parsedDirection)) {
                    direction = Sort.Direction.DESC;
                }
            } else {
                if (ALLOWED_FIELDS.contains(firstValue)) {
                    sortField = firstValue;
                }
                if (sort.length > 1 && sort[1] != null && "desc".equalsIgnoreCase(sort[1].trim())) {
                    direction = Sort.Direction.DESC;
                }
            }
        }
        return Sort.by(direction, sortField);
    }
}
