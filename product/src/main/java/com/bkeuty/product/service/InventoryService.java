package com.bkeuty.product.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bkeuty.product.dto.user.order.DecreaseStockResponseDto;
import com.bkeuty.product.dto.user.order.OrderItemDto;
import com.bkeuty.product.dto.user.product.PromotionPriceDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.microservicecommunication.PromotionService;
import com.bkeuty.product.repository.ProductVariantRepository;

@Service
public class InventoryService {
    private final ProductVariantRepository productVariantRepository;
    private final PromotionService promotionService;
    public InventoryService(ProductVariantRepository productVariantRepository, PromotionService promotionService) {
        this.productVariantRepository = productVariantRepository;
        this.promotionService = promotionService;
    }

    @Transactional
    public List<DecreaseStockResponseDto> decreaseOrderItem(List<OrderItemDto> orderItems) {
        List<DecreaseStockResponseDto> decreaseStockResponseDtos = new ArrayList<>();
        for (OrderItemDto orderItem : orderItems) {
            if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid quantity for order item: " + orderItem.getQuantity());
            }
            
            ProductVariant productVariant = productVariantRepository.findById(orderItem.getProductVariantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Product variant not found: " + orderItem.getProductVariantId()));
            
            PromotionPriceDto promotionPriceDto = promotionService.getPromotionPrice(productVariant);
            
            int updatedRows = productVariantRepository.decreaseStockAndIncreaseSold(
                    productVariant.getId(), 
                    orderItem.getQuantity()
            );
            
            if (updatedRows == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Insufficient stock for variant ID: " + productVariant.getId());
            }

            BigDecimal finalPrice = (promotionPriceDto != null && promotionPriceDto.getNewPrice() != null) 
                    ? promotionPriceDto.getNewPrice() 
                    : productVariant.getPrice();

            decreaseStockResponseDtos.add(new DecreaseStockResponseDto(
                    productVariant.getId(),
                    productVariant.getProductVariantName(),
                    productVariant.getProductImageUrl(),
                    orderItem.getQuantity(),
                    productVariant.getPrice(),
                    finalPrice
            ));
        }
        return decreaseStockResponseDtos;
    }
}
