package com.shop.productservice.service.productService;

import com.shop.productservice.DTOs.ProductDTOs.RequestDTOs.CreateProductRequestDTO;
import com.shop.productservice.DTOs.ProductDTOs.RequestDTOs.UpdateProductRequestDTO;
import com.shop.productservice.DTOs.ProductDTOs.ResponseDTOs.*;
import com.shop.productservice.enums.AgeGroup;
import com.shop.productservice.enums.Gender;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    // ---------- CREATE ----------
    CreatedProductResponseDTO createProduct(CreateProductRequestDTO dto); // for admin only

    // ---------------- UPDATE ----------------
    UpdateProductResponseDTO updateProduct(UpdateProductRequestDTO dto);// for admin only

    // ---------------- PUBLISH/UNPUBLISH ----------------
    String publishProduct(Long productId); // for admin only

    String unpublishProduct(Long productId); // for admin only

    // ---------- GET DETAILS for Admin use only ----------
    ProductResponseDTO getProductById(Long productId); // for admin only, can get active/inactive product

    ProductResponseDTO getProductByAsin(String asin); // for admin only, can get active/inactive product

    ProductResponseDTO getDeletedProductById(Long productId); // for admin only, can get deleted product

    Page<DeletedProductListResponseDTO> getAllDeletedProducts(Pageable pageable); // for admin only, paginated deleted product list

    Page<ProductListResponseDTO> getAllProducts(Pageable pageable); // for admin only, paginated list of all products active/inactive

    Page<ProductListResponseDTO> getAllPublishedProducts(Pageable pageable); // for admin only, paginated list of all published products

    Page<ProductListResponseDTO> getAllReadyToPublishProducts(Pageable pageable);

    // ---------------- LIST of products for customer ----------------

//    List<ProductListResponseDTO> getPublishedProductsForCustomer(); // publish product for customer

    List<ProductListResponseDTO> getRecentlyPublishedProducts(int limit); // publish product for customer

    // ---------- PAGINATED CUSTOMER ENDPOINTS ----------


    Page<ProductListResponseDTO> getRecentlyPublishedProducts(Pageable pageable); // paginated

    Page<ProductListResponseDTO> searchByName(String name, Pageable pageable); // paginated

    Page<ProductListResponseDTO> getByCategory(String category, Pageable pageable); // paginated

    Page<ProductListResponseDTO> getByCategoryAndSubCategory(String category, String subCategory, Pageable pageable); // paginated

    Page<ProductListResponseDTO> getByBrand(String brand, Pageable pageable); // paginated

    Page<ProductListResponseDTO> getByGender(Gender gender, Pageable pageable); // paginated

    Page<ProductListResponseDTO> getByAgeGroup(AgeGroup ageGroup, Pageable pageable); // paginated

    Page<ProductListResponseDTO> searchByTag(String tag, Pageable pageable); // paginated

    Page<ProductListResponseDTO> filterProducts(String category, String subCategory, Gender gender, String tag, Pageable pageable); // paginated

    Page<ProductListResponseDTO> getProductsByPriceRange(BigDecimal min, BigDecimal max, Pageable pageable); // paginated

    CustomerProductResponseDTO getActiveProductByAsin(String asin); // for customer only, can get active product
    // ---------- STATUS / LIFECYCLE HANDEL BY ADMIN ONLY ----------
    String deactivateProduct(Long productId);

    String activateProduct(Long productId);//reactivate a previously deactivated product

    String deleteProduct(Long productId); // soft delete

    String restoreDeletedProduct(Long productId); // restore soft delete

    // ---------- SEARCH can be only on published products----------
    List<ProductListResponseDTO> searchByName(String name);

    List<ProductListResponseDTO> getByCategory(String category);

    List<ProductListResponseDTO> getByBrand(String brand);

    List<ProductListResponseDTO> getByCategoryAndSubCategory(String category, String subCategory);

    List<ProductListResponseDTO> getByGender(Gender gender);

    List<ProductListResponseDTO> getByAgeGroup(AgeGroup ageGroup);

    List<ProductListResponseDTO> searchByTag(String tag);

    List<ProductListResponseDTO> filterProducts(String category, String subCategory, Gender gender, String tag);

    List<ProductListResponseDTO> getProductsByPriceRange(BigDecimal min, BigDecimal max);

    List<ProductListResponseDTO> getRelatedProducts(Long productId);

}
