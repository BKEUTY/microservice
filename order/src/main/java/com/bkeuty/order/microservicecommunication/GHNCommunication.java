package com.bkeuty.order.microservicecommunication;

import com.bkeuty.order.dto.shipping.GHNDistrictListResponse;
import com.bkeuty.order.dto.shipping.GHNProvinceListResponse;
import com.bkeuty.order.dto.shipping.GHNWardListResponse;
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
}
