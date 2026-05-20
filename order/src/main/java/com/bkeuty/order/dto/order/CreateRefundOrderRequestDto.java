package com.bkeuty.order.dto.order;

import com.bkeuty.order.dto.shipping.AddressDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRefundOrderRequestDto {
    private Integer orderId;
    private List<Integer> orderItemId;
    private AddressDto fromAddress;
    private String phoneNumber;
    private String note;
}
