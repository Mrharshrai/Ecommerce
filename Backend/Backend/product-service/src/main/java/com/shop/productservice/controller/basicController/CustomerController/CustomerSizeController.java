package com.shop.productservice.controller.basicController.CustomerController;

import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.ProductVariantSizeListResponseDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.ProductVariantSizeResponseDTO;
import com.shop.productservice.enums.Size;
import com.shop.productservice.service.sizeService.ProductVariantSizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customerProduct/sizes")
@Validated
@RequiredArgsConstructor
public class CustomerSizeController {

    private final ProductVariantSizeService sizeService;

    // 1️⃣ GET SIZE BY SKU FOR CUSTOMER
    @GetMapping("/sku/{sizeSku}")
    public ResponseEntity<ProductVariantSizeResponseDTO> getSizeBySkuForCustomer(@PathVariable("sizeSku") String sizeSku) {
        return ResponseEntity.ok(sizeService.getSizeBySkuForCustomer(sizeSku));
    }

    // 2️⃣ SEARCH BASED ON SIZE ENUM
    @GetMapping("/searchBySize/{size}")
    public ResponseEntity<List<ProductVariantSizeListResponseDTO>> searchBySize(@PathVariable("size") Size size) {
        return ResponseEntity.ok(sizeService.searchBySize(size));
    }
}

