package com.shop.productservice.service.sizeService;

import com.shop.productservice.DTOs.SizeDTOs.RequestDTOs.CreateProductVariantSizeRequestDTO;
import com.shop.productservice.DTOs.SizeDTOs.RequestDTOs.UpdateProductVariantSizeRequestDTO;
import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.*;
import com.shop.productservice.enums.Size;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductVariantSizeService {

    // ADMIN
    CreatedProductVariantSizeResponseDTO createSize(CreateProductVariantSizeRequestDTO dto);
    UpdateProductVariantSizeResponseDTO updateSize(UpdateProductVariantSizeRequestDTO dto);
    ProductVariantSizeResponseDTO getSizeById(Long sizeId);
    ProductVariantSizeResponseDTO getSizeBySku(String sizeSku);
    ProductVariantSizeResponseDTO getDeletedSize(Long sizeId);
    Page<DeletedSizeListResponseDTO> getAllDeletedSizes(Pageable pageable);
    Page<ProductVariantSizeResponseDTO> getAllSizes(Pageable pageable);
    List<ProductVariantSizeResponseDTO> getSizesByVariantId(Long variantId);
    String deactivateSize(Long sizeId, HttpServletRequest request, String reason);
    String activateSize(Long sizeId,HttpServletRequest request);
    String deleteSize(Long sizeId, HttpServletRequest request, String reason);
    String restoreSize(Long sizeId, HttpServletRequest request);
    Page<DeletedSizeListResponseDTO> getDeletedSizesByVariantId(Long variantId, Pageable pageable);

    // CUSTOMER
    ProductVariantSizeResponseDTO getSizeBySkuForCustomer(String sizeSku);
    List<ProductVariantSizeListResponseDTO> searchBySize(Size size);
    long countsizesByVariantId(Long variantId);
}

