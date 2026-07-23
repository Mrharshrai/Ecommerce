package com.shop.productservice.controller.internalController;

import com.shop.productservice.entity.Product;
import com.shop.productservice.entity.ProductVariant;
import com.shop.productservice.entity.ProductVariantSize;
import com.shop.productservice.enums.Size;
import com.shop.productservice.repository.ProductRepository;
import com.shop.productservice.repository.ProductVariantRepository;
import com.shop.productservice.repository.ProductVariantSizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/internal/stock")
public class InternalStockController {

    @Autowired
    private ProductVariantSizeRepository sizeRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductRepository productRepository;

    private void updateTotalsAndSave(ProductVariantSize size) {
        sizeRepository.save(size);

        ProductVariant variant = size.getVariant();
        if (variant != null) {
            variant.recalculateTotalProductVariantQuantity();
            variantRepository.save(variant);

            Product product = variant.getProduct();
            if (product != null) {
                product.recalculateTotalProductQuantity();
                productRepository.save(product);
            }
        }
    }

    @PostMapping("/decrease")
    public ResponseEntity<Map<String, Object>> decreaseStock(
            @RequestBody List<Map<String, Object>> requests,
            @RequestHeader("Authorization") String token) {

        int updatedCount = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> request : requests) {
            try {
                String asin = (String) request.get("productAsin");
                String skuCode = (String) request.get("skuCode");
                String sizeName = (String) request.get("size");
                Number qtyNum = (Number) request.get("quantity");
                if (qtyNum == null) {
                    qtyNum = (Number) request.get("quantityToUpdate");
                }
                if (qtyNum == null) {
                    throw new IllegalArgumentException("Quantity field (quantity or quantityToUpdate) is missing");
                }
                int quantity = qtyNum.intValue();

                Size sizeEnum = Size.valueOf(sizeName.toUpperCase());
                ProductVariantSize size = sizeRepository.findByVariant_SkuCodeAndSize(skuCode, sizeEnum)
                        .orElse(null);

                if (size != null && size.getQuantity() >= quantity) {
                    size.setQuantity(size.getQuantity() - quantity);
                    updateTotalsAndSave(size);
                    updatedCount++;
                } else if (size == null) {
                    errors.add("Size not found for SKU: " + skuCode + ", Size: " + sizeName);
                } else {
                    errors.add("Insufficient stock for SKU: " + skuCode + ", Size: " + sizeName);
                }
            } catch (Exception e) {
                errors.add("Error processing item: " + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("totalUpdated", updatedCount);
        response.put("errors", errors);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/increase")
    public ResponseEntity<Map<String, Object>> increaseStock(
            @RequestBody List<Map<String, Object>> requests,
            @RequestHeader("Authorization") String token) {

        int updatedCount = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> request : requests) {
            try {
                String asin = (String) request.get("productAsin");
                String skuCode = (String) request.get("skuCode");
                String sizeName = (String) request.get("size");
                Number qtyNum = (Number) request.get("quantity");
                if (qtyNum == null) {
                    qtyNum = (Number) request.get("quantityToUpdate");
                }
                if (qtyNum == null) {
                    throw new IllegalArgumentException("Quantity field (quantity or quantityToUpdate) is missing");
                }
                int quantity = qtyNum.intValue();

                Size sizeEnum = Size.valueOf(sizeName.toUpperCase());
                ProductVariantSize size = sizeRepository.findByVariant_SkuCodeAndSize(skuCode, sizeEnum)
                        .orElse(null);

                if (size != null) {
                    size.setQuantity(size.getQuantity() + quantity);
                    updateTotalsAndSave(size);
                    updatedCount++;
                } else {
                    errors.add("Size not found for SKU: " + skuCode + ", Size: " + sizeName);
                }
            } catch (Exception e) {
                errors.add("Error processing item: " + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("totalUpdated", updatedCount);
        response.put("errors", errors);

        return ResponseEntity.ok(response);
    }
}