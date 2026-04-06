package com.bkeuty.order.service.shipping;

import com.bkeuty.order.dto.shipping.*;
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
    public boolean isProvinceValid(ProvinceDtoGhn provinceDto) {
        Mono<GHNProvinceListResponse> GHNProvinceListResponseMono = getGHNProvinceList();
        GHNProvinceListResponse GHNProvinceListResponse = GHNProvinceListResponseMono.block();
        if(GHNProvinceListResponse==null|| GHNProvinceListResponse.getData()==null){
            return false;
        }
        for(ProvinceDtoGhn province : GHNProvinceListResponse.getData()){
            if(province.getProvinceID().equals(provinceDto.getProvinceID())&&province.getProvinceName().equals(provinceDto.getProvinceName())){
                return true;
            }
        }
        return false;
    }
    public boolean isDistrictValid(ProvinceDtoGhn provinceDto, DistrictDtoGhn districtDto) {
        Mono<GHNDistrictListResponse> ghnDistrictListResponseMono = getGHNDistrictList(provinceDto.getProvinceID().toString());

        GHNDistrictListResponse ghnDistrictListResponse = ghnDistrictListResponseMono.block();
        if(ghnDistrictListResponse==null|| ghnDistrictListResponse.getData()==null){
            return false;
        }
        for(DistrictDtoGhn district : ghnDistrictListResponse.getData()){
            if(district.getDistrictID().equals(districtDto.getDistrictID())&& district.getDistrictName().equals(districtDto.getDistrictName())){
                return true;
            }
        }
        return false;
    }
    public boolean isWardValid(DistrictDtoGhn districtDto, WardDtoGhn wardDto) {
        Mono<GHNWardListResponse> ghnWardListResponseMono = getGHNWardList(districtDto.getDistrictID().toString());
        GHNWardListResponse ghnWardListResponse = ghnWardListResponseMono.block();
        if(ghnWardListResponse==null|| ghnWardListResponse.getData()==null){
            return false;
        }
        for(WardDtoGhn ward : ghnWardListResponse.getData()){
            if(ward.getWardCode().equals(wardDto.getWardCode())&&ward.getWardName().equals(wardDto.getWardName())){
                return true;
            }
        }
        return false;
    }
}
