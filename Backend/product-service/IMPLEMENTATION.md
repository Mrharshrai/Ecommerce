# Product Service — Implementation Summary

## What This Service Does

The Product Service manages the full product lifecycle for the meradesh platform. This includes product catalog management (products, variants, sizes, images), discounts, customer reviews, related product suggestions, and **multi-warehouse inventory management**.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2.6 |
| Database | MySQL (`product_service_db`) |
| ORM | Hibernate / Spring Data JPA |
| Security | Spring Security + JWT |
| Service Discovery | Netflix Eureka Client |
| Inter-service | OpenFeign |
| Boilerplate | Lombok |

---

## Project Structure

```
product-service/
├── controller/
│   ├── AdminWarehouseController.java          ← Warehouse + inventory management (Admin)
│   └── basicController/
│       ├── AdminProductSetupController/
│       │   ├── AdminProductController.java
│       │   ├── AdminVariantController.java
│       │   ├── AdminVariantSizeController.java
│       │   ├── AdminVariantImageController.java
│       │   ├── AdminDiscountController.java
│       │   ├── AdminRelatedProductController.java
│       │   └── AdminReviewController.java
│       ├── CustomerController/
│       │   ├── CustomerProductController.java
│       │   ├── CustomerVariantController.java
│       │   ├── CustomerSizeController.java
│       │   └── CustomerImageController.java
│       └── CustomerProductController/
│           └── CustomerReviewController.java
├── entity/
│   ├── Product.java
│   ├── ProductVariant.java
│   ├── ProductVariantSize.java
│   ├── ProductVariantImage.java
│   ├── Discount.java
│   ├── ProductReview.java
│   ├── RelatedProduct.java
│   ├── Warehouse.java                         ← New
│   └── WarehouseInventory.java                ← New
├── repository/
│   ├── ProductRepository.java
│   ├── ProductVariantRepository.java
│   ├── ProductVariantSizeRepository.java
│   ├── ProductVariantImageRepository.java
│   ├── DiscountRepository.java
│   ├── ProductReviewRepository.java
│   ├── RelatedProductRepository.java
│   ├── WarehouseRepository.java               ← New
│   └── WarehouseInventoryRepository.java      ← New
├── service/
│   └── warehouse/
│       ├── WarehouseService.java              ← Interface (New)
│       └── WarehouseServiceImpl.java          ← Full implementation (New)
├── DTOs/
│   └── WarehouseDTOs/
│       ├── RequestDTOs/
│       │   ├── CreateWarehouseRequest.java
│       │   └── UpdateWarehouseInventoryRequest.java
│       └── ResponseDTOs/
│           ├── WarehouseResponse.java
│           └── WarehouseInventoryResponse.java
├── security/
│   └── JwtAuthFilter.java
├── config/
│   └── SecurityConfig.java
└── enums/
    ├── Gender.java
    ├── AgeGroup.java
    ├── DiscountType.java
    └── DiscountApplyTo.java
```

---

## Feature Breakdown

### 1. Product Catalog Management

**What it does:**
- Products have color variants. Each variant has multiple sizes. Each size has its own price and stock quantity.
- Products and variants can be published/unpublished independently.
- Soft-delete is used throughout (`@SQLDelete` + `@Where`).

**Key business rules:**
- A product cannot be published if it has no variants.
- A variant cannot be published if it has no sizes.
- Soft-deleted records are excluded from all queries via `@Where(clause = "is_deleted = false")`.

---

### 2. Discount System

**Types:**
- `FLAT` — Fixed rupee amount off
- `PERCENTAGE` — Percentage off

**Scope (`applyTo`):**
- `PRODUCT` — Applied to all sizes of all variants
- `VARIANT` — Applied to all sizes of one specific variant
- `SIZE` — Applied to one specific size only
- `ALL` — Applied to all products

**Rules:**
- Discounts are time-bounded (`validFrom`, `validUntil`).
- Only active discounts are returned to customers.

---

### 3. Reviews and Ratings System

- Customers can submit text reviews with star ratings (1–5).
- `isVerifiedPurchase` flag is set when the customer has actually purchased the product.
- Average rating on the product is auto-recalculated after each review submission.
- Admins can delete any review.

---

### 4. Related Products

- Admins can manually link products as related.
- There is also an auto-suggestion endpoint that finds products in the same category/brand.
- Supports ordered display (`displayOrder` field for custom sorting).

---

### 5. Warehouse Management *(New Feature)*

**Goal:** Track stock across multiple physical warehouses. Deduct stock from the correct warehouse when an order is placed.

**Design decisions:**
- `warehouses` and `warehouse_inventory` are **completely separate tables** — zero changes to existing product/variant/size tables.
- `product_variant_sizes.quantity` is kept in sync automatically whenever warehouse stock changes.
- Only **one warehouse** can be marked as `isDefault` at a time.
- Soft-delete is used (same pattern as rest of the service).

**Key operations:**
| Operation | What it does |
|-----------|-------------|
| `addInventory` | Creates a new (warehouse + size) stock record |
| `addStock` | Increases existing inventory quantity (new shipment) |
| `deductStock` | Decreases inventory quantity (order fulfilled) |
| `updateInventory` | Admin correction / stock count override |
| `getAvailableWarehouseForSize` | Returns warehouse with the most available stock for a given sizeSku |

**Stock sync rule:**
Every time warehouse stock changes, `product_variant_sizes.quantity` is updated by the same delta in the **same transaction**. This means the existing cart-service and order-service never need to know about warehouses.

---

## Security

All admin endpoints (`/api/adminProduct/**`) require a valid JWT with `ROLE_ADMIN`.

The JWT is validated using a shared secret defined in `application.properties`:
```
jwt.secret=your-jwt-secret-here
```

Internal endpoints (`/internal/**`) are open — they are only accessible within the Eureka service mesh.

---

## Database Auto-Migration

```properties
spring.jpa.hibernate.ddl-auto=update
```

On startup, Hibernate automatically creates or updates:
- `warehouses` table
- `warehouse_inventory` table

No manual migration scripts needed for development.

---

## Inter-Service Communication

The product-service **exposes** internal endpoints for other services:

| Called by | Endpoint | Purpose |
|-----------|----------|---------|
| cart-service | `GET /internal/products/size/{sizeId}` | Check stock/price before adding to cart |
| order-service | `PUT /internal/products/size/{sizeId}/deduct` | Deduct stock when order placed |

The product-service also potentially **calls** no external services — it is self-contained.

---

## Known Warnings (Non-breaking)

| Warning | Explanation |
|---------|-------------|
| `MySQL8Dialect deprecated` | Use `MySQLDialect` instead. Does not affect functionality. |
| `spring.jpa.open-in-view` | Safe to disable in production. |
| `UserDetailsServiceAutoConfiguration` | Product service uses custom JWT filter, so the auto-configured default security password warning is harmless. |
| `LoadBalancerCaffeineWarnLogger` | Add Caffeine cache dependency for production performance. |

---

## Total Endpoints

| Area | Count |
|------|-------|
| Product CRUD (Admin) | 8 |
| Variant CRUD (Admin) | 6 |
| Size CRUD (Admin) | 5 |
| Image CRUD (Admin) | 4 |
| Discount CRUD (Admin) | 8 |
| Related Products (Admin) | 3 |
| Reviews (Admin) | 2 |
| **Warehouse + Inventory (Admin)** | **22** |
| Product Browse (Customer) | 7 |
| Reviews (Customer) | 4 |
| Internal APIs | 3+ |
| **Total** | **~72+** |
