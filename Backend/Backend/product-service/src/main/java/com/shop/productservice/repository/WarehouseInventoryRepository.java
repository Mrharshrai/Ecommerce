package com.shop.productservice.repository;

import com.shop.productservice.entity.WarehouseInventory;
import com.shop.productservice.entity.Warehouse;
import com.shop.productservice.entity.ProductVariantSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Long> {

    Optional<WarehouseInventory> findByWarehouseAndVariantSizeAndIsDeletedFalse(Warehouse warehouse, ProductVariantSize variantSize);

    List<WarehouseInventory> findByVariantSizeAndIsDeletedFalse(ProductVariantSize variantSize);
    Page<WarehouseInventory> findByVariantSizeAndIsDeletedFalse(ProductVariantSize variantSize, Pageable pageable);

    List<WarehouseInventory> findByWarehouseAndIsDeletedFalse(Warehouse warehouse);
    Page<WarehouseInventory> findByWarehouseAndIsDeletedFalse(Warehouse warehouse, Pageable pageable);

    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.variantSize = :variantSize AND wi.quantity > wi.reservedQuantity AND wi.isDeleted = false AND wi.isActive = true ORDER BY wi.quantity - wi.reservedQuantity DESC")
    List<WarehouseInventory> findAvailableInventoryByVariantSize(@Param("variantSize") ProductVariantSize variantSize);

    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.variantSize.sizeSku = :sizeSku AND wi.quantity > wi.reservedQuantity AND wi.isDeleted = false AND wi.isActive = true ORDER BY wi.quantity - wi.reservedQuantity DESC")
    List<WarehouseInventory> findAvailableInventoryBySizeSku(@Param("sizeSku") String sizeSku);

    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.variantSize.sizeSku = :sizeSku AND wi.quantity > wi.reservedQuantity AND wi.isDeleted = false AND wi.isActive = true")
    Page<WarehouseInventory> findAvailableInventoryBySizeSku(@Param("sizeSku") String sizeSku, Pageable pageable);

    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.variantSize.id = :variantSizeId AND wi.quantity > wi.reservedQuantity AND wi.isDeleted = false AND wi.isActive = true ORDER BY wi.quantity - wi.reservedQuantity DESC")
    List<WarehouseInventory> findAvailableInventoryByVariantSizeId(@Param("variantSizeId") Long variantSizeId);

    @Query("SELECT SUM(wi.quantity - wi.reservedQuantity) FROM WarehouseInventory wi WHERE wi.variantSize = :variantSize AND wi.isDeleted = false")
    Integer getTotalAvailableQuantityByVariantSize(@Param("variantSize") ProductVariantSize variantSize);

    boolean existsByWarehouseAndVariantSize(Warehouse warehouse, ProductVariantSize variantSize);

    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId AND wi.variantSize.variant.id = :variantId AND wi.isDeleted = false")
    List<WarehouseInventory> findByWarehouseIdAndVariantId(@Param("warehouseId") Long warehouseId, @Param("variantId") Long variantId);
}
