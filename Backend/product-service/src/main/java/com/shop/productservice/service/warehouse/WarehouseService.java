package com.shop.productservice.service.warehouse;

import com.shop.productservice.DTOs.WarehouseDTOs.RequestDTOs.CreateWarehouseRequest;
import com.shop.productservice.DTOs.WarehouseDTOs.RequestDTOs.UpdateWarehouseInventoryRequest;
import com.shop.productservice.DTOs.WarehouseDTOs.RequestDTOs.UpdateWarehouseRequest;
import com.shop.productservice.DTOs.WarehouseDTOs.ResponseDTOs.WarehouseInventoryResponse;
import com.shop.productservice.DTOs.WarehouseDTOs.ResponseDTOs.WarehouseResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarehouseService {

    // ===== WAREHOUSE CRUD =====
    WarehouseResponse createWarehouse(CreateWarehouseRequest request);
    WarehouseResponse updateWarehouse(Long id, UpdateWarehouseRequest request);
    void deleteWarehouse(Long id);
    void restoreWarehouse(Long id);
    WarehouseResponse getWarehouseById(Long id);
    WarehouseResponse getWarehouseByCode(String warehouseCode);
    Page<WarehouseResponse> getAllWarehouses(Pageable pageable);
    Page<WarehouseResponse> getActiveWarehouses(Pageable pageable);
    WarehouseResponse getDefaultWarehouse();
    WarehouseResponse activateWarehouse(Long id);
    WarehouseResponse deactivateWarehouse(Long id);
    WarehouseResponse setDefaultWarehouse(Long id);

    // ===== INVENTORY CRUD =====
    WarehouseInventoryResponse addInventory(UpdateWarehouseInventoryRequest request);
    WarehouseInventoryResponse updateInventory(Long inventoryId, Integer quantity);
    void deleteInventory(Long inventoryId);
    Page<WarehouseInventoryResponse> getInventoryByWarehouse(Long warehouseId, Pageable pageable);
    Page<WarehouseInventoryResponse> getInventoryByVariantSize(Long variantSizeId, Pageable pageable);
    WarehouseInventoryResponse getAvailableWarehouseForSize(String sizeSku);
    Page<WarehouseInventoryResponse> getAllAvailableWarehousesForSize(String sizeSku, Pageable pageable);
    WarehouseInventoryResponse activateInventory(Long inventoryId);
    WarehouseInventoryResponse deactivateInventory(Long inventoryId);

    // ===== STOCK OPERATIONS =====
    WarehouseInventoryResponse addStock(Long inventoryId, Integer quantity);
    WarehouseInventoryResponse deductStock(Long inventoryId, Integer quantity);
}
