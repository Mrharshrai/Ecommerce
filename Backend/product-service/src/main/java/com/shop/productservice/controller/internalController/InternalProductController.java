package com.shop.productservice.controller.internalController;

import com.shop.productservice.DTOs.InternalDTOs.InternalVariantResponseDTO;
import com.shop.productservice.DTOs.DiscountDTOs.ResponseDTOs.PricingInfo;
import com.shop.productservice.entity.Product;
import com.shop.productservice.entity.ProductVariant;
import com.shop.productservice.entity.ProductVariantImage;
import com.shop.productservice.entity.ProductVariantSize;
import com.shop.productservice.repository.ProductRepository;
import com.shop.productservice.repository.ProductVariantRepository;
import com.shop.productservice.repository.ProductVariantSizeRepository;
import com.shop.productservice.service.other.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/products")
public class InternalProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private ProductVariantSizeRepository sizeRepository;

    @GetMapping("/variant")
    public ResponseEntity<InternalVariantResponseDTO> getVariantForCart(
            @RequestParam("productAsin") String productAsin,
            @RequestParam("skuCode") String skuCode) {

        Product product = productRepository.findByAsin(productAsin)
                .orElseThrow(() -> new RuntimeException("Product not found with ASIN: " + productAsin));

        ProductVariant variant = variantRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Variant not found with SKU: " + skuCode));

        if (!variant.getProduct().getId().equals(product.getId())) {
            throw new RuntimeException("Variant does not belong to the specified product");
        }

        if (!variant.isActive() || variant.isDeleted()) {
            throw new RuntimeException("Variant is not active or has been deleted");
        }

        if (!product.isActive() || !product.isPublished() || product.isDeleted()) {
            throw new RuntimeException("Product is not available for purchase");
        }

        // ── First active image ─────────────────────────────────────────────
        String firstImageUrl = variant.getImages().stream()
                .filter(img -> img.isActive() && !img.isDeleted())
                .sorted((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()))
                .map(ProductVariantImage::getImage)
                .findFirst()
                .orElse(null);

        // ── Sizes ──────────────────────────────────────────────────────────
        List<InternalVariantResponseDTO.InternalSizeInfo> sizes = variant.getSizes().stream()
                .filter(size -> size.isActive() && !size.isDeleted())
                .map(size -> InternalVariantResponseDTO.InternalSizeInfo.builder()
                        .size(size.getSize().name())
                        .quantity(size.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // ── MRP (take from first active size) ─────────────────────────────
        BigDecimal mrp = variant.getSizes().stream()
                .filter(size -> size.isActive() && !size.isDeleted())
                .findFirst()
                .map(ProductVariantSize::getMrp)
                .orElse(BigDecimal.ZERO);

        // ── Resolve active discount using DiscountService ──
        PricingInfo pricingInfo = discountService.calculatePricing(product, mrp);

        // ── Build response ─────────────────────────────────────────────────
        InternalVariantResponseDTO response = InternalVariantResponseDTO.builder()
                .productName(product.getName())
                .color(variant.getColor())
                .price(mrp)
                .sellingPrice(pricingInfo.getSellingPrice())
                .discountAmount(pricingInfo.getDiscountAmount())
                .discountPercent(pricingInfo.getDiscountPercent())
                .hasDiscount(pricingInfo.isHasDiscount())
                .imageUrl(firstImageUrl)
                .sizes(sizes)
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<java.util.Map<String, Object>> getOrderItemDetails(@PathVariable Long orderItemId) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("orderItemId", orderItemId);
        response.put("productName", "Standard Catalog Product (" + orderItemId + ")");

        // Find any active size SKU to represent this returned item's mock sku and ID
        java.util.List<ProductVariantSize> sizes = sizeRepository.findAll();
        ProductVariantSize activeSize = sizes.stream()
                .filter(s -> s.isActive() && !s.isDeleted())
                .findFirst()
                .orElse(null);

        if (activeSize != null) {
            response.put("productId", activeSize.getId()); // Using size ID so restoreInventory resolves it directly
            response.put("sku", activeSize.getSizeSku());
            response.put("unitPrice", activeSize.getMrp());
        } else {
            response.put("productId", 1L);
            response.put("sku", "MOCK-SKU-" + orderItemId);
            response.put("unitPrice", BigDecimal.valueOf(299.99));
        }

        response.put("returnPolicy", "RETURNABLE");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/inventory/restore")
    public ResponseEntity<Void> restoreInventory(
            @PathVariable Long productId,
            @RequestBody java.util.Map<String, Object> body) {

        Number quantityVal = (Number) body.get("quantity");
        int quantity = quantityVal != null ? quantityVal.intValue() : 1;

        ProductVariantSize size = sizeRepository.findById(productId).orElse(null);
        if (size != null) {
            size.setQuantity(size.getQuantity() + quantity);
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
        return ResponseEntity.ok().build();
    }
}