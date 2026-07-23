# Product Service - API Documentation

Base URL: `http://localhost:8085` (product-service)

---

## Admin Controllers (`@PreAuthorize("hasRole('ADMIN')")`)

---

### 1. AdminProductController

**Base path:** `/api/adminProduct/products`

#### `POST /api/adminProduct/products/createProduct`
Create a new product.

**Request Body (`CreateProductRequestDTO`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| asin | `String` | Yes | Max 100 chars |
| name | `String` | Yes | Max 255 chars |
| description | `String` | Yes | Max 500 chars |
| category | `String` | Yes | Max 100 chars |
| subCategory | `String` | No | Max 100 chars |
| brand | `String` | Yes | Max 100 chars |
| material | `String` | Yes | Max 255 chars |
| gender | `Gender` (enum) | Yes | |
| ageGroup | `AgeGroup` (enum) | Yes | |
| tags | `List<String>` | No | Each max 50 chars |
| highlights | `List<String>` | No | Each max 300 chars |

**Response Body (`201 Created`):**
```json
{
  "id": 1,
  "asin": "ABC123",
  "name": "Product Name",
  "message": "Product created successfully"
}
```

---

#### `PUT /api/adminProduct/products/updateProduct`
Update an existing product (partial update).

**Request Body (`UpdateProductRequestDTO`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| productId | `Long` | Yes | |
| name | `String` | No | Max 255 chars |
| description | `String` | No | Max 500 chars |
| category | `String` | No | Max 100 chars |
| subCategory | `String` | No | Max 100 chars |
| brand | `String` | No | Max 100 chars |
| material | `String` | No | Max 255 chars |
| gender | `Gender` (enum) | No | |
| ageGroup | `AgeGroup` (enum) | No | |
| tags | `List<String>` | No | Full replace list, each max 50 |
| highlights | `List<String>` | No | Full replace list, each max 300 |

**Response Body (`200 OK`):**
```json
{
  "productId": 1,
  "asin": "ABC123",
  "name": "Updated Name",
  "updatedFields": ["name", "description"],
  "message": "Product updated successfully"
}
```

---

#### `PUT /api/adminProduct/products/publishProduct/{productId}`
Publish a product. Path variable: `productId` (Long)

**Response Body (`200 OK`):**
```
"Product published successfully"
```

---

#### `PUT /api/adminProduct/products/unpublishProduct/{productId}`
Unpublish a product. Path variable: `productId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `GET /api/adminProduct/products/getProductById/{productId}`
Get product by ID. Path variable: `productId` (Long)

**Response Body (`200 OK`):**
```json
{
  "id": 1,
  "asin": "ABC123",
  "name": "Product Name",
  "description": "Description",
  "category": "Category",
  "subCategory": "SubCategory",
  "brand": "Brand",
  "material": "Material",
  "gender": "MALE",
  "ageGroup": "ADULT",
  "tags": ["tag1", "tag2"],
  "highlights": ["highlight1"],
  "totalProductQuantity": 100,
  "active": true,
  "published": true,
  "variants": []
}
```
`variants` is a `List<ProductVariantResponseDTO>` (see variant response).

---

#### `GET /api/adminProduct/products/getProductByAsin/{asin}`
Get product by ASIN. Path variable: `asin` (String)

**Response Body (`200 OK`):** `ProductResponseDTO` (same as getProductById)

---

#### `GET /api/adminProduct/products/getDeletedProduct/{productId}`
Get soft-deleted product by ID. Path variable: `productId` (Long)

**Response Body (`200 OK`):** `ProductResponseDTO`

---

#### `GET /api/adminProduct/products/getDeletedProducts`
List all deleted products (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<DeletedProductListResponseDTO>`
```json
{
  "content": [{
    "id": 1,
    "asin": "ABC123",
    "name": "Product Name",
    "brand": "Brand",
    "category": "Category",
    "active": false,
    "deleted": true
  }],
  "totalPages": 1,
  "totalElements": 1,
  ...
}
```

---

#### `GET /api/adminProduct/products/getAllProducts`
List all non-deleted products (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`
```json
{
  "content": [{
    "id": 1,
    "asin": "ABC123",
    "name": "Product Name",
    "brand": "Brand",
    "category": "Category",
    "startingPrice": 999.99,
    "primaryImageUrl": "https://...",
    "active": true,
    "sellingPrice": 899.99,
    "discountAmount": 100.00,
    "discountPercent": 10,
    "hasDiscount": true
  }],
  ...
}
```

---

#### `GET /api/adminProduct/products/getAllPublishedProducts`
List all published products (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `PUT /api/adminProduct/products/deactivateProduct/{productId}`
Deactivate a product. Path variable: `productId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `PUT /api/adminProduct/products/activateProduct/{productId}`
Activate a product. Path variable: `productId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `DELETE /api/adminProduct/products/deleteProduct/{productId}`
Soft-delete a product. Path variable: `productId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `PUT /api/adminProduct/products/restoreProduct/{productId}`
Restore a soft-deleted product. Path variable: `productId` (Long)

**Response Body (`200 OK`):** `String` message

---

### 2. AdminVariantController

**Base path:** `/api/adminProduct/variants`

#### `POST /api/adminProduct/variants/createVariant`
Create a product variant.

**Request Body (`CreateProductVariantRequestDTO`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| productId | `Long` | Yes | |
| variantName | `String` | Yes | Max 255 chars |
| color | `String` | Yes | Max 100 chars |

**Response Body (`201 Created`):**
```json
{
  "variantId": 1,
  "productId": 1,
  "skuCode": "PRD-1-BLK",
  "variantName": "Black Variant",
  "message": "Variant created successfully"
}
```

---

#### `PUT /api/adminProduct/variants/updateVariant`
Update a variant.

**Request Body (`UpdateProductVariantRequestDTO`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| variantId | `Long` | Yes | |
| variantName | `String` | No | Max 255 chars |

**Response Body (`200 OK`):**
```json
{
  "variantId": 1,
  "skuCode": "PRD-1-BLK",
  "variantName": "Updated Variant",
  "updatedFields": ["variantName"],
  "message": "Variant updated successfully"
}
```

---

#### `GET /api/adminProduct/variants/{variantId}`
Get variant by ID. Path variable: `variantId` (Long)

**Response Body (`200 OK`):**
```json
{
  "id": 1,
  "skuCode": "PRD-1-BLK",
  "variantName": "Black Variant",
  "color": "Black",
  "totalProductVariantQuantity": 50,
  "active": true,
  "sizes": [],
  "images": []
}
```
`sizes` is `List<ProductVariantSizeResponseDTO>`, `images` is `List<ProductVariantImageResponseDTO>`.

---

#### `GET /api/adminProduct/variants/sku/{skuCode}`
Get variant by SKU. Path variable: `skuCode` (String)

**Response Body (`200 OK`):** `ProductVariantResponseDTO`

---

#### `GET /api/adminProduct/variants/getAllVariants`
List all variants (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<ProductVariantResponseDTO>`

---

#### `GET /api/adminProduct/variants/product/{productId}`
Get all variants under a product. Path variable: `productId` (Long)

**Response Body (`200 OK`):** `List<ProductVariantResponseDTO>`

---

#### `GET /api/adminProduct/variants/deletedVariant/{variantId}`
Get a soft-deleted variant by ID. Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `ProductVariantResponseDTO`

---

#### `GET /api/adminProduct/variants/deletedVariants`
List all deleted variants (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<ProductVariantResponseDTO>`

---

#### `PUT /api/adminProduct/variants/deactivateVariant/{variantId}`
Deactivate a variant. Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `PUT /api/adminProduct/variants/activateVariant/{variantId}`
Activate a variant. Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `DELETE /api/adminProduct/variants/deleteVariant/{variantId}`
Soft-delete a variant. Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `PUT /api/adminProduct/variants/restoreVariant/{variantId}`
Restore a soft-deleted variant. Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `String` message

---

### 3. AdminVariantSizeController

**Base path:** `/api/adminProduct/sizes`

#### `POST /api/adminProduct/sizes/createSize`
Create a size for a variant.

**Request Body (`CreateProductVariantSizeRequestDTO`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| variantId | `Long` | Yes | |
| size | `Size` (enum) | Yes | S, M, L, XL, etc. |
| quantity | `Integer` | Yes | Min 0 |
| mrp | `BigDecimal` | Yes | 8 integer + 2 decimal digits |
| weight | `BigDecimal` | Yes | Min 0.01 |
| length | `BigDecimal` | Yes | Min 0.01 |
| width | `BigDecimal` | Yes | Min 0.01 |
| height | `BigDecimal` | Yes | Min 0.01 |

**Response Body (`201 Created`):**
```json
{
  "sizeId": 1,
  "variantId": 1,
  "productId": 1,
  "sizeSku": "PRD-1-BLK-M",
  "message": "Size created successfully"
}
```

---

#### `PUT /api/adminProduct/sizes/updateSize`
Update a size.

**Request Body (`UpdateProductVariantSizeRequestDTO`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| sizeId | `Long` | Yes | |
| quantity | `Integer` | No | Min 0 |
| mrp | `BigDecimal` | No | 8 integer + 2 decimal digits |
| weight | `BigDecimal` | No | Min 0.01 |
| length | `BigDecimal` | No | Min 0.01 |
| width | `BigDecimal` | No | Min 0.01 |
| height | `BigDecimal` | No | Min 0.01 |

**Response Body (`200 OK`):**
```json
{
  "sizeId": 1,
  "size": "M",
  "updatedFields": ["mrp", "quantity"],
  "message": "Size updated successfully"
}
```

---

#### `GET /api/adminProduct/sizes/{sizeId}`
Get size by ID. Path variable: `sizeId` (Long)

**Response Body (`200 OK`):**
```json
{
  "sizeId": 1,
  "sizeSku": "PRD-1-BLK-M",
  "size": "M",
  "quantity": 50,
  "mrp": 999.99,
  "weight": 0.5,
  "length": 10.0,
  "width": 5.0,
  "height": 2.0,
  "active": true,
  "sellingPrice": 899.99,
  "discountAmount": 100.00,
  "discountPercent": 10,
  "hasDiscount": true,
  "discountName": "Summer Sale",
  "discountCode": "SUMMER10"
}
```

---

#### `GET /api/adminProduct/sizes/sku/{sizeSku}`
Get size by SKU. Path variable: `sizeSku` (String)

**Response Body (`200 OK`):** `ProductVariantSizeResponseDTO`

---

#### `GET /api/adminProduct/sizes/deletedSize/{sizeId}`
Get deleted size by ID. Path variable: `sizeId` (Long)

**Response Body (`200 OK`):** `ProductVariantSizeResponseDTO`

---

#### `GET /api/adminProduct/sizes/allDeletedSizes`
List all deleted sizes (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<ProductVariantSizeResponseDTO>`

---

#### `GET /api/adminProduct/sizes/allSizes`
List all non-deleted sizes (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<ProductVariantSizeResponseDTO>`

---

#### `GET /api/adminProduct/sizes/sizesUnderVariant/{variantId}`
List sizes under a variant. Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `List<ProductVariantSizeResponseDTO>`

---

#### `PUT /api/adminProduct/sizes/deactivateSize/{sizeId}`
Deactivate a size. Path variable: `sizeId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `PUT /api/adminProduct/sizes/activateSize/{sizeId}`
Activate a size. Path variable: `sizeId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `DELETE /api/adminProduct/sizes/deleteSize/{sizeId}`
Soft-delete a size. Path variable: `sizeId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `PUT /api/adminProduct/sizes/restoreSize/{sizeId}`
Restore a soft-deleted size. Path variable: `sizeId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `GET /api/adminProduct/sizes/sizeUnderVariant/{variantId}/count`
Count sizes under a variant. Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `Long` (count)

---

### 4. AdminVariantImageController

**Base path:** `/api/adminProduct/images`

#### `POST /api/adminProduct/images/createImage`
Create an image for a variant.

**Request Body (`CreateProductVariantImageRequestDTO`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| variantId | `Long` | Yes | |
| image | `String` | Yes | Max 500 chars (URL) |
| sortOrder | `Integer` | Yes | Min 1 |
| altText | `String` | No | Max 255 chars |

**Response Body (`201 Created`):**
```json
{
  "imageId": 1,
  "variantId": 1,
  "productId": 1,
  "image": "https://...",
  "message": "Image created successfully"
}
```

---

#### `PUT /api/adminProduct/images/updateImage`
Update an image.

**Request Body (`UpdateProductVariantImageRequestDTO`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| imageId | `Long` | Yes | |
| image | `String` | No | Max 500 chars |
| sortOrder | `Integer` | No | Min 1 |
| altText | `String` | No | Max 255 chars |

**Response Body (`200 OK`):**
```json
{
  "imageId": 1,
  "image": "https://...",
  "updatedFields": ["altText"],
  "message": "Image updated successfully"
}
```

---

#### `GET /api/adminProduct/images/{imageId}`
Get image by ID (non-deleted only). Path variable: `imageId` (Long)

**Response Body (`200 OK`):**
```json
{
  "imageId": 1,
  "image": "https://...",
  "sortOrder": 1,
  "altText": "Product image",
  "active": true
}
```

---

#### `GET /api/adminProduct/images/allImages`
List all non-deleted images (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<ProductVariantImageResponseDTO>`

---

#### `GET /api/adminProduct/images/imagesUnderVariant/{variantId}`
Get images by variant ID. Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `List<ProductVariantImageResponseDTO>`

---

#### `GET /api/adminProduct/images/deletedImage/{imageId}`
Get deleted image by ID. Path variable: `imageId` (Long)

**Response Body (`200 OK`):** `ProductVariantImageResponseDTO`

---

#### `GET /api/adminProduct/images/allDeletedImages`
List all deleted images (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<ProductVariantImageResponseDTO>`

---

#### `PUT /api/adminProduct/images/deactivateImage/{imageId}`
Deactivate an image. Path variable: `imageId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `PUT /api/adminProduct/images/activateImage/{imageId}`
Activate an image. Path variable: `imageId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `DELETE /api/adminProduct/images/deleteImage/{imageId}`
Soft-delete an image. Path variable: `imageId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `PUT /api/adminProduct/images/restoreImage/{imageId}`
Restore a soft-deleted image. Path variable: `imageId` (Long)

**Response Body (`200 OK`):** `String` message

---

#### `GET /api/adminProduct/images/imagesUnderVariant/{variantId}/count`
Count images per variant. Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `Long` (count)

---

#### `GET /api/adminProduct/images/activeImages/variant/{variantId}`
Get active images by variant for admin. Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `List<ProductVariantImageResponseDTO>`

---

### 5. AdminDiscountController

**Base path:** `/api/adminProduct/discounts`

#### `POST /api/adminProduct/discounts`
Create a discount.

**Request Body (`CreateDiscountRequest`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| discountCode | `String` | Yes | Max 50, alphanumeric only |
| discountName | `String` | Yes | Max 255 |
| discountType | `DiscountType` (enum) | Yes | |
| discountValue | `BigDecimal` | Yes | Min 0.01, 8 int + 2 decimal |
| minProductPrice | `BigDecimal` | No | 8 int + 2 decimal |
| applyTo | `DiscountApplyTo` (enum) | Yes | |
| category | `String` | No | Max 100 |
| productId | `Long` | No | |
| startDate | `Instant` | Yes | Future or present |
| endDate | `Instant` | Yes | Must be future, after startDate |

**Response Body (`201 Created`):**
```json
{
  "message": "Discount created successfully",
  "data": { "id": 1, "discountCode": "SUMMER10", ... }
}
```
`data` is a `DiscountResponse` object.

---

#### `PUT /api/adminProduct/discounts/{id}`
Update a discount. Path variable: `id` (Long)

**Request Body (`UpdateDiscountRequest`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| discountName | `String` | No | Max 255, not blank if provided |
| discountValue | `BigDecimal` | No | |
| minProductPrice | `BigDecimal` | No | |
| startDate | `Instant` | No | |
| endDate | `Instant` | No | Must be after startDate if both provided |

**Response Body (`200 OK`):**
```json
{
  "message": "Discount updated successfully",
  "data": { ... }
}
```

---

#### `GET /api/adminProduct/discounts/{id}`
Get discount by ID. Path variable: `id` (Long)

**Response Body (`200 OK`):** `DiscountResponse`
```json
{
  "id": 1,
  "discountCode": "SUMMER10",
  "discountName": "Summer Sale 10%",
  "discountType": "PERCENTAGE",
  "discountValue": 10.00,
  "minProductPrice": 500.00,
  "applyTo": "CATEGORY",
  "category": "Clothing",
  "productId": null,
  "productName": null,
  "startDate": "2026-06-01 00:00:00",
  "endDate": "2026-08-31 23:59:59",
  "active": true,
  "currentlyValid": true,
  "createdAt": "2026-05-15 10:30:00",
  "updatedAt": "2026-05-15 10:30:00",
  "maxUsageCount": null,
  "usedCount": 0
}
```

---

#### `GET /api/adminProduct/discounts`
Get all discounts.

**Response Body (`200 OK`):** `List<DiscountResponse>`

---

#### `GET /api/adminProduct/discounts/active`
Get active discounts.

**Response Body (`200 OK`):** `List<DiscountResponse>`

---

#### `GET /api/adminProduct/discounts/type/{applyTo}`
Get discounts by apply-to type. Path variable: `applyTo` (String)

**Response Body (`200 OK`):** `List<DiscountResponse>`

---

#### `PUT /api/adminProduct/discounts/{id}/activate`
Activate a discount. Path variable: `id` (Long)

**Response Body (`200 OK`):**
```json
{
  "message": "Discount activated successfully",
  "data": { ... }
}
```

---

#### `PUT /api/adminProduct/discounts/{id}/deactivate`
Deactivate a discount. Path variable: `id` (Long)

**Response Body (`200 OK`):**
```json
{
  "message": "Discount deactivated successfully",
  "data": { ... }
}
```

---

#### `DELETE /api/adminProduct/discounts/{id}`
Delete a discount. Path variable: `id` (Long)

**Response Body (`200 OK`):**
```json
{
  "message": "Discount deleted successfully"
}
```

---

### 6. AdminRelatedProductController

**Base path:** `/api/adminRelatedProduct`

#### `POST /api/adminRelatedProduct`
Add a related product.

**Request Body (`AddRelatedProductRequest`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| productId | `Long` | Yes | |
| relatedProductVariantId | `Long` | Yes | |
| displayOrder | `Integer` | Yes | 1-5 |

**Response Body (`201 Created`):**
```json
{
  "message": "Related product added successfully",
  "data": { ... }
}
```
`data` is a `RelatedProductResponse`:
```json
{
  "id": 1,
  "productId": 1,
  "productName": "Product Name",
  "relatedProductVariantId": 2,
  "relatedVariantName": "Variant Name",
  "relatedProductColor": "Black",
  "relatedProductVariantSku": "PRD-1-BLK",
  "displayOrder": 1,
  "active": true
}
```

---

#### `GET /api/adminRelatedProduct/{productId}`
Get manual related products. Path variable: `productId` (Long)

**Response Body (`200 OK`):** `List<RelatedProductResponse>`

---

#### `DELETE /api/adminRelatedProduct/{productId}/remove/{relatedVariantId}`
Remove a related product. Path variables: `productId` (Long), `relatedVariantId` (Long)

**Response Body (`200 OK`):**
```json
{
  "message": "Related product removed successfully"
}
```

---

#### `PUT /api/adminRelatedProduct/{productId}/restore/{relatedVariantId}`
Restore a removed related product. Path variables: `productId` (Long), `relatedVariantId` (Long)

**Response Body (`200 OK`):**
```json
{
  "message": "Related product restored successfully"
}
```

---

#### `PUT /api/adminRelatedProduct/{productId}/activate/{relatedVariantId}`
Activate a related product. Path variables: `productId` (Long), `relatedVariantId` (Long)

**Response Body (`200 OK`):**
```json
{
  "message": "Related product activated successfully",
  "data": { ... }
}
```

---

#### `PUT /api/adminRelatedProduct/{productId}/deactivate/{relatedVariantId}`
Deactivate a related product. Path variables: `productId` (Long), `relatedVariantId` (Long)

**Response Body (`200 OK`):**
```json
{
  "message": "Related product deactivated successfully",
  "data": { ... }
}
```

---

### 7. AdminReviewController

**Base path:** `/api/adminProduct/reviews`

#### `GET /api/adminProduct/reviews/{productId}`
Get reviews by product ID (paginated). Path variable: `productId` (Long)

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<ReviewResponse>`
```json
{
  "content": [{
    "id": 1,
    "productId": 1,
    "productName": "Product Name",
    "userEmail": "user@example.com",
    "orderId": 123,
    "rating": 5,
    "reviewTitle": "Great product",
    "reviewText": "Loved it!",
    "verifiedPurchase": true,
    "reviewImageUrls": ["https://..."],
    "createdAt": "2026-06-01 12:00:00"
  }],
  ...
}
```

---

#### `DELETE /api/adminProduct/reviews/{id}`
Delete a review. Path variable: `id` (Long)

**Response Body (`200 OK`):**
```json
{
  "message": "Review deleted successfully"
}
```

---

## Customer Controllers (Public / Authenticated)

---

### 8. CustomerProductController

**Base path:** `/api/customerProduct/products`

#### `GET /api/customerProduct/products/published`
Get all published products (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=20)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/recent`
Get recently published products (paginated).

**Query Params:** `page` (int, default=0), `size` (int, default=10)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/search/name/{name}`
Search products by name (paginated). Path variable: `name` (String)

**Query Params:** `page` (int, default=0), `size` (int, default=20)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/category/{category}`
Filter by category (paginated). Path variable: `category` (String)

**Query Params:** `page` (int, default=0), `size` (int, default=20)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/category/{category}/subcategory/{subCategory}`
Filter by category + sub-category (paginated).

**Path Variables:** `category` (String), `subCategory` (String)

**Query Params:** `page` (int, default=0), `size` (int, default=20)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/brand/{brand}`
Filter by brand (paginated). Path variable: `brand` (String)

**Query Params:** `page` (int, default=0), `size` (int, default=20)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/gender/{gender}`
Filter by gender (paginated). Path variable: `gender` (Gender enum)

**Query Params:** `page` (int, default=0), `size` (int, default=20)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/ageGroup/{ageGroup}`
Filter by age group (paginated). Path variable: `ageGroup` (AgeGroup enum)

**Query Params:** `page` (int, default=0), `size` (int, default=20)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/search/tag/{tag}`
Search by tag (paginated). Path variable: `tag` (String)

**Query Params:** `page` (int, default=0), `size` (int, default=20)

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/getProductByAsin/{asin}`
Get active product by ASIN (public). Path variable: `asin` (String)

**Response Body (`200 OK`):** `ProductResponseDTO`

---

#### `GET /api/customerProduct/products/filter`
Filter by multiple parameters (paginated).

**Query Params:**
| Param | Type | Required |
|-------|------|----------|
| category | `String` | No |
| subCategory | `String` | No |
| gender | `Gender` (enum) | No |
| tag | `String` | No |
| page | `int` | No (default=0) |
| size | `int` | No (default=20) |

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/price-range`
Price range filter (paginated).

**Query Params:**
| Param | Type | Required |
|-------|------|----------|
| min | `BigDecimal` | Yes |
| max | `BigDecimal` | Yes |
| page | `int` | No (default=0) |
| size | `int` | No (default=20) |

**Response Body (`200 OK`):** `Page<ProductListResponseDTO>`

---

#### `GET /api/customerProduct/products/{productId}/related`
Get related products. Path variable: `productId` (Long)

**Response Body (`200 OK`):** `List<ProductListResponseDTO>`

---

### 9. CustomerVariantController

**Base path:** `/api/customerProduct/variants`

#### `GET /api/customerProduct/variants/sku/{skuCode}`
Get active variant by SKU. Path variable: `skuCode` (String)

**Response Body (`200 OK`):** `ProductVariantResponseDTO`

---

#### `GET /api/customerProduct/variants/search/name`
Search variants by name.

**Query Params:** `name` (String)

**Response Body (`200 OK`):** `List<ProductVariantListResponseDTO>`
```json
[{
  "id": 1,
  "skuCode": "PRD-1-BLK",
  "variantName": "Black Variant",
  "color": "Black",
  "primaryImageUrl": "https://...",
  "startingPrice": 999.99,
  "active": true,
  "sellingPrice": 899.99,
  "discountAmount": 100.00,
  "discountPercent": 10,
  "hasDiscount": true
}]
```

---

#### `GET /api/customerProduct/variants/search/color`
Search variants by color.

**Query Params:** `color` (String)

**Response Body (`200 OK`):** `List<ProductVariantListResponseDTO>`

---

#### `GET /api/customerProduct/variants/countVariant/{productId}`
Count variants by product ID. Path variable: `productId` (Long)

**Response Body (`200 OK`):** `Long` (count)

---

### 10. CustomerSizeController

**Base path:** `/api/customerProduct/sizes`

#### `GET /api/customerProduct/sizes/sku/{sizeSku}`
Get size by SKU for customer. Path variable: `sizeSku` (String)

**Response Body (`200 OK`):** `ProductVariantSizeResponseDTO`

---

#### `GET /api/customerProduct/sizes/searchBySize/{size}`
Search by size enum. Path variable: `size` (Size enum)

**Response Body (`200 OK`):** `List<ProductVariantSizeListResponseDTO>`
```json
[{
  "sizeId": 1,
  "sizeSku": "PRD-1-BLK-M",
  "size": "M",
  "quantity": 50,
  "mrp": 999.99,
  "active": true
}]
```

---

### 11. CustomerImageController

**Base path:** `/api/customerProduct/images`

#### `GET /api/customerProduct/images/variant/{variantId}`
Get all active images for a variant (customer-facing). Path variable: `variantId` (Long)

**Response Body (`200 OK`):** `List<ProductVariantImageResponseDTO>`

---

### 12. CustomerReviewController

**Base path:** `/api/customer/reviews`

#### `POST /api/customer/reviews`
Create a review (requires `CUSTOMER` role).

**Request Body (`CreateReviewRequest`):**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| productId | `Long` | Yes | |
| orderId | `Long` | Yes | |
| rating | `Integer` | Yes | 1-5 |
| reviewTitle | `String` | No | Max 255 |
| reviewText | `String` | No | Max 2000 |
| reviewImageUrls | `List<String>` | No | Max 4 |

**Response Body (`201 Created`):**
```json
{
  "message": "Review added successfully",
  "review": { ... }
}
```
`review` is a `ReviewResponse`.

---

#### `GET /api/customer/reviews/product/{productId}`
Get all reviews for a product (public). Path variable: `productId` (Long)

**Response Body (`200 OK`):** `List<ReviewResponse>`

---

#### `GET /api/customer/reviews/product/{productId}/rating`
Get rating summary for a product (public). Path variable: `productId` (Long)

**Response Body (`200 OK`):**
```json
{
  "averageRating": 4.5,
  "reviewCount": 10,
  "ratingDistribution": { "1": 0, "2": 1, "3": 1, "4": 3, "5": 5 }
}
```

---

#### `GET /api/customer/reviews/{id}`
Get single review by ID (public). Path variable: `id` (Long)

**Response Body (`200 OK`):** `ReviewResponse`

---

#### `GET /api/customer/reviews/my-reviews`
Get my reviews (requires `CUSTOMER` role). Uses JWT authentication.

**Response Body (`200 OK`):** `List<ReviewResponse>`

---

## Enum Types Reference

| Enum | Values |
|------|--------|
| `Gender` | `MALE`, `FEMALE`, `UNISEX` |
| `AgeGroup` | `ADULT`, `KIDS`, `INFANT` |
| `Size` | `S`, `M`, `L`, `XL`, `XXL`, `XXXL` |
| `DiscountType` | `PERCENTAGE`, `FLAT` |
| `DiscountApplyTo` | `PRODUCT`, `CATEGORY`, `ALL` |

---

## Paginated Response Structure

All paginated endpoints return Spring Data `Page<T>`:
```json
{
  "content": [...],
  "totalPages": 5,
  "totalElements": 50,
  "size": 10,
  "number": 0,
  "sort": { "sorted": false, "unsorted": true, "empty": true },
  "first": true,
  "last": false,
  "empty": false
}
```
