package com.bkeuty.order.service.shipping;

import com.bkeuty.order.dto.shipping.GHNDistrictListResponse;
import com.bkeuty.order.dto.shipping.GHNProvinceListResponse;
import com.bkeuty.order.dto.shipping.GHNWardListResponse;
import com.bkeuty.order.microservicecommunication.GHNCommunication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AddressService {
    private final GHNCommunication ghnCommunication;
    public AddressService(GHNCommunication ghnCommunication) {
        this.ghnCommunication = ghnCommunication;
    }

    public Mono<GHNProvinceListResponse> getGHNProvinceList() {
        return ghnCommunication.getGHNProvinceList();
    }
    public Mono<GHNDistrictListResponse> getGHNDistrictList(String provinceId) {
        return ghnCommunication.getGHNDistrictList(provinceId);
    }
    public Mono<GHNWardListResponse> getGHNWardList(String districtId) {
        return ghnCommunication.getGHNWardList(districtId);
    }
}
