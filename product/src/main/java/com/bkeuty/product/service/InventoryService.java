package com.bkeuty.product.service;

import com.bkeuty.product.dto.user.order.DecreaseStockResponseDto;
import com.bkeuty.product.dto.user.order.OrderItemDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.exception.ProductVariantNotFoundException;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {
    private final ProductVariantRepository productVariantRepository;
    public InventoryService(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    public List<DecreaseStockResponseDto> decreaseOrderItem(List<OrderItemDto> orderItems) {
        List<DecreaseStockResponseDto> decreaseStockResponseDtos = new ArrayList<>();
        for (OrderItemDto orderItem : orderItems) {
            ProductVariant productVariant = productVariantRepository.findById(orderItem.getProductVariantId()).orElseThrow(() -> new ProductVariantNotFoundException("ProductVariantNotFound"));
            Integer remainQuantity = productVariant.getStockQuantity() - orderItem.getQuantity();
            productVariant.setStockQuantity(remainQuantity);
            productVariantRepository.save(productVariant);
            decreaseStockResponseDtos.add(new DecreaseStockResponseDto(productVariant.getId(),productVariant.getProductVariantName(),productVariant.getProductImageUrl(),orderItem.getQuantity(),productVariant.getPrice()));
        }
        return decreaseStockResponseDtos;
    }
}
