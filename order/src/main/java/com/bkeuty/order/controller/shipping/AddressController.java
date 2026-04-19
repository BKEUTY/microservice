//package com.bkeuty.order.controller.shipping;
//
//import com.bkeuty.order.dto.shipping.GHNDistrictListResponse;
//import com.bkeuty.order.dto.shipping.GHNProvinceListResponse;
//import com.bkeuty.order.dto.shipping.GHNWardListResponse;
//import com.bkeuty.order.service.shipping.AddressService;
//import com.bkeuty.order.service.shipping.ShippingService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
//@RestController
//@RequestMapping("/api/address")
//public class AddressController {
//    private final AddressService addressService;
//    AddressController(AddressService addressService) {
//        this.addressService = addressService;
//    }
//
//    @GetMapping("/province")
//    public ResponseEntity<Mono<GHNProvinceListResponse>> getGHNProvinceList() {
//        return ResponseEntity.ok(addressService.getGHNProvinceList());
//    }
//    @GetMapping("/district")
//    public ResponseEntity<Mono<GHNDistrictListResponse>> getGHNDistrictList(@RequestParam String provinceId) {
//        return ResponseEntity.ok(addressService.getGHNDistrictList(provinceId));
//    }
//    @GetMapping("/ward")
//    public ResponseEntity<Mono<GHNWardListResponse>> getGHNWardList(@RequestParam String districtId) {
//        return ResponseEntity.ok(addressService.getGHNWardList(districtId));
//    }
//
//
//}
