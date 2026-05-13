package com.bkeuty.promotion_service.dto.CreatePromotion;

import com.bkeuty.promotion_service.dto.CreatePromotion.abstractClass.CreatePromotionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class CreateUserPromotionResponse extends CreatePromotionResponse {
    private Set<Integer> birthdayMonth;
    private Set<String> userIds;
}
