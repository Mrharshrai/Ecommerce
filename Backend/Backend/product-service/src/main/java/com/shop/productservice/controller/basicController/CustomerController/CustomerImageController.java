package com.shop.productservice.controller.basicController.CustomerController;

import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.ProductVariantImageResponseDTO;
import com.shop.productservice.service.imageService.ProductVariantImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customerProduct/images")
@Validated
@RequiredArgsConstructor
public class CustomerImageController {

    private final ProductVariantImageService imageService;

    // 1️⃣ GET ALL ACTIVE IMAGES FOR CUSTOMER
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<ProductVariantImageResponseDTO>>
    getActiveImagesByVariantForCustomer(@PathVariable("variantId") Long variantId) {
        return ResponseEntity.ok(imageService.getActiveImagesByVariantForCustomer(variantId)
        );
    }
}
