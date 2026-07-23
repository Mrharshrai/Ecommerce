package com.shop.productservice.controller.basicController.AdminProductSetupController.WareHouseSetup;

import com.shop.productservice.DTOs.WarehouseDTOs.RequestDTOs.CreateWarehouseRequest;
import com.shop.productservice.DTOs.WarehouseDTOs.RequestDTOs.UpdateWarehouseInventoryRequest;
import com.shop.productservice.DTOs.WarehouseDTOs.RequestDTOs.UpdateWarehouseRequest;
import com.shop.productservice.DTOs.WarehouseDTOs.ResponseDTOs.WarehouseInventoryResponse;
import com.shop.productservice.DTOs.WarehouseDTOs.ResponseDTOs.WarehouseResponse;
import com.shop.productservice.service.warehouse.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.shop.productservice.service.activityService.ProductActivityService;
import com.shop.productservice.enums.ProductActivityType;

/**
 * Admin controller for warehouse and inventory management.
 * All endpoints require ROLE_ADMIN authentication.
 * Base path: /api/adminProduct/warehouses
 */
@RestController
@RequestMapping("/api/adminProduct/warehouses")
@PreAuthorize("hasRole('ADMIN')")
@Validated
@RequiredArgsConstructor
public class AdminWarehouseController {

    private final WarehouseService warehouseService;
    private final ProductActivityService activityService;
    
    private void logActivity(Long productId, ProductActivityType type, String description, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth != null ? auth.getName() : "system";
        
        String userRole = "ROLE_ADMIN";
        if (auth != null && !auth.getAuthorities().isEmpty()) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            userRole = isAdmin ? "ROLE_ADMIN" : auth.getAuthorities().iterator().next().getAuthority();
        }

        activityService.logActivity(userEmail, userRole, productId, type, description, request);
    }

    // ======================== WAREHOUSE CRUD ========================

    /**
     * Create a new warehouse.
     * POST /api/adminProduct/warehouses
     */
    @PostMapping
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest requestPayload,
            HttpServletRequest request) {
        WarehouseResponse response = warehouseService.createWarehouse(requestPayload);
        logActivity(null, ProductActivityType.WAREHOUSE_CREATED, "Created warehouse " + response.getWarehouseCode(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing warehouse.
     * PUT /api/adminProduct/warehouses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponse> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWarehouseRequest requestPayload,
            HttpServletRequest request) {
        WarehouseResponse response = warehouseService.updateWarehouse(id, requestPayload);
        logActivity(null, ProductActivityType.WAREHOUSE_UPDATED, "Updated warehouse with ID " + id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft-delete a warehouse.
     * DELETE /api/adminProduct/warehouses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteWarehouse(@PathVariable("id") Long id, HttpServletRequest request) {
        warehouseService.deleteWarehouse(id);
        logActivity(null, ProductActivityType.WAREHOUSE_DELETED, "Deleted warehouse with ID " + id, request);
        return ResponseEntity.ok(Map.of("message", "Warehouse deleted successfully"));
    }

    /**
     * Restore Soft-deleted a warehouse.
     * PUT /api/adminProduct/warehouses/{id}
     */
    @PutMapping("/{id}/restore")
    public ResponseEntity<Map<String, String>> restoreWarehouse(@PathVariable("id") Long id, HttpServletRequest request) {
        warehouseService.restoreWarehouse(id);
        logActivity(null, ProductActivityType.WAREHOUSE_DELETED, "Restored warehouse with ID " + id, request);
        return ResponseEntity.ok(Map.of("message", "Warehouse restored successfully, Make sure to activate before assigning this"));
    }

    /**
     * Get warehouse by ID.
     * GET /api/adminProduct/warehouses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    /**
     * Get warehouse by code.
     * GET /api/adminProduct/warehouses/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<WarehouseResponse> getWarehouseByCode(@PathVariable String code) {
        return ResponseEntity.ok(warehouseService.getWarehouseByCode(code.trim().toUpperCase()));
    }

    /**
     * Get all warehouses (including inactive, excluding deleted).
     * GET /api/adminProduct/warehouses
     */
    @GetMapping
    public ResponseEntity<Page<WarehouseResponse>> getAllWarehouses(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(warehouseService.getAllWarehouses(pageable));
    }

    /**
     * Get all active warehouses.
     * GET /api/adminProduct/warehouses/active
     */
    @GetMapping("/active")
    public ResponseEntity<Page<WarehouseResponse>> getActiveWarehouses(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(warehouseService.getActiveWarehouses(pageable));
    }

    /**
     * Get the default warehouse.
     * GET /api/adminProduct/warehouses/default
     */
    @GetMapping("/default")
    public ResponseEntity<WarehouseResponse> getDefaultWarehouse() {
        return ResponseEntity.ok(warehouseService.getDefaultWarehouse());
    }

    /**
     * Activate a warehouse.
     * PUT /api/adminProduct/warehouses/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<WarehouseResponse> activateWarehouse(@PathVariable Long id, HttpServletRequest request) {
        WarehouseResponse response = warehouseService.activateWarehouse(id);
        logActivity(null, ProductActivityType.WAREHOUSE_UPDATED, "Activated warehouse with ID " + id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a warehouse.
     * PUT /api/adminProduct/warehouses/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<WarehouseResponse> deactivateWarehouse(@PathVariable Long id, HttpServletRequest request) {
        WarehouseResponse response = warehouseService.deactivateWarehouse(id);
        logActivity(null, ProductActivityType.WAREHOUSE_UPDATED, "Deactivated warehouse with ID " + id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Set a warehouse as the default.
     * PUT /api/adminProduct/warehouses/{id}/set-default
     */
    @PutMapping("/{id}/set-default")
    public ResponseEntity<WarehouseResponse> setDefaultWarehouse(@PathVariable Long id, HttpServletRequest request) {
        WarehouseResponse response = warehouseService.setDefaultWarehouse(id);
        logActivity(null, ProductActivityType.WAREHOUSE_UPDATED, "Set warehouse with ID " + id + " as default", request);
        return ResponseEntity.ok(response);
    }

    // ======================== INVENTORY MANAGEMENT ========================

    /**
     * Add stock record for a warehouse + variantSize combination.
     * POST /api/adminProduct/warehouses/inventory
     */
    @PostMapping("/inventory")
    public ResponseEntity<WarehouseInventoryResponse> addInventory(
            @Valid @RequestBody UpdateWarehouseInventoryRequest requestPayload,
            HttpServletRequest request) {
        WarehouseInventoryResponse response = warehouseService.addInventory(requestPayload);
        logActivity(null, ProductActivityType.INVENTORY_ADDED, "Added inventory for variant size " + requestPayload.getVariantSizeId() + " to warehouse " + requestPayload.getWarehouseId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Overwrite quantity for an inventory record.
     * PUT /api/adminProduct/warehouses/inventory/{inventoryId}?quantity={qty}
     */
    @PutMapping("/inventory/{inventoryId}")
    public ResponseEntity<WarehouseInventoryResponse> updateInventory(
            @PathVariable Long inventoryId,
            @RequestParam @NotNull @Min(0) Integer quantity,
            HttpServletRequest request) {
        WarehouseInventoryResponse response = warehouseService.updateInventory(inventoryId, quantity);
        logActivity(null, ProductActivityType.INVENTORY_UPDATED, "Updated quantity to " + quantity + " for inventory ID " + inventoryId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an inventory record.
     * DELETE /api/adminProduct/warehouses/inventory/{inventoryId}
     */
    @DeleteMapping("/inventory/{inventoryId}")
    public ResponseEntity<Map<String, String>> deleteInventory(@PathVariable Long inventoryId, HttpServletRequest request) {
        warehouseService.deleteInventory(inventoryId);
        logActivity(null, ProductActivityType.INVENTORY_DELETED, "Deleted inventory record with ID " + inventoryId, request);
        return ResponseEntity.ok(Map.of("message", "Inventory record deleted successfully"));
    }

    /**
     * Get all inventory for a warehouse.
     * GET /api/adminProduct/warehouses/{warehouseId}/inventory
     */
    @GetMapping("/{warehouseId}/inventory")
    public ResponseEntity<Page<WarehouseInventoryResponse>> getInventoryByWarehouse(
            @PathVariable Long warehouseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(warehouseService.getInventoryByWarehouse(warehouseId, pageable));
    }

    /**
     * Get all warehouses holding a specific variant size.
     * GET /api/adminProduct/warehouses/inventory/variant-size/{variantSizeId}
     */
    @GetMapping("/inventory/variant-size/{variantSizeId}")
    public ResponseEntity<Page<WarehouseInventoryResponse>> getInventoryByVariantSize(
            @PathVariable Long variantSizeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(warehouseService.getInventoryByVariantSize(variantSizeId, pageable));
    }

    /**
     * Get best available warehouse for a sizeSku (highest available stock first).
     * GET /api/adminProduct/warehouses/inventory/available?sizeSku=SKU-001-M
     */
    @GetMapping("/inventory/available")
    public ResponseEntity<WarehouseInventoryResponse> getAvailableWarehouseForSize(@RequestParam @NotBlank String sizeSku) {
        return ResponseEntity.ok(warehouseService.getAvailableWarehouseForSize(sizeSku));
    }

    /**
     * Get all warehouses with available stock for a sizeSku.
     * GET /api/adminProduct/warehouses/inventory/available/all?sizeSku=SKU-001-M
     */
    @GetMapping("/inventory/available/all")
    public ResponseEntity<Page<WarehouseInventoryResponse>> getAllAvailableWarehousesForSize(
            @RequestParam @NotBlank String sizeSku,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(warehouseService.getAllAvailableWarehousesForSize(sizeSku, pageable));
    }

    /**
     * Activate an inventory record.
     * PUT /api/adminProduct/warehouses/inventory/{inventoryId}/activate
     */
    @PutMapping("/inventory/{inventoryId}/activate")
    public ResponseEntity<WarehouseInventoryResponse> activateInventory(@PathVariable Long inventoryId, HttpServletRequest request) {
        WarehouseInventoryResponse response = warehouseService.activateInventory(inventoryId);
        logActivity(null, ProductActivityType.INVENTORY_UPDATED, "Activated inventory record with ID " + inventoryId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate an inventory record.
     * PUT /api/adminProduct/warehouses/inventory/{inventoryId}/deactivate
     */
    @PutMapping("/inventory/{inventoryId}/deactivate")
    public ResponseEntity<WarehouseInventoryResponse> deactivateInventory(@PathVariable Long inventoryId, HttpServletRequest request) {
        WarehouseInventoryResponse response = warehouseService.deactivateInventory(inventoryId);
        logActivity(null, ProductActivityType.INVENTORY_UPDATED, "Deactivated inventory record with ID " + inventoryId, request);
        return ResponseEntity.ok(response);
    }

    // ======================== STOCK OPERATIONS ========================

    /**
     * Add stock to an existing inventory record.
     * PUT /api/adminProduct/warehouses/inventory/{inventoryId}/add-stock?quantity=50
     */
    @PutMapping("/inventory/{inventoryId}/add-stock")
    public ResponseEntity<WarehouseInventoryResponse> addStock(
            @PathVariable Long inventoryId,
            @RequestParam @NotNull @Min(1) Integer quantity,
            HttpServletRequest request) {
        WarehouseInventoryResponse response = warehouseService.addStock(inventoryId, quantity);
        logActivity(null, ProductActivityType.STOCK_UPDATED, "Added " + quantity + " to stock for inventory ID " + inventoryId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deduct stock from an inventory record (admin manual correction).
     * PUT /api/adminProduct/warehouses/inventory/{inventoryId}/deduct-stock?quantity=5
     */
    @PutMapping("/inventory/{inventoryId}/deduct-stock")
    public ResponseEntity<WarehouseInventoryResponse> deductStock(
            @PathVariable Long inventoryId,
            @RequestParam @NotNull @Min(1) Integer quantity,
            HttpServletRequest request) {
        WarehouseInventoryResponse response = warehouseService.deductStock(inventoryId, quantity);
        logActivity(null, ProductActivityType.STOCK_UPDATED, "Deducted " + quantity + " from stock for inventory ID " + inventoryId, request);
        return ResponseEntity.ok(response);
    }
}
