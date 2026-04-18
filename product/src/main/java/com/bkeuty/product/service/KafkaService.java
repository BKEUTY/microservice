package com.bkeuty.product.service;

import com.bkeuty.product.dto.user.order.DecreaseStockRequestDto;
import com.bkeuty.product.dto.user.order.DecreaseStockResponseDto;
import com.bkeuty.product.dto.user.order.DecreaseStockStatusDto;
import com.bkeuty.product.dto.user.order.OrderItemDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class KafkaService {
    private final ProductVariantRepository  productVariantRepository;
    private final KafkaTemplate<String, DecreaseStockStatusDto> kafkaTemplate;
    public KafkaService(ProductVariantRepository productVariantRepository,  KafkaTemplate<String, DecreaseStockStatusDto> kafkaTemplate) {
        this.productVariantRepository = productVariantRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "decrease-stock-topic")
    @Transactional
    public void receiveMessage(DecreaseStockRequestDto message){
        List<DecreaseStockResponseDto> failDecreaseStockItem = new ArrayList<>();
        boolean decreaseFailed = false;
        for(OrderItemDto item : message.getOrderItems()){
            ProductVariant productVariant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
            if(productVariant == null){
                failDecreaseStockItem.add(DecreaseStockResponseDto.builder().productVariantId(item.getProductVariantId()).build());
                decreaseFailed = true;
                continue;
            }
            int remainQuantity = productVariant.getStockQuantity() - item.getQuantity();
            if (remainQuantity < 0) {
                decreaseFailed = true;
                failDecreaseStockItem.add(DecreaseStockResponseDto.builder().productVariantId(item.getProductVariantId()).build());
                continue;
            }
            productVariant.setStockQuantity(remainQuantity);
        }
        if(decreaseFailed){
            kafkaTemplate.send("decrease-stock-status-topic", DecreaseStockStatusDto.builder().isSuccess(Boolean.FALSE).orderId(message.getOrderId()).failDecreaseStockItems(failDecreaseStockItem).build());
            throw new RuntimeException("Insufficient stock for product: "
                    +". Transaction rolled back.");

        }
        kafkaTemplate.send("decrease-stock-status-topic", DecreaseStockStatusDto.builder().isSuccess(Boolean.TRUE).orderId(message.getOrderId()).failDecreaseStockItems(failDecreaseStockItem).build());


    }

}
