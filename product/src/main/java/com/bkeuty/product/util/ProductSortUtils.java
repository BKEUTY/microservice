package com.bkeuty.product.util;

import org.springframework.data.domain.Sort;
import java.util.Set;
import java.util.Map;

public final class ProductSortUtils {
    private ProductSortUtils() {
    }

    private static final Set<String> VARIANT_ALLOWED = Set.of("id", "price", "promotionPrice", "stockQuantity", "productVariantName", "averageRating", "reviewCount", "status");
    private static final Set<String> PRODUCT_ALLOWED = Set.of("id", "name", "description");
    private static final Set<String> BRAND_ALLOWED = Set.of("id", "brandName", "brandStatus", "category");

    private static final Map<String, String> VARIANT_MAPPINGS = Map.of(
            "price", "promotionPrice",
            "stock", "stockQuantity",
            "rating", "averageRating",
            "reviews", "reviewCount",
            "variantName", "productVariantName",
            "name", "productVariantName"
    );

    public static Sort parseVariantSort(String[] sort, String defaultField) {
        return parseSort(sort, defaultField, VARIANT_ALLOWED, VARIANT_MAPPINGS);
    }

    public static Sort parseProductSort(String[] sort, String defaultField) {
        return parseSort(sort, defaultField, PRODUCT_ALLOWED, Map.of());
    }

    public static Sort parseBrandSort(String[] sort, String defaultField) {
        return parseSort(sort, defaultField, BRAND_ALLOWED, Map.of("name", "brandName", "status", "brandStatus"));
    }

    private static Sort parseSort(String[] sort, String defaultField, Set<String> allowedFields, Map<String, String> mappings) {
        String sortField = (defaultField != null && allowedFields.contains(defaultField)) ? defaultField : "id";
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

                String mappedField = mappings.getOrDefault(parsedField, parsedField);
                if (allowedFields.contains(mappedField)) {
                    sortField = mappedField;
                }
                if ("desc".equalsIgnoreCase(parsedDirection)) {
                    direction = Sort.Direction.DESC;
                }
            } else {
                String mappedField = mappings.getOrDefault(firstValue, firstValue);
                if (allowedFields.contains(mappedField)) {
                    sortField = mappedField;
                }
                if (sort.length > 1 && sort[1] != null && "desc".equalsIgnoreCase(sort[1].trim())) {
                    direction = Sort.Direction.DESC;
                }
            }
        }
        return Sort.by(direction, sortField);
    }
}
