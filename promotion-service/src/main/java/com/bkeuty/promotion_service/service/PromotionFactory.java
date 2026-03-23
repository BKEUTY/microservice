package com.bkeuty.promotion_service.service;

import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionRequest;
import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PromotionFactory {
    private final Map<String, PromotionStrategy> strategies;

    // Spring automatically injects ALL implementations of the interface here
    public PromotionFactory(List<PromotionStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        PromotionStrategy::getSupportedType,
                        Function.identity()
                ));
    }

    public CreatePromotionResponse executeCreation(CreatePromotionRequest request){
        PromotionStrategy strategy = strategies.get(request.getType());

        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported promotion type: " + request.getType());
        }

        return strategy.create(request);
    }
    public CreatePromotionResponse executeUpdate(Integer promotionId, CreatePromotionRequest request){
        PromotionStrategy strategy = strategies.get(request.getType());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported promotion type: " + request.getType());
        }

        return strategy.update(promotionId, request);
    }
}
