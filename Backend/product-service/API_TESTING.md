# PRODUCT SERVICE - API TESTING GUIDE (POSTMAN)

## 📋 Table of Contents
1. [Setup & Configuration](#setup--configuration)
2. [Admin Product Management](#admin-product-management)
3. [Admin Variant Management](#admin-variant-management)
4. [Admin Size Management](#admin-size-management)
5. [Admin Image Management](#admin-image-management)
6. [Admin Discount Management](#admin-discount-management)
7. [Admin Review Management](#admin-review-management)
8. [Admin Related Products Management](#admin-related-products-management)
9. [Customer Product Browsing](#customer-product-browsing)
10. [Customer Reviews](#customer-reviews)

---

## Setup & Configuration

### Base URL
```
http://localhost:8082
```

### Authentication Headers

**For Admin Endpoints:**
```
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**For Customer Endpoints (Protected):**
```
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

### Getting JWT Tokens
You need to get tokens from User Service (Port 8081):

**Admin Login:**
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

**Customer Login:**
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "customer@example.com",
  "password": "customer123"
}
```

---

## Admin Product Management

### 1. Create Product

```http
POST http://localhost:8082/api/adminProduct/products
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "asin": "PROD001",
  "name": "Cotton T-Shirt",
  "category": "Clothing",
  "subCategory": "T-Shirts",
  "brand": "Nike",
  "gender": "MALE",
  "ageGroup": "ADULT",
  "description": "Premium cotton t-shirt with comfortable fit",
  "mrp": 999.00,
  "tags": ["casual", "summer", "cotton"]
}
```

**Expected Response:**
```json
{
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "asin": "PROD001",
    "name": "Cotton T-Shirt",
    "isActive": true,
    "isPublished": false,
    "isDeleted": false,
    "totalProductQuantity": 0
  }
}
```

---

### 2. Update Product

```http
PUT http://localhost:8082/api/adminProduct/products/1
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "name": "Premium Cotton T-Shirt",
  "description": "Updated description",
  "mrp": 1099.00
}
```

---

### 3. Publish Product

```http
PUT http://localhost:8082/api/adminProduct/products/1/publish
Authorization: Bearer <ADMIN_TOKEN>
```

**Note:** Product must have at least 1 active variant, 1 active size, and 1 active image before publishing.

---

### 4. Get Product by ID

```http
GET http://localhost:8082/api/adminProduct/products/1
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 5. Get Product by ASIN

```http
GET http://localhost:8082/api/adminProduct/products/asin/PROD001
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 6. Get All Active Products

```http
GET http://localhost:8082/api/adminProduct/products/active
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 7. Deactivate Product

```http
PUT http://localhost:8082/api/adminProduct/products/1/deactivate
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 8. Soft Delete Product

```http
DELETE http://localhost:8082/api/adminProduct/products/1
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 9. Restore Product

```http
PUT http://localhost:8082/api/adminProduct/products/1/restore
Authorization: Bearer <ADMIN_TOKEN>
```

---

## Admin Variant Management

### 1. Create Variant

```http
POST http://localhost:8082/api/adminProduct/variants
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "productId": 1,
  "skuCode": "PROD001-RED",
  "color": "Red",
  "mrp": 999.00,
  "description": "Red color variant"
}
```

**Expected Response:**
```json
{
  "message": "Variant created successfully",
  "data": {
    "id": 1,
    "skuCode": "PROD001-RED",
    "color": "Red",
    "isActive": true,
    "totalVariantQuantity": 0
  }
}
```

---

### 2. Get Variant by SKU Code

```http
GET http://localhost:8082/api/adminProduct/variants/sku/PROD001-RED
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 3. Get Variants by Product ID

```http
GET http://localhost:8082/api/adminProduct/variants/product/1
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 4. Update Variant

```http
PUT http://localhost:8082/api/adminProduct/variants/1
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "color": "Bright Red",
  "mrp": 1099.00
}
```

---

## Admin Size Management

### 1. Create Size

```http
POST http://localhost:8082/api/adminProduct/sizes
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "variantId": 1,
  "sizeSku": "PROD001-RED-M",
  "size": "M",
  "quantity": 100
}
```

**Expected Response:**
```json
{
  "message": "Size created successfully",
  "data": {
    "id": 1,
    "sizeSku": "PROD001-RED-M",
    "size": "M",
    "quantity": 100,
    "isActive": true
  }
}
```

---

### 2. Get Size by Size SKU

```http
GET http://localhost:8082/api/adminProduct/sizes/sizeSku/PROD001-RED-M
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 3. Get Sizes by Variant ID

```http
GET http://localhost:8082/api/adminProduct/sizes/variant/1
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 4. Update Size Quantity

```http
PUT http://localhost:8082/api/adminProduct/sizes/1
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "quantity": 150
}
```

---

## Admin Image Management

### 1. Create Image

```http
POST http://localhost:8082/api/adminProduct/images
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "variantId": 1,
  "imageUrl": "https://example.com/images/prod001-red-1.jpg",
  "isPrimary": true
}
```

**Expected Response:**
```json
{
  "message": "Image created successfully",
  "data": {
    "id": 1,
    "imageUrl": "https://example.com/images/prod001-red-1.jpg",
    "isPrimary": true,
    "isActive": true
  }
}
```

---

### 2. Get Images by Variant ID

```http
GET http://localhost:8082/api/adminProduct/images/variant/1
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 3. Get Active Images by Variant ID

```http
GET http://localhost:8082/api/adminProduct/images/variant/1/active
Authorization: Bearer <ADMIN_TOKEN>
```

---

## Admin Discount Management

### 1. Create Category Discount

```http
POST http://localhost:8082/api/adminProduct/discounts
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "discountCode": "SUMMER20",
  "discountName": "Summer Sale 2026",
  "discountType": "PERCENTAGE",
  "discountValue": 20,
  "applyTo": "CATEGORY",
  "category": "Clothing",
  "startDate": "2026-06-01T00:00:00",
  "endDate": "2026-06-30T23:59:59"
}
```

**Expected Response:**
```json
{
  "message": "Discount created successfully",
  "data": {
    "id": 1,
    "discountCode": "SUMMER20",
    "discountName": "Summer Sale 2026",
    "discountType": "PERCENTAGE",
    "discountValue": 20,
    "applyTo": "CATEGORY",
    "category": "Clothing",
    "isActive": true,
    "isCurrentlyValid": true
  }
}
```

---

### 2. Create Product-Specific Discount

```http
POST http://localhost:8082/api/adminProduct/discounts
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "discountCode": "TSHIRT50",
  "discountName": "T-Shirt Special",
  "discountType": "FLAT",
  "discountValue": 50,
  "applyTo": "PRODUCT",
  "productId": 1,
  "startDate": "2026-02-01T00:00:00",
  "endDate": "2026-02-28T23:59:59"
}
```

---

### 3. Get All Discounts

```http
GET http://localhost:8082/api/adminProduct/discounts
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 4. Get Active Discounts

```http
GET http://localhost:8082/api/adminProduct/discounts/active
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 5. Get Discounts by Type

```http
GET http://localhost:8082/api/adminProduct/discounts/type/CATEGORY
Authorization: Bearer <ADMIN_TOKEN>
```

```http
GET http://localhost:8082/api/adminProduct/discounts/type/PRODUCT
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 6. Update Discount

```http
PUT http://localhost:8082/api/adminProduct/discounts/1
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "discountName": "Summer Mega Sale 2026",
  "discountValue": 25
}
```

---

### 7. Activate Discount

```http
PUT http://localhost:8082/api/adminProduct/discounts/1/activate
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 8. Deactivate Discount

```http
PUT http://localhost:8082/api/adminProduct/discounts/1/deactivate
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 9. Delete Discount

```http
DELETE http://localhost:8082/api/adminProduct/discounts/1
Authorization: Bearer <ADMIN_TOKEN>
```

---

## Admin Review Management

### 1. Delete Review

```http
DELETE http://localhost:8082/api/adminProduct/reviews/1
Authorization: Bearer <ADMIN_TOKEN>
```

**Expected Response:**
```json
{
  "message": "Review deleted successfully"
}
```

---

## Admin Related Products Management

### 1. Add Related Product

```http
POST http://localhost:8082/api/adminProduct/products/1/related
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "relatedProductId": 2,
  "displayOrder": 1
}
```

**Expected Response:**
```json
{
  "message": "Related product added successfully",
  "data": {
    "id": 1,
    "productId": 1,
    "productName": "Cotton T-Shirt",
    "relatedProductId": 2,
    "relatedProductName": "Polo Shirt",
    "displayOrder": 1,
    "isActive": true
  }
}
```

---

### 2. Get Manual Related Products

```http
GET http://localhost:8082/api/adminProduct/products/1/related
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 3. Remove Related Product

```http
DELETE http://localhost:8082/api/adminProduct/products/1/related/2
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 4. Activate Related Product

```http
PUT http://localhost:8082/api/adminProduct/products/1/related/2/activate
Authorization: Bearer <ADMIN_TOKEN>
```

---

### 5. Deactivate Related Product

```http
PUT http://localhost:8082/api/adminProduct/products/1/related/2/deactivate
Authorization: Bearer <ADMIN_TOKEN>
```

---

## Customer Product Browsing

### 1. Get All Published Products

```http
GET http://localhost:8082/api/customerProduct/products/published
```

**No authentication required**

---

### 2. Get Recent Products

```http
GET http://localhost:8082/api/customerProduct/products/recent?limit=10
```

---

### 3. Search by Name

```http
GET http://localhost:8082/api/customerProduct/products/search/name/T-Shirt
```

---

### 4. Filter by Category

```http
GET http://localhost:8082/api/customerProduct/products/category/Clothing
```

---

### 5. Filter by Category and Subcategory

```http
GET http://localhost:8082/api/customerProduct/products/category/Clothing/subcategory/T-Shirts
```

---

### 6. Filter by Brand

```http
GET http://localhost:8082/api/customerProduct/products/brand/Nike
```

---

### 7. Filter by Gender

```http
GET http://localhost:8082/api/customerProduct/products/gender/MALE
```

**Valid values**: MALE, FEMALE, UNISEX

---

### 8. Filter by Age Group

```http
GET http://localhost:8082/api/customerProduct/products/ageGroup/ADULT
```

**Valid values**: KIDS, TEEN, ADULT, SENIOR

---

### 9. Search by Tag

```http
GET http://localhost:8082/api/customerProduct/products/search/tag/summer
```

---

### 10. Get Product by ASIN

```http
GET http://localhost:8082/api/customerProduct/products/getProductByAsin/PROD001
```

---

### 11. Multi-Filter

```http
GET http://localhost:8082/api/customerProduct/products/filter?category=Clothing&gender=MALE&tag=summer
```

---

### 12. Price Range Filter

```http
GET http://localhost:8082/api/customerProduct/products/price-range?min=500&max=1500
```

---

### 13. Get Related Products

```http
GET http://localhost:8082/api/customerProduct/products/1/related
```

**Returns**: Manual related products (if any), otherwise auto-suggested products based on brand & category

---

## Customer Reviews

### 1. Add Review

```http
POST http://localhost:8082/api/customer/reviews
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json

{
  "productId": 1,
  "orderId": 123,
  "rating": 5,
  "reviewTitle": "Excellent quality!",
  "reviewText": "This t-shirt is amazing. Great fabric and perfect fit. Highly recommended!"
}
```

**Expected Response:**
```json
{
  "message": "Review added successfully",
  "data": {
    "id": 1,
    "productId": 1,
    "productName": "Cotton T-Shirt",
    "userId": 456,
    "orderId": 123,
    "rating": 5,
    "reviewTitle": "Excellent quality!",
    "reviewText": "This t-shirt is amazing...",
    "verifiedPurchase": true,
    "createdAt": "2026-02-04T22:30:00"
  }
}
```

---

### 2. Get Product Reviews

```http
GET http://localhost:8082/api/customer/reviews/product/1
```

**No authentication required**

---

### 3. Get Product Rating Info

```http
GET http://localhost:8082/api/customer/reviews/product/1/rating
```

**Expected Response:**
```json
{
  "averageRating": 4.5,
  "reviewCount": 24,
  "ratingDistribution": {
    "5": 15,
    "4": 6,
    "3": 2,
    "2": 1,
    "1": 0
  }
}
```

---

### 4. Get Review by ID

```http
GET http://localhost:8082/api/customer/reviews/1
```

---

### 5. Get My Reviews

```http
GET http://localhost:8082/api/customer/reviews/my-reviews
Authorization: Bearer <CUSTOMER_TOKEN>
```

---

## Testing Workflow

### Complete Product Setup Flow

**Step 1: Create Product**
```http
POST /api/adminProduct/products
{
  "asin": "TEST001",
  "name": "Test Product",
  "category": "Electronics",
  "brand": "TestBrand",
  "mrp": 1999.00
}
```

**Step 2: Create Variant**
```http
POST /api/adminProduct/variants
{
  "productId": 1,
  "skuCode": "TEST001-BLK",
  "color": "Black",
  "mrp": 1999.00
}
```

**Step 3: Create Size**
```http
POST /api/adminProduct/sizes
{
  "variantId": 1,
  "sizeSku": "TEST001-BLK-L",
  "size": "L",
  "quantity": 50
}
```

**Step 4: Create Image**
```http
POST /api/adminProduct/images
{
  "variantId": 1,
  "imageUrl": "https://example.com/test.jpg",
  "isPrimary": true
}
```

**Step 5: Publish Product**
```http
PUT /api/adminProduct/products/1/publish
```

**Step 6: Create Discount**
```http
POST /api/adminProduct/discounts
{
  "discountCode": "TEST10",
  "discountName": "Test Discount",
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "applyTo": "PRODUCT",
  "productId": 1,
  "startDate": "2026-02-01T00:00:00",
  "endDate": "2026-12-31T23:59:59"
}
```

**Step 7: Verify Product (Customer)**
```http
GET /api/customerProduct/products/getProductByAsin/TEST001
```

---

## Common Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid input",
  "message": "Rating must be between 1 and 5"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Access denied. Admin role required."
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Product not found with ID: 999"
}
```

### 409 Conflict
```json
{
  "error": "Conflict",
  "message": "Product with ASIN 'PROD001' already exists"
}
```

---

## Postman Collection Tips

### Environment Variables
Create a Postman environment with:
```
base_url: http://localhost:8082
admin_token: <your_admin_jwt>
customer_token: <your_customer_jwt>
```

### Using Variables in Requests
```
{{base_url}}/api/adminProduct/products
Authorization: Bearer {{admin_token}}
```

### Save Response IDs
After creating a product, save the ID:
```javascript
// In Postman Tests tab
pm.environment.set("product_id", pm.response.json().data.id);
```

Then use it:
```
{{base_url}}/api/adminProduct/products/{{product_id}}
```

---

## Testing Checklist

### Admin Features
- [ ] Create, update, delete products
- [ ] Create, update, delete variants
- [ ] Create, update, delete sizes
- [ ] Create, update, delete images
- [ ] Publish/unpublish products
- [ ] Create, manage discounts
- [ ] Delete reviews
- [ ] Manage related products

### Customer Features
- [ ] Browse published products
- [ ] Search and filter products
- [ ] View product details
- [ ] Add reviews
- [ ] View reviews and ratings
- [ ] View related products

### Discount Testing
- [ ] Category discount applies correctly
- [ ] Product discount overrides category discount
- [ ] Date range validation works
- [ ] Inactive discounts don't apply

### Review Testing
- [ ] Can add review with valid order
- [ ] Cannot add duplicate review
- [ ] Rating updates product average
- [ ] Rating distribution calculates correctly

### Related Products Testing
- [ ] Manual relations show first
- [ ] Auto-suggestions work when no manual relations
- [ ] Cannot add self as related
- [ ] Cannot add duplicate relations

---

**Happy Testing!** 🚀

For issues or questions, refer to DOCUMENTATION.md or IMPLEMENTATION.md
