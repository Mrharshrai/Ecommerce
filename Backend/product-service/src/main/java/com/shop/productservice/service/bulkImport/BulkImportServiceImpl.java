package com.shop.productservice.service.bulkImport;

import com.shop.productservice.DTOs.BulkImportDTOs.RequestDTOs.BulkImportRequest;
import com.shop.productservice.DTOs.BulkImportDTOs.RequestDTOs.BulkImportRow;
import com.shop.productservice.DTOs.BulkImportDTOs.ResponseDTOs.BulkImportResponse;
import com.shop.productservice.DTOs.BulkImportDTOs.ResponseDTOs.ImportError;
import com.shop.productservice.entity.Product;
import com.shop.productservice.entity.ProductVariant;
import com.shop.productservice.entity.ProductVariantSize;
import com.shop.productservice.enums.ProductActivityType;
import com.shop.productservice.exception.InvalidProductException;
import com.shop.productservice.repository.ProductRepository;
import com.shop.productservice.repository.ProductVariantRepository;
import com.shop.productservice.repository.ProductVariantSizeRepository;
import com.shop.productservice.service.activityService.ProductActivityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BulkImportServiceImpl implements BulkImportService {

    private static final Logger log = LoggerFactory.getLogger(BulkImportServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductVariantSizeRepository sizeRepository;
    private final ProductActivityService activityService;

    @Override
    @Transactional
    public BulkImportResponse importRows(BulkImportRequest request, HttpServletRequest servletRequest) {

        List<BulkImportRow> rows = request.getRows();
        int totalRows = rows.size();

        List<ImportError> errors = new ArrayList<>();

        int productsCreated = 0;
        int productsUpdated = 0;
        int variantsCreated = 0;
        int variantsSkipped = 0;
        int sizesCreated = 0;
        int sizesFailed = 0;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = auth.getName();
        String adminRole = auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_ADMIN");

        Map<String, List<BulkImportRow>> productGroups = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> row.getProductAsin().trim().toUpperCase(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Map.Entry<String, List<BulkImportRow>> productEntry : productGroups.entrySet()) {
            String asin = productEntry.getKey();
            List<BulkImportRow> productRows = productEntry.getValue();

            try {
                BulkImportRow firstRow = productRows.get(0);

                ResolveResult<Product> productResult = resolveProduct(firstRow, errors);
                if (productResult == null) {
                    continue;
                }

                Product product = productResult.entity;
                boolean wasNewProduct = productResult.wasCreated;

                if (wasNewProduct) {
                    product = productRepository.save(product);
                }

                Map<String, List<BulkImportRow>> variantGroups = productRows.stream()
                        .collect(Collectors.groupingBy(
                                row -> (row.getVariantName().trim() + "|||" + row.getVariantColor().trim()).toLowerCase(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

                for (Map.Entry<String, List<BulkImportRow>> variantEntry : variantGroups.entrySet()) {
                    List<BulkImportRow> variantRows = variantEntry.getValue();

                    try {
                        BulkImportRow vRow = variantRows.get(0);

                        ResolveResult<ProductVariant> variantResult = resolveVariant(product, vRow, errors);
                        if (variantResult == null) {
                            continue;
                        }

                        ProductVariant variant = variantResult.entity;
                        boolean wasNewVariant = variantResult.wasCreated;

                        if (wasNewVariant) {
                            variant = variantRepository.save(variant);
                            product.getVariants().add(variant);
                        }

                        for (BulkImportRow sizeRow : variantRows) {
                            try {
                                processSize(product, variant, sizeRow);
                                sizesCreated++;
                            } catch (Exception e) {
                                sizesFailed++;
                                errors.add(ImportError.builder()
                                        .entityType("SIZE")
                                        .identifier(asin + " / " + vRow.getVariantName() + " / " + sizeRow.getSize().name())
                                        .message(e.getMessage())
                                        .build());
                            }
                        }

                        if (wasNewVariant) {
                            variantsCreated++;
                        } else {
                            variantsSkipped++;
                        }

                        variant.recalculateTotalProductVariantQuantity();
                        variantRepository.save(variant);

                    } catch (Exception e) {
                        errors.add(ImportError.builder()
                                .entityType("VARIANT")
                                .identifier(asin + " / " + variantEntry.getKey())
                                .message(e.getMessage())
                                .build());
                    }
                }

                product.recalculateTotalProductQuantity();
                productRepository.save(product);

                if (wasNewProduct) {
                    productsCreated++;
                } else {
                    productsUpdated++;
                }

                logActivity(adminEmail, adminRole, product.getId(),
                        ProductActivityType.PRODUCT_UPDATED,
                        "Bulk import: " + productRows.size() + " rows processed for product '"
                                + product.getName() + "' (ASIN: " + asin + ")",
                        servletRequest);

            } catch (Exception e) {
                errors.add(ImportError.builder()
                        .entityType("PRODUCT")
                        .identifier(asin)
                        .message("Failed to process product: " + e.getMessage())
                        .build());
            }
        }

        return BulkImportResponse.builder()
                .totalRows(totalRows)
                .productsCreated(productsCreated)
                .productsUpdated(productsUpdated)
                .variantsCreated(variantsCreated)
                .variantsSkipped(variantsSkipped)
                .sizesCreated(sizesCreated)
                .sizesSkipped(sizesFailed)
                .failedRows(errors.size())
                .errors(errors)
                .build();
    }

    private ResolveResult<Product> resolveProduct(BulkImportRow row, List<ImportError> errors) {
        String asin = row.getProductAsin().trim().toUpperCase();

        Optional<Product> existingOpt = productRepository.findByAsin(asin);

        if (existingOpt.isPresent()) {
            Product existing = existingOpt.get();

            if (existing.isDeleted()) {
                errors.add(ImportError.builder()
                        .entityType("PRODUCT")
                        .identifier(asin)
                        .message("Product with ASIN '" + asin
                                + "' exists but is soft-deleted. Restore it first.")
                        .build());
                return null;
            }

            if (!existing.isActive()) {
                errors.add(ImportError.builder()
                        .entityType("PRODUCT")
                        .identifier(asin)
                        .message("Product with ASIN '" + asin
                                + "' exists but is INACTIVE. Activate it first.")
                        .build());
                return null;
            }

            log.info("Bulk import: using existing product ASIN={}, ID={}", asin, existing.getId());
            return new ResolveResult<>(existing, false);
        }

        Product product = Product.builder()
                .asin(asin)
                .name(row.getProductName().trim())
                .description(row.getProductDescription().trim())
                .category(row.getProductCategory().trim().toUpperCase())
                .subCategory(row.getProductSubCategory() != null ? row.getProductSubCategory().trim() : null)
                .brand(row.getProductBrand().trim())
                .material(row.getProductMaterial().trim())
                .gender(row.getProductGender())
                .ageGroup(row.getProductAgeGroup())
                .totalProductQuantity(0)
                .isActive(true)
                .isDeleted(false)
                .isPublished(false)
                .variants(new ArrayList<>())
                .tags(row.getTags() != null ? row.getTags() : new ArrayList<>())
                .highlights(row.getHighlights() != null ? row.getHighlights() : new ArrayList<>())
                .build();

        log.info("Bulk import: creating new product ASIN={}", asin);
        return new ResolveResult<>(product, true);
    }

    private ResolveResult<ProductVariant> resolveVariant(Product product, BulkImportRow row, List<ImportError> errors) {
        String variantName = row.getVariantName().trim();
        String color = row.getVariantColor().trim();

        List<ProductVariant> existingVariants = variantRepository.findByProductId(product.getId());
        Optional<ProductVariant> match = existingVariants.stream()
                .filter(v -> v.getVariantName().equalsIgnoreCase(variantName)
                        && v.getColor().equalsIgnoreCase(color)
                        && !v.isDeleted())
                .findFirst();

        if (match.isPresent()) {
            log.info("Bulk import: variant already exists (product={}, name={}, color={}), skipping",
                    product.getAsin(), variantName, color);
            return new ResolveResult<>(match.get(), false);
        }

        String generatedSku = generateVariantSku(product, variantName, color);

        ProductVariant variant = ProductVariant.builder()
                .skuCode(generatedSku)
                .variantName(variantName)
                .color(color)
                .totalProductVariantQuantity(0)
                .product(product)
                .isActive(true)
                .isDeleted(false)
                .sizes(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        log.info("Bulk import: creating variant '{}' (SKU={}) for product ASIN={}",
                variantName, generatedSku, product.getAsin());
        return new ResolveResult<>(variant, true);
    }

    private void processSize(Product product, ProductVariant variant, BulkImportRow row) {

        boolean sizeExists = variant.getSizes().stream()
                .anyMatch(s -> s.getSize() == row.getSize() && !s.isDeleted());

        if (sizeExists) {
            throw new InvalidProductException(
                    "Size '" + row.getSize().name() + "' already exists for variant '"
                            + variant.getVariantName() + "'"
            );
        }

        String sizeSku = product.getAsin() + "-" + variant.getColor().toUpperCase() + "-" + row.getSize().name();

        ProductVariantSize size = ProductVariantSize.builder()
                .sizeSku(sizeSku)
                .size(row.getSize())
                .quantity(row.getQuantity())
                .mrp(row.getMrp())
                .weight(row.getWeight())
                .length(row.getLength())
                .width(row.getWidth())
                .height(row.getHeight())
                .variant(variant)
                .isActive(true)
                .isDeleted(false)
                .build();

        ProductVariantSize saved = sizeRepository.save(size);
        variant.getSizes().add(saved);

        log.info("Bulk import: created size '{}' (SKU={}) for variant '{}'",
                row.getSize().name(), sizeSku, variant.getVariantName());
    }

    private String generateVariantSku(Product product, String variantName, String color) {
        String productCode = "P" + product.getId();
        String nameCode = variantName.trim().replaceAll("\\s+", "")
                .substring(0, Math.min(4, variantName.trim().length()))
                .toUpperCase();
        String colorCode = color.trim().replaceAll("\\s+", "")
                .substring(0, Math.min(3, color.length()))
                .toUpperCase();
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return productCode + "-" + nameCode + "-" + colorCode + "-" + random;
    }

    private void logActivity(String userEmail, String userRole, Long targetId,
                             ProductActivityType type, String description,
                             HttpServletRequest request) {
        try {
            activityService.logActivity(userEmail, userRole, targetId, type, description, request);
        } catch (Exception e) {
            log.warn("Failed to log activity: {}", e.getMessage());
        }
    }

    private record ResolveResult<T>(T entity, boolean wasCreated) {}
}
