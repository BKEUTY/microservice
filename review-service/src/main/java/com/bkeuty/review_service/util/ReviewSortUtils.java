package com.bkeuty.review_service.util;

import org.springframework.data.domain.Sort;
import java.util.Set;

public final class ReviewSortUtils {
    private ReviewSortUtils() {
    }

    private static final Set<String> ALLOWED_FIELDS = Set.of("id", "rating", "createdAt", "variantId", "userId");

    public static Sort parseSort(String[] sort, String defaultField) {
        String sortField = (defaultField != null && ALLOWED_FIELDS.contains(defaultField)) ? defaultField : "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;

        if (sort != null && sort.length > 0 && sort[0] != null && !sort[0].isBlank()) {
            String firstValue = sort[0].trim();
            if (firstValue.isBlank()) {
                return Sort.by(direction, sortField);
            }
            
            int delimiterIndex = firstValue.indexOf(',');
            if (delimiterIndex < 0) {
                delimiterIndex = firstValue.lastIndexOf('_');
            }
            
            if (delimiterIndex >= 0) {
                String parsedField = firstValue.substring(0, delimiterIndex).trim();
                String parsedDirection = firstValue.substring(delimiterIndex + 1).trim();
                
                if (ALLOWED_FIELDS.contains(parsedField)) {
                    sortField = parsedField;
                }
                if ("asc".equalsIgnoreCase(parsedDirection)) {
                    direction = Sort.Direction.ASC;
                } else {
                    direction = Sort.Direction.DESC;
                }
            } else {
                if (ALLOWED_FIELDS.contains(firstValue)) {
                    sortField = firstValue;
                }
                if (sort.length > 1 && sort[1] != null && "asc".equalsIgnoreCase(sort[1].trim())) {
                    direction = Sort.Direction.ASC;
                }
            }
        }
        return Sort.by(direction, sortField);
    }
}
