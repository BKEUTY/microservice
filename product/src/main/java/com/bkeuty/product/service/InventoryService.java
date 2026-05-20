package com.bkeuty.product.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bkeuty.product.dto.user.order.DecreaseStockResponseDto;
import com.bkeuty.product.dto.user.order.OrderItemDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.repository.ProductVariantRepository;

@Service
public class InventoryService {
    private final ProductVariantRepository productVariantRepository;
    public InventoryService(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional
    public List<DecreaseStockResponseDto> decreaseOrderItem(List<OrderItemDto> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Order items must not be null or empty");
        }
        
        List<DecreaseStockResponseDto> decreaseStockResponseDtos = new ArrayList<>();
        List<OrderItemDto> sortedOrderItems = new ArrayList<>(orderItems);
        sortedOrderItems.sort(java.util.Comparator.comparing(OrderItemDto::getProductVariantId));

        for (OrderItemDto orderItem : sortedOrderItems) {
            if (orderItem.getProductVariantId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Product variant ID is required for each order item");
            }
            
            if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Invalid quantity for order item: " + orderItem.getQuantity());
            }
            
            ProductVariant productVariant = productVariantRepository.findById(orderItem.getProductVariantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Product variant not found: " + orderItem.getProductVariantId()));
            
            int updatedRows = productVariantRepository.decreaseStockAndIncreaseSold(
                    productVariant.getId(), 
                    orderItem.getQuantity()
            );
            
            if (updatedRows == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Insufficient stock for variant: " + productVariant.getProductVariantName());
            }
            String imgUrl =  null;
            if(productVariant.getProductImageUrls()!=null){
                if(productVariant.getProductImageUrls().getFirst()!=null){
                    if(productVariant.getProductImageUrls().getFirst().getImageUrl()!=null){
                        imgUrl = productVariant.getProductImageUrls().getFirst().getImageUrl();
                    }
                }
            }
            decreaseStockResponseDtos.add(new DecreaseStockResponseDto(
                    productVariant.getId(),
                    productVariant.getProductVariantName(),
                    imgUrl,
                    orderItem.getQuantity(),
                    productVariant.getPrice(),
                    productVariant.getPromotionPrice()
            ));
        }
        return decreaseStockResponseDtos;
    }
}
