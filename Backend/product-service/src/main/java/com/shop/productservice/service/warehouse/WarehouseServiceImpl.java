package com.shop.productservice.service.warehouse;

import com.shop.productservice.DTOs.WarehouseDTOs.RequestDTOs.CreateWarehouseRequest;
import com.shop.productservice.DTOs.WarehouseDTOs.RequestDTOs.UpdateWarehouseInventoryRequest;
import com.shop.productservice.DTOs.WarehouseDTOs.RequestDTOs.UpdateWarehouseRequest;
import com.shop.productservice.DTOs.WarehouseDTOs.ResponseDTOs.WarehouseInventoryResponse;
import com.shop.productservice.DTOs.WarehouseDTOs.ResponseDTOs.WarehouseResponse;
import com.shop.productservice.entity.ProductVariantSize;
import com.shop.productservice.entity.Warehouse;
import com.shop.productservice.entity.WarehouseInventory;
import com.shop.productservice.repository.ProductVariantSizeRepository;
import com.shop.productservice.repository.WarehouseInventoryRepository;
import com.shop.productservice.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WarehouseServiceImpl - Business logic for multi-warehouse stock management.
 *
 * DESIGN NOTES:
 * - Warehouses and inventory are stored in separate tables (warehouses, warehouse_inventory).
 * - Existing product/variant/size tables are NOT modified.
 * - product_variant_sizes.quantity is always kept in sync with the total warehouse stock
 *   so that existing cart/order services continue to read correct stock without any changes.
 * - Only ONE warehouse can be marked as "default" at a time.
 * - Soft-delete is used for both warehouses and inventory records (@SQLDelete).
 *
 * STOCK DEDUCTION FLOW (when an order is placed):
 *   1. Call getAvailableWarehouseForSize(sizeSku) to find the warehouse with the most available stock.
 *   2. Call deductStock(inventoryId, quantity) to reduce stock there.
 *   3. product_variant_sizes.quantity is automatically decreased in the same transaction.
 */
@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseInventoryRepository warehouseInventoryRepository;

    // Used to keep product_variant_sizes.quantity in sync with warehouse stock
    @Autowired
    private ProductVariantSizeRepository variantSizeRepository;


    // ======================================================================
    //                        WAREHOUSE CRUD
    // ======================================================================

    /**
     * Create a new warehouse.
     * - Validates that warehouseCode is unique across all warehouses.
     * - If isDefault=true, the existing default warehouse is automatically unset.
     */
    @Override
    @Transactional
    public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {

        String normalize=request.getWarehouseCode().trim().toUpperCase();

        // Prevent duplicate warehouse codes present as active/inaactive/deleted
        Long count =
                warehouseRepository.existsByWarehouseCodeIncludingDeleted(normalize);

        if (count > 0) {
            throw new RuntimeException(
                    "Warehouse with code '" + request.getWarehouseCode() + "' already exists in state Active/Deleted");
        }

        // duplicate check for email and mobile in future

        // If this warehouse is being set as default, unset the current default first
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            warehouseRepository.findByIsDefaultTrueAndIsDeletedFalse().ifPresent(existing -> {
                existing.setDefault(false);
                warehouseRepository.save(existing);
            });
        }

        Warehouse warehouse = Warehouse.builder()
                .warehouseCode(request.getWarehouseCode().trim().toUpperCase())
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry())
                .contactNumber(request.getContactNumber())
                .email(request.getEmail())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(true)
                .isDeleted(false)
                .build();

        warehouse = warehouseRepository.save(warehouse);
        return toWarehouseResponse(warehouse);
    }

    /**
     * Update an existing warehouse's details.
     * - Partial update: only fields provided in the request are changed.
     * - If warehouseCode is changed, uniqueness is validated (excluding the warehouse itself).
     * - If isDefault=true, the previous default warehouse is automatically unset.
     */
    @Override
    @Transactional
    public WarehouseResponse updateWarehouse(Long id, UpdateWarehouseRequest request) {
        // only active/inactive
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + id));

        // Handle default switch: unset old default, set this one as new default
        if (Boolean.TRUE.equals(request.getIsDefault()) && !warehouse.isDefault()) {
            warehouseRepository.findByIsDefaultTrueAndIsDeletedFalse().ifPresent(existing -> {
                existing.setDefault(false);
                warehouseRepository.save(existing);
            });
            warehouse.setDefault(true);
        }

        // Apply only provided fields (partial update pattern)
//        if (request.getWarehouseCode() != null) warehouse.setWarehouseCode(request.getWarehouseCode());
        if (request.getName() != null) warehouse.setName(request.getName());
        if (request.getAddress() != null) warehouse.setAddress(request.getAddress());
        if (request.getCity() != null) warehouse.setCity(request.getCity());
        if (request.getState() != null) warehouse.setState(request.getState());
        if (request.getPincode() != null) warehouse.setPincode(request.getPincode());
        if (request.getCountry() != null) warehouse.setCountry(request.getCountry());
        if (request.getContactNumber() != null) warehouse.setContactNumber(request.getContactNumber());
        if (request.getEmail() != null) warehouse.setEmail(request.getEmail());

        warehouse = warehouseRepository.save(warehouse);
        return toWarehouseResponse(warehouse);
    }

    /**
     * Soft-delete a warehouse (sets is_deleted=true via @SQLDelete).
     * - The default warehouse cannot be deleted. Set another as default first.
     */
    @Override
    @Transactional
    public void deleteWarehouse(Long id) {

        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + id));

        // Prevent accidental deletion of the default warehouse
        if (warehouse.isDefault()) {
            throw new RuntimeException("Cannot delete the default warehouse. Set another warehouse as default first.");
        }

        // @SQLDelete annotation ensures this triggers: UPDATE warehouses SET is_deleted=true WHERE id=?
        warehouse.setDeleted(true);
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }

    /**
     * Soft-delete a warehouse (sets is_deleted=true via @SQLDelete).
     * - The default warehouse cannot be deleted. Set another as default first.
     */
    @Override
    @Transactional
    public void restoreWarehouse(Long id) {
        // check only in deleted warehouses
        Warehouse deletedWarehouse = warehouseRepository.findDeletedById(id)
                .orElseThrow(() -> new RuntimeException("No soft-deleted warehouse found with ID: " + id));
        deletedWarehouse.setDeleted(false);
        warehouseRepository.save(deletedWarehouse);
    }

    /**
     * Fetch a warehouse by its database ID.
     */
    @Override
    public WarehouseResponse getWarehouseById(Long id) {
        // only active/inactive
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + id));
        return toWarehouseResponse(warehouse);
    }

    /**
     * Fetch a warehouse using its unique business code (e.g., "WH-MUM-01").
     */
    @Override
    public WarehouseResponse getWarehouseByCode(String warehouseCode) {
        // only active/inactive
        Warehouse warehouse = warehouseRepository.findByWarehouseCodeAndIsDeletedFalse(warehouseCode)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with code: " + warehouseCode));
        return toWarehouseResponse(warehouse);
    }

    /**
     * Return all non-deleted warehouses (includes both active and inactive).
     */
    @Override
    public Page<WarehouseResponse> getAllWarehouses(Pageable pageable) {
        // only active/inactive
        return warehouseRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toWarehouseResponse);
    }

    /**
     * Return only warehouses that are currently active (isActive=true).
     * These are the warehouses eligible for stock operations.
     */
    @Override
    public Page<WarehouseResponse> getActiveWarehouses(Pageable pageable) {
        // only active
        return warehouseRepository.findAllByIsActiveTrueAndIsDeletedFalse(pageable)
                .map(this::toWarehouseResponse);
    }

    /**
     * Return the warehouse currently marked as default.
     * The default warehouse is used when no specific warehouse is targeted.
     */
    @Override
    public WarehouseResponse getDefaultWarehouse() {
        // only active/inactive
        Warehouse warehouse = warehouseRepository.findByIsDefaultTrueAndIsDeletedFalse()
                .orElseThrow(() -> new RuntimeException("No default warehouse configured"));
        return toWarehouseResponse(warehouse);
    }

    /**
     * Mark a warehouse as active (re-enable it for stock operations).
     */
    @Override
    @Transactional
    public WarehouseResponse activateWarehouse(Long id) {
        // only active/inactive
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + id));
        if(warehouse.isActive()){
            throw new RuntimeException("This warehouse as already active.");
        }
        warehouse.setActive(true);
        return toWarehouseResponse(warehouseRepository.save(warehouse));
    }

    /**
     * Mark a warehouse as inactive (temporarily disable it without deleting).
     * - The default warehouse cannot be deactivated directly.
     */
    @Override
    @Transactional
    public WarehouseResponse deactivateWarehouse(Long id) {
        // only active/inactive
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + id));
        if(!warehouse.isActive()){
            throw new RuntimeException("This warehouse as already inactive.");
        }

        if (warehouse.isDefault()) {
            throw new RuntimeException("Cannot deactivate the default warehouse. Change the default first.");
        }

        warehouse.setActive(false);
        return toWarehouseResponse(warehouseRepository.save(warehouse));
    }

    /**
     * Promote a warehouse to be the default.
     * - Automatically unsets the current default.
     * - Only active warehouses can be set as default.
     */
    @Override
    @Transactional
    public WarehouseResponse setDefaultWarehouse(Long id) {
        // only active/inactive
        Warehouse newDefault = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + id));

        // Only active warehouses can be made the default
        if (!newDefault.isActive()) {
            throw new RuntimeException("Cannot set an inactive warehouse as default. Activate it first.");
        }

        // Remove default flag from the current default warehouse
        warehouseRepository.findByIsDefaultTrueAndIsDeletedFalse().ifPresent(existing -> {
            existing.setDefault(false);
            warehouseRepository.save(existing);
        });

        newDefault.setDefault(true);
        return toWarehouseResponse(warehouseRepository.save(newDefault));
    }


    // ======================================================================
    //                      INVENTORY MANAGEMENT
    // ======================================================================

    /**
     * Create a new inventory record linking a warehouse to a product variant size.
     *
     * Each unique (warehouse, variantSize) pair can only have ONE inventory record.
     * Use updateInventory / addStock / deductStock for subsequent changes.
     *
     * IMPORTANT: product_variant_sizes.quantity is increased by the initial quantity
     * so that the existing stock display across the app remains accurate.
     */
    @Override
    @Transactional
    public WarehouseInventoryResponse addInventory(UpdateWarehouseInventoryRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + request.getWarehouseId()));

        ProductVariantSize variantSize = variantSizeRepository.findById(request.getVariantSizeId())
                .orElseThrow(() -> new RuntimeException("ProductVariantSize not found with ID: " + request.getVariantSizeId()));

        // Prevent duplicate inventory records for the same warehouse + size combination
        if (warehouseInventoryRepository.existsByWarehouseAndVariantSize(warehouse, variantSize)) {
            throw new RuntimeException("Inventory record already exists for this warehouse and variant size. Use addStock or updateInventory instead.");
        }

        WarehouseInventory inventory = WarehouseInventory.builder()
                .warehouse(warehouse)
                .variantSize(variantSize)
                .quantity(request.getQuantity())
                .reservedQuantity(0) // no reservations on a new record
                .isActive(true)
                .isDeleted(false)
                .build();

        // Keep product_variant_sizes.quantity in sync across all warehouses
        variantSize.setQuantity(variantSize.getQuantity() + request.getQuantity());
        variantSizeRepository.save(variantSize);

        inventory = warehouseInventoryRepository.save(inventory);
        return toInventoryResponse(inventory);
    }

    /**
     * Overwrite the total quantity of an inventory record (admin correction / stock count).
     *
     * The difference (delta) between old and new quantity is applied to
     * product_variant_sizes.quantity to keep everything consistent.
     *
     * - New quantity cannot be less than what is already reserved.
     */
    @Override
    @Transactional
    public WarehouseInventoryResponse updateInventory(Long inventoryId, Integer quantity) {
        WarehouseInventory inventory = warehouseInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory record not found with ID: " + inventoryId));

        // Cannot set quantity below what is already reserved for pending orders
        if (quantity < inventory.getReservedQuantity()) {
            throw new RuntimeException("New quantity (" + quantity + ") cannot be less than reserved quantity ("
                    + inventory.getReservedQuantity() + ")");
        }

        int oldQty = inventory.getQuantity();
        inventory.setQuantity(quantity);

        // Apply the delta to the size-level quantity (may be positive or negative)
        ProductVariantSize variantSize = inventory.getVariantSize();
        int delta = quantity - oldQty;
        int newSizeQty = Math.max(0, variantSize.getQuantity() + delta);
        variantSize.setQuantity(newSizeQty);
        variantSizeRepository.save(variantSize);

        inventory = warehouseInventoryRepository.save(inventory);
        return toInventoryResponse(inventory);
    }

    /**
     * Soft-delete an inventory record.
     *
     * - Cannot be deleted if there is any reserved stock (reserved by active orders).
     * - product_variant_sizes.quantity is decreased by the inventory quantity being removed.
     */
    @Override
    @Transactional
    public void deleteInventory(Long inventoryId) {
        WarehouseInventory inventory = warehouseInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory record not found with ID: " + inventoryId));

        // Block deletion if there are pending reservations
        if (inventory.getReservedQuantity() > 0) {
            throw new RuntimeException("Cannot delete inventory with reserved stock. Reserved: "
                    + inventory.getReservedQuantity());
        }

        // Reduce the size-level quantity to stay in sync
        ProductVariantSize variantSize = inventory.getVariantSize();
        int newSizeQty = Math.max(0, variantSize.getQuantity() - inventory.getQuantity());
        variantSize.setQuantity(newSizeQty);
        variantSizeRepository.save(variantSize);

        // @SQLDelete triggers: UPDATE warehouse_inventory SET is_deleted=true WHERE id=?
        warehouseInventoryRepository.delete(inventory);
    }

    /**
     * Get all inventory records for a specific warehouse.
     * Useful for a full stock report of one warehouse.
     */
    @Override
    public Page<WarehouseInventoryResponse> getInventoryByWarehouse(Long warehouseId, Pageable pageable) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + warehouseId));
        return warehouseInventoryRepository.findByWarehouseAndIsDeletedFalse(warehouse, pageable)
                .map(this::toInventoryResponse);
    }

    /**
     * Get all warehouse inventory records for a specific product variant size.
     * Useful to see which warehouses stock a particular size.
     */
    @Override
    public Page<WarehouseInventoryResponse> getInventoryByVariantSize(Long variantSizeId, Pageable pageable) {
        ProductVariantSize variantSize = variantSizeRepository.findById(variantSizeId)
                .orElseThrow(() -> new RuntimeException("ProductVariantSize not found with ID: " + variantSizeId));
        return warehouseInventoryRepository.findByVariantSizeAndIsDeletedFalse(variantSize, pageable)
                .map(this::toInventoryResponse);
    }

    /**
     * Find the single BEST warehouse to fulfil an order for a given size SKU.
     * Returns the warehouse with the highest available (quantity - reservedQuantity) stock.
     *
     * Used by order placement logic to determine where to deduct stock from.
     */
    @Override
    public WarehouseInventoryResponse getAvailableWarehouseForSize(String sizeSku) {
        List<WarehouseInventory> results = warehouseInventoryRepository.findAvailableInventoryBySizeSku(sizeSku);
        if (results.isEmpty()) {
            throw new RuntimeException("No available inventory found for sizeSku: " + sizeSku);
        }
        // The query already orders by available quantity DESC, so index 0 is the best option
        return toInventoryResponse(results.get(0));
    }

    /**
     * Get ALL warehouses that have available stock for a given size SKU, ordered by highest stock first.
     */
    @Override
    public Page<WarehouseInventoryResponse> getAllAvailableWarehousesForSize(String sizeSku, Pageable pageable) {
        return warehouseInventoryRepository.findAvailableInventoryBySizeSku(sizeSku, pageable)
                .map(this::toInventoryResponse);
    }

    /**
     * Activate an inventory record (re-enable it for stock queries).
     */
    @Override
    @Transactional
    public WarehouseInventoryResponse activateInventory(Long inventoryId) {
        WarehouseInventory inventory = warehouseInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory record not found with ID: " + inventoryId));
        inventory.setActive(true);
        return toInventoryResponse(warehouseInventoryRepository.save(inventory));
    }

    /**
     * Deactivate an inventory record (exclude it from available stock queries without deleting).
     */
    @Override
    @Transactional
    public WarehouseInventoryResponse deactivateInventory(Long inventoryId) {
        WarehouseInventory inventory = warehouseInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory record not found with ID: " + inventoryId));
        inventory.setActive(false);
        return toInventoryResponse(warehouseInventoryRepository.save(inventory));
    }


    // ======================================================================
    //                       STOCK OPERATIONS
    // ======================================================================

    /**
     * Add stock to an existing inventory record (e.g., new shipment arrived at warehouse).
     * - Increases warehouse_inventory.quantity
     * - Increases product_variant_sizes.quantity by the same amount (keeps data in sync)
     */
    @Override
    @Transactional
    public WarehouseInventoryResponse addStock(Long inventoryId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity to add must be greater than 0");
        }

        WarehouseInventory inventory = warehouseInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory record not found with ID: " + inventoryId));

        // Increase warehouse stock
        inventory.setQuantity(inventory.getQuantity() + quantity);

        // Keep product_variant_sizes.quantity in sync
        ProductVariantSize variantSize = inventory.getVariantSize();
        variantSize.setQuantity(variantSize.getQuantity() + quantity);
        variantSizeRepository.save(variantSize);

        return toInventoryResponse(warehouseInventoryRepository.save(inventory));
    }

    /**
     * Deduct stock from a specific warehouse inventory record.
     * Called when an order is being fulfilled from this warehouse.
     *
     * - Checks available quantity (quantity - reservedQuantity) before deducting.
     * - Decreases warehouse_inventory.quantity
     * - Decreases product_variant_sizes.quantity by the same amount (keeps data in sync)
     */
    @Override
    @Transactional
    public WarehouseInventoryResponse deductStock(Long inventoryId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity to deduct must be greater than 0");
        }

        WarehouseInventory inventory = warehouseInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory record not found with ID: " + inventoryId));

        // availableQuantity = quantity - reservedQuantity (computed field in entity)
        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: "
                    + inventory.getAvailableQuantity() + ", Requested: " + quantity);
        }

        // Reduce warehouse stock
        inventory.setQuantity(inventory.getQuantity() - quantity);

        // Reduce product size quantity — Math.max(0) prevents negative values
        ProductVariantSize variantSize = inventory.getVariantSize();
        int newSizeQty = Math.max(0, variantSize.getQuantity() - quantity);
        variantSize.setQuantity(newSizeQty);
        variantSizeRepository.save(variantSize);

        return toInventoryResponse(warehouseInventoryRepository.save(inventory));
    }


    // ======================================================================
    //                          MAPPERS (entity → DTO)
    // ======================================================================

    /**
     * Convert a Warehouse entity to its API response DTO.
     */
    private WarehouseResponse toWarehouseResponse(Warehouse w) {
        return WarehouseResponse.builder()
                .id(w.getId())
                .warehouseCode(w.getWarehouseCode())
                .name(w.getName())
                .address(w.getAddress())
                .city(w.getCity())
                .state(w.getState())
                .pincode(w.getPincode())
                .country(w.getCountry())
                .contactNumber(w.getContactNumber())
                .email(w.getEmail())
                .isActive(w.isActive())
                .isDefault(w.isDefault())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    /**
     * Convert a WarehouseInventory entity (with joined Warehouse and ProductVariantSize) to its response DTO.
     */
    private WarehouseInventoryResponse toInventoryResponse(WarehouseInventory wi) {
        Warehouse w = wi.getWarehouse();
        ProductVariantSize pvs = wi.getVariantSize();
        return WarehouseInventoryResponse.builder()
                .id(wi.getId())
                .warehouseId(w.getId())
                .warehouseCode(w.getWarehouseCode())
                .warehouseName(w.getName())
                .warehouseCity(w.getCity())
                .variantSizeId(pvs.getId())
                .sizeSku(pvs.getSizeSku())
                .quantity(wi.getQuantity())
                .reservedQuantity(wi.getReservedQuantity())
                .availableQuantity(wi.getAvailableQuantity()) // computed: quantity - reservedQuantity
                .isActive(wi.isActive())
                .createdAt(wi.getCreatedAt())
                .updatedAt(wi.getUpdatedAt())
                .build();
    }
}
