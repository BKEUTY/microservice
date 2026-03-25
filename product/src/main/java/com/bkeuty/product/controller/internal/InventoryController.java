package com.bkeuty.product.controller.internal;

import com.bkeuty.product.dto.user.order.DecreaseStockRequestDto;
import com.bkeuty.product.dto.user.order.DecreaseStockResponseDto;
import com.bkeuty.product.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/inventory/internal")
public class InventoryController {
    private final InventoryService inventoryService;

    InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    @PostMapping("/decreaseStock")
    public ResponseEntity<List<DecreaseStockResponseDto>> decreaseStock( @RequestBody DecreaseStockRequestDto decreaseStockRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.decreaseOrderItem(decreaseStockRequestDto.getOrderItems()));
    }
}
