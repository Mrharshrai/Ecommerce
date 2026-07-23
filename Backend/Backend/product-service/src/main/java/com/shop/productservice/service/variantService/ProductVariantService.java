package com.shop.productservice.service.variantService;


import com.shop.productservice.DTOs.VariantDTOs.RequestDTOs.CreateProductVariantRequestDTO;
import com.shop.productservice.DTOs.VariantDTOs.RequestDTOs.UpdateProductVariantRequestDTO;
import com.shop.productservice.DTOs.VariantDTOs.ResponseDTOs.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductVariantService {

    // --------------------ADMIN --------------------------
    CreatedProductVariantResponseDTO createVariant(CreateProductVariantRequestDTO dto);

    UpdateProductVariantResponseDTO updateVariant(UpdateProductVariantRequestDTO dto);

    ProductVariantResponseDTO getVariantById(Long variantId); //can get active/inactive

    ProductVariantResponseDTO getVariantBySkuCode(String skuCode);

    Page<ProductVariantResponseDTO> getAllVariants(Pageable pageable);

    List<ProductVariantResponseDTO> getVariantsByProductId(Long productId);

    ProductVariantResponseDTO getDeletedVariantById(Long variantId);

    Page<DeletedVariantListResponseDTO> getDeletedVariants(Pageable pageable);

    String deactivateVariant(Long variantId, String reason, HttpServletRequest request);

    String activateVariant(Long variantId,HttpServletRequest request);

    String deleteVariant(Long variantId,HttpServletRequest request,String reason);

    String restoreVariant(Long variantId,HttpServletRequest request);

    /**
     * =============================================================
     * Get all deleted variants belonging to a specific product.
     *
     * Returns only deleted variants.
     * Parent product may be Active or Inactive.
     * Deleted product is not allowed.
     * Ordered by latest deleted first.
     * =============================================================
     */
    Page<DeletedVariantListResponseDTO> getDeletedVariantsByProductId(
            Long productId,
            Pageable pageable
    );

    //------------ CUSTOMERS -------------------------

    ProductVariantResponseDTO getActiveVariantBySkuCode(String skuCode);

    List<ProductVariantListResponseDTO> searchVariantsByName(String name);

    List<ProductVariantListResponseDTO> searchVariantsByColor(String color);

    long countVariantsByProductId(Long productId);


}
