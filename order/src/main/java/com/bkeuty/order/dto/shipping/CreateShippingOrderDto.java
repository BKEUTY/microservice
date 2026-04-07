package com.bkeuty.order.dto.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateShippingOrderDto {
    @JsonProperty("payment_type_id")
    private Integer paymentTypeId;
    @JsonProperty("note")
    private String note;
    @JsonProperty("required_note")
    private String requiredNote = "KHONGCHOXEMHANG";

    @Value("${ghn.return-phone}")
    @JsonProperty("return_phone")
    private String returnPhone;

    @Value("${ghn.return-address}")
    @JsonProperty("return_address")
    private String returnAddress;

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

    @JsonProperty("cod_amount")
    private Integer codAmount = 0;
    @JsonProperty("weight")
    private Integer weight = 100;
    @JsonProperty("length")
    private Integer length = 10;
    @JsonProperty("width")
    private Integer width = 10;
    @JsonProperty("height")
    private Integer height = 10;


}
