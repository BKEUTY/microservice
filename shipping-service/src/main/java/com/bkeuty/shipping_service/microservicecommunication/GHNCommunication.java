package com.bkeuty.shipping_service.microservicecommunication;

import com.bkeuty.shipping_service.dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Service
public class GHNCommunication {
    private final WebClient GHNWebClient;
    public GHNCommunication(@Qualifier("GHNWebClient") WebClient GHNWebClient) {
        this.GHNWebClient = GHNWebClient;
    }
    @Value("${ghn.api-token}")
    private String apiToken;
    @Value("${ghn.shop-id}")
    private String shopId;

    public Mono<GHNProvinceListResponse> getGHNProvinceList() {
        return GHNWebClient.get().uri("/shiip/public-api/master-data/province").header("Token",apiToken).retrieve().bodyToMono(GHNProvinceListResponse.class);
    }
    public Mono<GHNDistrictListResponse> getGHNDistrictList(String provinceId) {
        return GHNWebClient.get().uri(uriBuilder -> uriBuilder.path("/shiip/public-api/master-data/district").queryParam("province_id",provinceId).build()).header("Token",apiToken)
                .retrieve().bodyToMono(GHNDistrictListResponse.class);
    }
    public Mono<GHNWardListResponse> getGHNWardList(String districtId) {
        return GHNWebClient.get().uri(uriBuilder -> uriBuilder.path("/shiip/public-api/master-data/ward").queryParam("district_id",districtId).build()).header("Token",apiToken)
                .retrieve().bodyToMono(GHNWardListResponse.class);
    }

    public Mono<CalShippingFeeResponseDto>  getCalShippingFee(CalShippingFeeDto  calShippingFeeDto) {
        return GHNWebClient.post().uri(uriBuilder -> uriBuilder.path("/shiip/public-api/v2/shipping-order/fee").build()).bodyValue(calShippingFeeDto)
                .header("Token",apiToken)
                .header("ShopId",shopId)
                .retrieve().bodyToMono(CalShippingFeeResponseDto.class);
    }

    public Mono<CalShippingTimeResponseDto>  getCalShippingTime(CalShippingTimeDto calShippingTimeDto) {
        return GHNWebClient.post().uri(uriBuilder -> uriBuilder.path("/shiip/public-api/v2/shipping-order/leadtime").build()).bodyValue(calShippingTimeDto)
                .header("Token",apiToken)
                .header("ShopId",shopId)
                .retrieve().bodyToMono(CalShippingTimeResponseDto.class);
    }

    public Mono<CreateShippingOrderResponseDto> createShippingOrder(CreateShippingOrderDto createShippingOrderDto) {
        return GHNWebClient.post().uri(uriBuilder -> uriBuilder.path("shiip/public-api/v2/shipping-order/create").build()).bodyValue(createShippingOrderDto)
                .header("Token",apiToken)
                .header("ShopId",shopId)
                .retrieve().bodyToMono(CreateShippingOrderResponseDto.class);
    }
    public Mono<CreateShippingOrderResponseDto> createRefundOrder(CreateRefundShippingDto createRefundShippingDto) {
        return GHNWebClient.post().uri(uriBuilder -> uriBuilder.path("shiip/public-api/v2/shipping-order/create").build()).bodyValue(createRefundShippingDto)
                .header("Token",apiToken)
                .header("ShopId",shopId)
                .retrieve().bodyToMono(CreateShippingOrderResponseDto.class);
    }

    public Mono<GetShippingOrderStatusResponseDto> getShippingStatus(OrderCodeDto orderCodeDto) {
        return GHNWebClient.post().uri(uriBuilder -> uriBuilder.path("shiip/public-api/v2/shipping-order/detail").build()).bodyValue(orderCodeDto)
                .header("Token",apiToken)
                .header("ShopId",shopId)
                .retrieve().bodyToMono(GetShippingOrderStatusResponseDto.class);
    }
}
