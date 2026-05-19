package com.bkeuty.order.dto.shipping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GhnWebhookMessage {
    @JsonProperty("CODAmount")
    private Integer amount;

    @JsonProperty("OrderCode")
    private String orderCode;

    @JsonProperty("Status")
    private String status;
    @JsonProperty("IsRefund")
    private boolean isRefund;
}
