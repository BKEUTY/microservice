package com.bkeuty.product.converter;

import com.bkeuty.product.entity.ProductImage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProductImageAttributeConverter implements AttributeConverter<ProductImage, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(ProductImage productImage) {
        try {
            return objectMapper.writeValueAsString(productImage);
        } catch (JsonProcessingException jpe) {
            System.out.println("Cannot convert ProductImage to JSON: " + jpe.getMessage());
            return null;
        }
    }

    @Override
    public ProductImage convertToEntityAttribute(String value) {
        try {
            return objectMapper.readValue(value, ProductImage.class);
        } catch (JsonProcessingException e) {
            System.out.println("Cannot convert JSON into Address");
            return null;
        }
    }
}
