package com.bkeuty.order.dto.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateShippingOrderDto {
    @Builder.Default
    @JsonProperty("payment_type_id")
    private Integer paymentTypeId=1;

    @Builder.Default
    @JsonProperty("service_type_id")
    private Integer serviceTypeId=2;

    @Builder.Default
    @JsonProperty("note")
    private String note="Giao gio hanh chinh";

    @Builder.Default
    @JsonProperty("required_note")
    private String requiredNote = "KHONGCHOXEMHANG";

    @JsonProperty("to_name")
    private String toName;

    @JsonProperty("to_phone")
    private String toPhone;

    @JsonProperty("to_address")
    private String toAddress;

    @JsonProperty("to_ward_name")
    private String toWardName;

    @JsonProperty("to_district_name")
    private String toDistrictName;

    @JsonProperty("to_province_name")
    private String toProvinceName;

    @Builder.Default
    @JsonProperty("cod_amount")
    private Integer codAmount = 0;

    @Builder.Default
    @JsonProperty("weight")
    private Integer weight = 100;

    @Builder.Default
    @JsonProperty("length")
    private Integer length = 10;

    @Builder.Default
    @JsonProperty("width")
    private Integer width = 10;

    @Builder.Default
    @JsonProperty("height")
    private Integer height = 10;

    private List<ShippingItemDto> items;
}
