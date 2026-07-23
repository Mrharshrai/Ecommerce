package com.shop.productservice.service.other;

import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.ProductListResponseDTO;
import com.shop.productservice.DTOs.RelatedProductDTOs.RequestDTOs.AddRelatedProductRequest;
import com.shop.productservice.DTOs.RelatedProductDTOs.ResponseDTOs.RelatedProductResponse;

import java.util.List;

public interface RelatedProductService {

    RelatedProductResponse getRelatedProduct(Long productId, Long relatedVariantId);

    RelatedProductResponse addRelatedProduct(AddRelatedProductRequest request);

    void removeRelatedProduct(Long productId, Long relatedVariantId);

    void restoreRelatedProduct(Long productId, Long relatedVariantId);

    List<RelatedProductResponse> getManualRelatedProducts(Long productId);

    List<ProductListResponseDTO> getAllRelatedProducts(Long productId);

    RelatedProductResponse activateRelatedProduct(Long productId, Long relatedVariantId);

    RelatedProductResponse deactivateRelatedProduct(Long productId, Long relatedVariantId);
}
