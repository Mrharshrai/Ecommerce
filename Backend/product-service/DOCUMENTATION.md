# Product Service — Documentation

## Overview

The **Product Service** is a standalone Spring Boot microservice responsible for managing the entire product catalog, warehouse inventory, discounts, reviews, and related products. It is designed to be used by both **Admin** (for catalog management) and **Customers** (for browsing and purchasing).

- **Port:** `8082`
- **Database:** `product_service_db` (MySQL)
- **Service Name (Eureka):** `product-service`
- **Authentication:** JWT-based (shared secret with user-service)

---

## Database Schema

### Existing Tables (Product Catalog — unchanged)

| Table | Description |
|-------|-------------|
| `products` | Core product information (name, brand, gender, etc.) |
| `product_variants` | Color variants of a product |
| `product_variant_sizes` | Sizes under each variant with `quantity` field |
| `product_variant_images` | Images linked to each variant |
| `discounts` | Discount rules (flat/percentage, apply scope) |
| `product_reviews` | Customer reviews and star ratings |
| `related_products` | Manually or auto-linked related products |

### New Tables (Warehouse Management — separate, non-breaking)

| Table | Description |
|-------|-------------|
| `warehouses` | Physical warehouse locations |
| `warehouse_inventory` | Stock per (warehouse + variantSize) pair |

> **Note:** `product_variant_sizes.quantity` is always kept in sync with warehouse stock. No existing code needed to change.

---

## Entities

### Product
- Fields: `id`, `name`, `description`, `brand`, `basePrice`, `gender`, `ageGroup`, `isPublished`, `isDeleted`

### ProductVariant (Color Variant)
- Fields: `id`, `product`, `colorName`, `colorHex`, `isPublished`, `isDeleted`

### ProductVariantSize
- Fields: `id`, `variant`, `sizeName`, `sizeSku`, `price`, `quantity`, `isAvailable`

### ProductVariantImage
- Fields: `id`, `variant`, `imageUrl`, `isPrimary`, `displayOrder`

### Discount
- Fields: `id`, `code`, `discountType` (FLAT/PERCENTAGE), `value`, `applyTo`, `isActive`, `validFrom`, `validUntil`

### ProductReview
- Fields: `id`, `productId`, `customerId`, `rating`, `comment`, `isVerifiedPurchase`, `createdAt`

### RelatedProduct
- Fields: `id`, `product`, `relatedProduct`, `relationshipType`, `displayOrder`

### Warehouse *(New)*
- Fields: `id`, `warehouseCode`, `name`, `address`, `city`, `state`, `pincode`, `country`, `contactNumber`, `email`, `isActive`, `isDefault`, `isDeleted`

### WarehouseInventory *(New)*
- Fields: `id`, `warehouse`, `variantSize`, `quantity`, `reservedQuantity`, `isActive`, `isDeleted`
- Computed: `availableQuantity = quantity - reservedQuantity`

---

## Enums

| Enum | Values |
|------|--------|
| `Gender` | MALE, FEMALE, UNISEX, KIDS |
| `AgeGroup` | ADULT, TEEN, CHILD, INFANT |
| `DiscountType` | FLAT, PERCENTAGE |
| `DiscountApplyTo` | PRODUCT, VARIANT, SIZE, ALL |

---

## Security Configuration

| Path Pattern | Access |
|---|---|
| `/api/adminProduct/**` | `ROLE_ADMIN` only |
| `/internal/**` | Permit all (inter-service calls via Eureka) |
| All others | Permit all (public browse endpoints) |

---

## API Endpoints

### Admin — Product Management (`/api/adminProduct/products`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/adminProduct/products` | Create a new product |
| PUT | `/api/adminProduct/products/{id}` | Update product details |
| DELETE | `/api/adminProduct/products/{id}` | Soft-delete a product |
| PUT | `/api/adminProduct/products/{id}/publish` | Publish product |
| PUT | `/api/adminProduct/products/{id}/unpublish` | Unpublish product |
| PUT | `/api/adminProduct/products/{id}/restore` | Restore deleted product |
| GET | `/api/adminProduct/products` | Get all products (paginated) |
| GET | `/api/adminProduct/products/{id}` | Get product by ID |

### Admin — Variant Management (`/api/adminProduct/variants`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/adminProduct/variants` | Create a variant |
| PUT | `/api/adminProduct/variants/{id}` | Update variant |
| DELETE | `/api/adminProduct/variants/{id}` | Soft-delete variant |
| PUT | `/api/adminProduct/variants/{id}/publish` | Publish variant |
| PUT | `/api/adminProduct/variants/{id}/unpublish` | Unpublish variant |
| GET | `/api/adminProduct/variants/product/{productId}` | List variants of a product |

### Admin — Size Management (`/api/adminProduct/sizes`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/adminProduct/sizes` | Add size to a variant |
| PUT | `/api/adminProduct/sizes/{id}` | Update size |
| DELETE | `/api/adminProduct/sizes/{id}` | Delete size |
| PUT | `/api/adminProduct/sizes/{id}/update-stock` | Update stock quantity directly |
| GET | `/api/adminProduct/sizes/variant/{variantId}` | Get sizes of a variant |

### Admin — Image Management (`/api/adminProduct/images`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/adminProduct/images` | Add image to variant |
| PUT | `/api/adminProduct/images/{id}` | Update image |
| DELETE | `/api/adminProduct/images/{id}` | Delete image |
| PUT | `/api/adminProduct/images/{id}/set-primary` | Set image as primary |

### Admin — Discount Management (`/api/adminProduct/discounts`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/adminProduct/discounts` | Create discount |
| PUT | `/api/adminProduct/discounts/{id}` | Update discount |
| DELETE | `/api/adminProduct/discounts/{id}` | Delete discount |
| PUT | `/api/adminProduct/discounts/{id}/activate` | Activate discount |
| PUT | `/api/adminProduct/discounts/{id}/deactivate` | Deactivate discount |
| POST | `/api/adminProduct/discounts/apply/{productId}` | Apply discount to product |
| GET | `/api/adminProduct/discounts` | List all discounts |
| GET | `/api/adminProduct/discounts/{id}` | Get discount by ID |

### Admin — Review Management (`/api/adminProduct/reviews`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/adminProduct/reviews/product/{productId}` | Get all reviews for a product |
| DELETE | `/api/adminProduct/reviews/{id}` | Remove a review |

### Admin — Related Products (`/api/adminProduct/related`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/adminProduct/related` | Link two products |
| DELETE | `/api/adminProduct/related/{id}` | Unlink related product |
| GET | `/api/adminProduct/related/product/{productId}` | Get related products |

### Admin — Warehouse Management (`/api/adminProduct/warehouses`) *(New)*

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/adminProduct/warehouses` | Create warehouse |
| PUT | `/api/adminProduct/warehouses/{id}` | Update warehouse |
| DELETE | `/api/adminProduct/warehouses/{id}` | Soft-delete warehouse |
| GET | `/api/adminProduct/warehouses` | List all warehouses |
| GET | `/api/adminProduct/warehouses/{id}` | Get warehouse by ID |
| GET | `/api/adminProduct/warehouses/code/{code}` | Get warehouse by code |
| GET | `/api/adminProduct/warehouses/active` | List active warehouses |
| GET | `/api/adminProduct/warehouses/default` | Get default warehouse |
| PUT | `/api/adminProduct/warehouses/{id}/activate` | Activate warehouse |
| PUT | `/api/adminProduct/warehouses/{id}/deactivate` | Deactivate warehouse |
| PUT | `/api/adminProduct/warehouses/{id}/set-default` | Set as default warehouse |
| POST | `/api/adminProduct/warehouses/inventory` | Create inventory record |
| PUT | `/api/adminProduct/warehouses/inventory/{inventoryId}` | Overwrite inventory quantity |
| DELETE | `/api/adminProduct/warehouses/inventory/{inventoryId}` | Delete inventory record |
| GET | `/api/adminProduct/warehouses/{warehouseId}/inventory` | Get all inventory for warehouse |
| GET | `/api/adminProduct/warehouses/inventory/variant-size/{variantSizeId}` | Get inventory by size |
| GET | `/api/adminProduct/warehouses/inventory/available?sizeSku={sku}` | Best warehouse for a size |
| GET | `/api/adminProduct/warehouses/inventory/available/all?sizeSku={sku}` | All warehouses with stock |
| PUT | `/api/adminProduct/warehouses/inventory/{inventoryId}/activate` | Activate inventory record |
| PUT | `/api/adminProduct/warehouses/inventory/{inventoryId}/deactivate` | Deactivate inventory record |
| PUT | `/api/adminProduct/warehouses/inventory/{inventoryId}/add-stock?quantity={n}` | Add stock |
| PUT | `/api/adminProduct/warehouses/inventory/{inventoryId}/deduct-stock?quantity={n}` | Deduct stock |

---

### Customer — Product Browsing

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/customerProduct/products` | List published products (with filters) |
| GET | `/api/customerProduct/products/{id}` | Product detail |
| GET | `/api/customerProduct/products/search?q={keyword}` | Search products |
| GET | `/api/customerProduct/variants/{productId}` | List variants of a product |
| GET | `/api/customerProduct/sizes/{variantId}` | List sizes of a variant |
| GET | `/api/customerProduct/images/{variantId}` | Images of a variant |
| GET | `/api/customerProduct/related/{productId}` | Related products |

### Customer — Reviews (`/api/customer/reviews`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/customer/reviews` | Submit a review |
| PUT | `/api/customer/reviews/{id}` | Edit own review |
| DELETE | `/api/customer/reviews/{id}` | Delete own review |
| GET | `/api/customer/reviews/product/{productId}` | List reviews for a product |

---

## Internal APIs (`/internal/...`)

These endpoints are called by other microservices (order-service, cart-service) via Feign clients. No JWT needed — secured by Eureka network boundary.

| Method | Endpoint | Caller | Description |
|--------|----------|--------|-------------|
| GET | `/internal/products/size/{sizeId}` | cart-service | Get size details (price, stock) |
| PUT | `/internal/products/size/{sizeId}/deduct?qty={n}` | order-service | Deduct stock after order |
| GET | `/internal/warehouse/available?sizeSku={sku}` | order-service | Get best warehouse for order routing |

---

## Warehouse Stock Flow

```
New Stock Arrives at Warehouse
        ↓
Admin calls: addStock(inventoryId, quantity)
        ↓
warehouse_inventory.quantity  += quantity
product_variant_sizes.quantity += quantity (synced)

---

Order is Placed for sizeSku "SKU-RED-M"
        ↓
Order-service calls: getAvailableWarehouseForSize("SKU-RED-M")
        ↓  (returns warehouse with most available stock)
Order-service calls: deductStock(inventoryId, quantity)
        ↓
warehouse_inventory.quantity  -= quantity
product_variant_sizes.quantity -= quantity (synced)
```

---

## Configuration

```properties
server.port=8082
spring.application.name=product-service

spring.datasource.url=jdbc:mysql://localhost:3306/product_service_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

---

## Dependencies

- Spring Boot 3.2.6
- Spring Data JPA (MySQL)
- Spring Security (JWT)
- Spring Cloud Netflix Eureka Client
- Spring Cloud OpenFeign
- Lombok
- MapStruct
