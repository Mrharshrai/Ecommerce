package com.shop.productservice.controller.basicController.CustomerController;

import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.CustomerProductResponseDTO;
import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.ProductListResponseDTO;
import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.ProductResponseDTO;
import com.shop.productservice.enums.AgeGroup;
import com.shop.productservice.enums.Gender;
import com.shop.productservice.service.productService.ProductService;
import com.shop.productservice.service.other.RelatedProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/customerProduct/products")
@Validated
@RequiredArgsConstructor
public class CustomerProductController {

    private final ProductService productService;
    private final RelatedProductService relatedProductService;

    // 1️⃣ GET ALL PUBLISHED PRODUCTS (with pagination)
    @GetMapping("/published")
    public ResponseEntity<Page<ProductListResponseDTO>> getPublishedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getAllPublishedProducts(pageable));
    }

    // 2️⃣ GET RECENTLY PUBLISHED PRODUCTS (with pagination)
    @GetMapping("/recent")
    public ResponseEntity<Page<ProductListResponseDTO>> getRecentlyPublishedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getRecentlyPublishedProducts(pageable));
    }

    // 3️⃣ SEARCH BY NAME (with pagination)
    @GetMapping("/search/name/{name}")
    public ResponseEntity<Page<ProductListResponseDTO>> searchByName(
            @PathVariable("name") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.searchByName(name, pageable));
    }

    // 4️⃣ FILTER BY CATEGORY (with pagination)
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductListResponseDTO>> getByCategory(
            @PathVariable("category") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getByCategory(category, pageable));
    }

    // 5️⃣ FILTER BY CATEGORY + SUB-CATEGORY (with pagination)
    @GetMapping("/category/{category}/subcategory/{subCategory}")
    public ResponseEntity<Page<ProductListResponseDTO>> getByCategoryAndSubCategory(
            @PathVariable("category") String category,
            @PathVariable("subCategory") String subCategory,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getByCategoryAndSubCategory(category, subCategory, pageable));
    }

    // 6️⃣ FILTER BY BRAND (with pagination)
    @GetMapping("/brand/{brand}")
    public ResponseEntity<Page<ProductListResponseDTO>> getByBrand(
            @PathVariable("brand") String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getByBrand(brand, pageable));
    }

    // 7️⃣ FILTER BY GENDER (with pagination)
    @GetMapping("/gender/{gender}")
    public ResponseEntity<Page<ProductListResponseDTO>> getByGender(
            @PathVariable("gender") Gender gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getByGender(gender, pageable));
    }

    // 8️⃣ FILTER BY AGE GROUP (with pagination)
    @GetMapping("/ageGroup/{ageGroup}")
    public ResponseEntity<Page<ProductListResponseDTO>> getByAgeGroup(
            @PathVariable("ageGroup") AgeGroup ageGroup,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getByAgeGroup(ageGroup, pageable));
    }

    // 9️⃣ SEARCH BY TAG (with pagination)
    @GetMapping("/search/tag/{tag}")
    public ResponseEntity<Page<ProductListResponseDTO>> searchByTag(
            @PathVariable("tag") String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.searchByTag(tag, pageable));
    }

    // 1️⃣0️⃣ GET PRODUCT BY ASIN
    @GetMapping("/getProductByAsin/{asin}")
    public ResponseEntity<CustomerProductResponseDTO> getActiveProductByAsin(@PathVariable("asin") String asin) {
        return ResponseEntity.ok(productService.getActiveProductByAsin(asin));
    }

    // 1️⃣1️⃣ FILTER BY MULTIPLE PARAMETERS (with pagination)
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductListResponseDTO>> filterProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.filterProducts(category, subCategory, gender, tag, pageable));
    }

    // 1️⃣2️⃣ PRICE RANGE FILTER (with pagination)
    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductListResponseDTO>> getByPrice(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByPriceRange(min, max, pageable));
    }

    // 1️⃣3️⃣ RELATED PRODUCTS
    @GetMapping("/{productId}/related")
    public ResponseEntity<List<ProductListResponseDTO>> getRelatedProducts(@PathVariable Long productId) {
        return ResponseEntity.ok(relatedProductService.getAllRelatedProducts(productId));
    }

}
