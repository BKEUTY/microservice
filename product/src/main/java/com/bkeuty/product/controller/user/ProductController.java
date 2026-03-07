//package com.bkeuty.product.controller.user;
//
//import com.bkeuty.product.dto.user.cart.ProductVariantDto;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("api/product")
//public class ProductController {
//    @GetMapping()
//    public ResponseEntity<Page<ProductVariantDto>> getProductVariants(int page, String name, String category, String sortPrice ) {
//        Pageable pageable = PageRequest.of(page, 30);
//
//    }
//
//}
