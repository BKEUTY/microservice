package com.bkeuty.product.util;

import org.springframework.data.domain.Sort;

public final class ProductSortUtils {
    private ProductSortUtils() {
    }

    public static Sort parseSort(String[] sort, String defaultField) {
        String sortField = defaultField != null ? defaultField : "id";
        Sort.Direction direction = Sort.Direction.ASC;

        if (sort != null && sort.length > 0 && sort[0] != null && !sort[0].isBlank()) {
            String firstValue = sort[0].trim();
            
            int delimiterIndex = firstValue.indexOf(',');
            if (delimiterIndex < 0) {
                delimiterIndex = firstValue.lastIndexOf('_');
            }
            
            if (delimiterIndex >= 0) {
                String parsedField = firstValue.substring(0, delimiterIndex).trim();
                String parsedDirection = firstValue.substring(delimiterIndex + 1).trim();
                
                if (!parsedField.isBlank()) {
                    sortField = mapField(parsedField);
                }
                if ("desc".equalsIgnoreCase(parsedDirection)) {
                    direction = Sort.Direction.DESC;
                }
            } else {
                sortField = mapField(firstValue);
                if (sort.length > 1 && sort[1] != null && "desc".equalsIgnoreCase(sort[1].trim())) {
                    direction = Sort.Direction.DESC;
                }
            }
        }
        return Sort.by(direction, sortField);
    }

    private static String mapField(String field) {
        return switch (field) {
            case "price" -> "promotionPrice";
            case "stock" -> "stockQuantity";
            case "rating" -> "averageRating";
            case "reviews" -> "reviewCount";
            default -> field;
        };
    }
}
