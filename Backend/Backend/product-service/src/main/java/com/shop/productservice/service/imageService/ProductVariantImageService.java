package com.shop.productservice.service.imageService;

import com.shop.productservice.DTOs.ImageDTOs.RequestDTOs.CreateProductVariantImageRequestDTO;
import com.shop.productservice.DTOs.ImageDTOs.RequestDTOs.UpdateProductVariantImageRequestDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.CreatedProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.DeletedImageListResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.ProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.UpdateProductVariantImageResponseDTO;

import java.util.List;

import com.shop.productservice.DTOs.SizeDTOs.ResponseDTOs.DeletedSizeListResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;

public interface ProductVariantImageService {

    //--------------------------------------- ADMIN ------------------------------------------
    // CREATE IMAGE
    CreatedProductVariantImageResponseDTO createImage(CreateProductVariantImageRequestDTO dto);

    // UPDATE IMAGE
    UpdateProductVariantImageResponseDTO updateImage(UpdateProductVariantImageRequestDTO dto);

    // GET / FETCH IMAGE
    ProductVariantImageResponseDTO getImageById(Long imageId);
    Page<ProductVariantImageResponseDTO> getAllImages(Pageable pageable); // non-deleted only
    List<ProductVariantImageResponseDTO> getImagesByVariantId(Long variantId);

    // Deleted images
    ProductVariantImageResponseDTO getDeletedImageById(Long imageId);
    Page<DeletedImageListResponseDTO> getAllDeletedImages(Pageable pageable);

    // ADMIN — ACTIVATE / DEACTIVATE
    String deactivateImage(Long imageId, HttpServletRequest request,String reason);
    String activateImage(Long imageId, HttpServletRequest request);            // only if parent variant active

    // ADMIN — DELETE / RESTORE
    String deleteImage(Long imageId, HttpServletRequest request,String reason);              // soft delete
    String restoreImage(Long imageId, HttpServletRequest request);             // restore (if variant active)

    Page<DeletedImageListResponseDTO> getDeletedImagesByVariantId(Long variantId, Pageable pageable);

    // ADMIN — COUNT
    long countImagesByVariant(Long variantId);

    // NEW: admin filtered list (active only)
    List<ProductVariantImageResponseDTO> getActiveImagesByVariantForAdmin(Long variantId);


    //--------------------------------------- CUSTOMER ------------------------------------------
    List<ProductVariantImageResponseDTO> getActiveImagesByVariantForCustomer(Long variantId);

}
