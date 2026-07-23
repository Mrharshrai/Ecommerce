package com.shop.productservice.mapper;

import com.shop.productservice.DTOs.ImageDTOs.RequestDTOs.CreateProductVariantImageRequestDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.CreatedProductVariantImageResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.ProductVariantImageListResponseDTO;
import com.shop.productservice.DTOs.ImageDTOs.ResponseDTOs.ProductVariantImageResponseDTO;
import com.shop.productservice.entity.ProductVariant;
import com.shop.productservice.entity.ProductVariantImage;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class ProductVariantImageMapper {

    // ---------------- CREATE DTO → ENTITY ----------------
    public ProductVariantImage toEntity(CreateProductVariantImageRequestDTO dto) {
        if (dto == null) return null;

        return ProductVariantImage.builder()
                .id(null)
                .image(dto.getImage())
                .sortOrder(dto.getSortOrder())
                .altText(dto.getAltText())
                .variant(null)      // set in SERVICE
                .isActive(true)
                .isDeleted(false)
                .build();
    }

    // ---------------- AFTER CREATE RESPONSE ----------------
    public CreatedProductVariantImageResponseDTO toCreatedResponseDTO(ProductVariantImage image) {
        if (image == null) return null;

        CreatedProductVariantImageResponseDTO dto = new CreatedProductVariantImageResponseDTO();
        dto.setImageId(image.getId());

        ProductVariant variant = image.getVariant();
        if (variant != null) {
            dto.setVariantId(variant.getId());

            if (variant.getProduct() != null) {
                dto.setProductId(variant.getProduct().getId());
            }
        }

        dto.setImage(image.getImage());
        dto.setMessage("Image created successfully.");

        return dto;
    }

    // ---------------- DETAIL RESPONSE ----------------
    public ProductVariantImageResponseDTO toResponseDTO(ProductVariantImage image) {
        if (image == null) return null;

        ProductVariantImageResponseDTO dto = new ProductVariantImageResponseDTO();
        dto.setImageId(image.getId());
        dto.setVariantId(image.getVariant().getId());
        dto.setVariantName(image.getVariant().getVariantName());
        dto.setProductId(image.getVariant().getProduct().getId());
        dto.setProductName(image.getVariant().getProduct().getName());
        dto.setImage(image.getImage());
        dto.setSortOrder(image.getSortOrder());
        dto.setAltText(image.getAltText());
        dto.setActive(image.isActive());
        dto.setDeleted(image.isDeleted());
        return dto;
    }

    public List<ProductVariantImageResponseDTO> toResponseDTOs(List<ProductVariantImage> images) {
        List<ProductVariantImageResponseDTO> list = new ArrayList<>();
        if (images == null) return list;

        for (ProductVariantImage img : images) {
            list.add(toResponseDTO(img));
        }
        return list;
    }

    // ---------------- LIST RESPONSE ----------------
    public ProductVariantImageListResponseDTO toListDTO(ProductVariantImage image) {
        if (image == null) return null;

        ProductVariantImageListResponseDTO dto = new ProductVariantImageListResponseDTO();
        dto.setImageId(image.getId());
        dto.setImage(image.getImage());
        dto.setAltText(image.getAltText());
        dto.setSortOrder(image.getSortOrder());
        dto.setActive(image.isActive());
        dto.setDeleted(image.isDeleted());

        return dto;
    }

    public List<ProductVariantImageListResponseDTO> toListDTOs(List<ProductVariantImage> images) {
        List<ProductVariantImageListResponseDTO> list = new ArrayList<>();
        if (images == null) return list;

        for (ProductVariantImage img : images) {
            list.add(toListDTO(img));
        }
        return list;
    }

}




//@Mapper(componentModel = "spring")
//public interface ProductVariantImageMapper {
//
//    // -------------- CREATE: DTO -> ENTITY --------------
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "variant", ignore = true) // set in service
//    @Mapping(target = "isActive", constant = "true")
//    @Mapping(target = "isDeleted", constant = "false")
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    ProductVariantImage toEntity(CreateProductVariantImageRequestDTO dto);
//
//
//    // -------------- AFTER CREATE RESPONSE --------------
//    @Mapping(source = "id", target = "imageId")
//    @Mapping(source = "variant.id", target = "variantId")
//    @Mapping(source = "variant.product.id", target = "productId")
//    @Mapping(source = "image", target = "image")
//    @Mapping(target = "message", constant = "Image created successfully.")
//    CreatedProductVariantImageResponseDTO toCreatedResponseDTO(ProductVariantImage image);
//
//
//    // -------------- FULL IMAGE RESPONSE (DETAIL VIEW) --------------
//    @Named("toImageResponseDTO")
//    @Mapping(source = "id", target = "imageId")
//    @Mapping(source = "image", target = "image")
//    @Mapping(source = "sortOrder", target = "sortOrder")
//    @Mapping(source = "altText", target = "altText")
//    @Mapping(source = "isActive", target = "isActive")
//    ProductVariantImageResponseDTO toResponseDTO(ProductVariantImage image);
//
//    @Named("toImageResponseDTOs")
//    List<ProductVariantImageResponseDTO> toResponseDTOs(List<ProductVariantImage> images);
//
//
//    @Mapping(source = "id", target = "imageId")
//    @Mapping(source = "image", target = "image")
//    @Mapping(source = "altText", target = "altText")
//    @Mapping(source = "sortOrder", target = "sortOrder")
//    @Mapping(source = "isActive", target = "isActive")
//    ProductVariantImageListResponseDTO toListDTO(ProductVariantImage image);
//
//    List<ProductVariantImageListResponseDTO> toListDTOs(List<ProductVariantImage> images);
//
//}


