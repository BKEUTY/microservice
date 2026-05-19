package com.bkeuty.product.service;

import com.bkeuty.product.dto.user.order.DecreaseStockResponseDto;
import com.bkeuty.product.dto.user.order.OrderItemDto;
import com.bkeuty.product.entity.ProductVariant;
import com.bkeuty.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void decreaseOrderItem_ShouldDecreaseStock_WhenValidItemsProvided() {
        OrderItemDto item1 = new OrderItemDto();
        item1.setProductVariantId(10);
        item1.setQuantity(2);

        OrderItemDto item2 = new OrderItemDto();
        item2.setProductVariantId(20);
        item2.setQuantity(5);

        List<OrderItemDto> items = List.of(item2, item1); // Will be sorted internally

        ProductVariant variant1 = new ProductVariant();
        variant1.setId(10);
        variant1.setProductVariantName("Product A");
        variant1.setPrice(new BigDecimal("100000"));

        ProductVariant variant2 = new ProductVariant();
        variant2.setId(20);
        variant2.setProductVariantName("Product B");
        variant2.setPrice(new BigDecimal("200000"));

        when(productVariantRepository.findById(10)).thenReturn(Optional.of(variant1));
        when(productVariantRepository.findById(20)).thenReturn(Optional.of(variant2));
        
        when(productVariantRepository.decreaseStockAndIncreaseSold(10, 2)).thenReturn(1);
        when(productVariantRepository.decreaseStockAndIncreaseSold(20, 5)).thenReturn(1);

        List<DecreaseStockResponseDto> response = inventoryService.decreaseOrderItem(items);

        assertNotNull(response);
        assertEquals(2, response.size());
        
        assertEquals(10, response.get(0).getProductVariantId()); // Sorted order (10 comes first)
        assertEquals("Product A", response.get(0).getProductVariantName());
        
        verify(productVariantRepository, times(1)).decreaseStockAndIncreaseSold(10, 2);
        verify(productVariantRepository, times(1)).decreaseStockAndIncreaseSold(20, 5);
    }

    @Test
    void decreaseOrderItem_ShouldThrowBadRequest_WhenItemsNullOrEmpty() {
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () -> {
            inventoryService.decreaseOrderItem(null);
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatusCode());

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () -> {
            inventoryService.decreaseOrderItem(Collections.emptyList());
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());
    }

    @Test
    void decreaseOrderItem_ShouldThrowConflict_WhenStockInsufficient() {
        OrderItemDto item = new OrderItemDto();
        item.setProductVariantId(10);
        item.setQuantity(200);

        ProductVariant variant = new ProductVariant();
        variant.setId(10);

        when(productVariantRepository.findById(10)).thenReturn(Optional.of(variant));
        when(productVariantRepository.decreaseStockAndIncreaseSold(10, 200)).thenReturn(0); // Simulate failure

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            inventoryService.decreaseOrderItem(List.of(item));
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Insufficient stock"));
    }
}
