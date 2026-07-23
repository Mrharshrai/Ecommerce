package com.shop.productservice.controller.basicController.CustomerController;

import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.ProductVariantListResponseDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.ProductVariantResponseDTO;
import com.shop.productservice.service.variantService.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customerProduct/variants")
@Validated
@RequiredArgsConstructor
public class CustomerVariantController {

    private final ProductVariantService variantService;

    // GET ACTIVE VARIANT BY SKU
    @GetMapping("/sku/{skuCode}")
    public ResponseEntity<ProductVariantResponseDTO> getActiveVariantBySku(@PathVariable("skuCode") String skuCode) {
        return ResponseEntity.ok(variantService.getActiveVariantBySkuCode(skuCode));
    }

    // SEARCH BY NAME
    @GetMapping("/search/name")
    public ResponseEntity<List<ProductVariantListResponseDTO>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(variantService.searchVariantsByName(name));
    }

    // SEARCH BY COLOR
    @GetMapping("/search/color")
    public ResponseEntity<List<ProductVariantListResponseDTO>> searchByColor(@RequestParam String color) {
        return ResponseEntity.ok(variantService.searchVariantsByColor(color));
    }

    // COUNT VARIANTS BY PRODUCT ID
    @GetMapping("/countVariant/{productId}")
    public ResponseEntity<Long> countByProduct(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(variantService.countVariantsByProductId(productId));
    }

}
