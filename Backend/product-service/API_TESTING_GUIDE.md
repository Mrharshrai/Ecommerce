# Product Service — API Testing Guide

## Prerequisites

1. All services running:
   - Eureka: `http://localhost:8761`
   - Product Service: `http://localhost:8082`
   - API Gateway (optional): `http://localhost:8080`

2. A valid **Admin JWT token** — obtained from user-service login:
```
POST http://localhost:8081/api/auth/login
Body: { "email": "admin@example.com", "password": "yourpassword" }
```

3. Set the token in headers:
```
Authorization: Bearer <your-admin-jwt-token>
```

---

## Base URLs

| Route | Direct | Via Gateway |
|-------|--------|-------------|
| Admin Product | `http://localhost:8082/api/adminProduct/...` | `http://localhost:8080/api/adminProduct/...` |
| Customer Browse | `http://localhost:8082/api/customerProduct/...` | `http://localhost:8080/api/customerProduct/...` |
| Warehouse (Admin) | `http://localhost:8082/api/adminProduct/warehouses/...` | `http://localhost:8080/api/adminProduct/warehouses/...` |

---

## Product Management

### Create Product
```
POST /api/adminProduct/products
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "name": "Classic Cotton T-Shirt",
  "description": "Premium quality cotton t-shirt",
  "brand": "MeraDesh",
  "basePrice": 599.00,
  "gender": "UNISEX",
  "ageGroup": "ADULT"
}
```

### Publish Product
```
PUT /api/adminProduct/products/{id}/publish
Authorization: Bearer <admin-token>
```

### Get All Products (Admin)
```
GET /api/adminProduct/products
Authorization: Bearer <admin-token>
```

---

## Variant Management

### Create Variant (Color)
```
POST /api/adminProduct/variants
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "productId": 1,
  "colorName": "Navy Blue",
  "colorHex": "#1F2D5A"
}
```

### Publish Variant
```
PUT /api/adminProduct/variants/{id}/publish
Authorization: Bearer <admin-token>
```

---

## Size Management

### Add Size to Variant
```
POST /api/adminProduct/sizes
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "variantId": 1,
  "sizeName": "M",
  "price": 599.00,
  "quantity": 0
}
```
> Set `quantity=0` initially. Add actual stock via warehouse inventory.

### Update Stock Directly (Admin Override)
```
PUT /api/adminProduct/sizes/{id}/update-stock?quantity=50
Authorization: Bearer <admin-token>
```

---

## Discount Management

### Create Discount
```
POST /api/adminProduct/discounts
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "code": "FLAT100",
  "discountType": "FLAT",
  "value": 100.00,
  "applyTo": "ALL",
  "validFrom": "2026-03-01T00:00:00",
  "validUntil": "2026-03-31T23:59:59"
}
```

### Apply Discount to Product
```
POST /api/adminProduct/discounts/apply/{productId}
Authorization: Bearer <admin-token>
Body: { "discountId": 1 }
```

---

## Customer Browsing

### List Published Products
```
GET /api/customerProduct/products
GET /api/customerProduct/products?gender=MALE&brand=MeraDesh&page=0&size=20
```

### Search Products
```
GET /api/customerProduct/products/search?q=cotton+shirt
```

### Get Product Details
```
GET /api/customerProduct/products/{id}
```

### Get Variants of a Product
```
GET /api/customerProduct/variants/{productId}
```

### Get Sizes of a Variant
```
GET /api/customerProduct/sizes/{variantId}
```

---

## Customer Reviews

### Submit Review
```
POST /api/customer/reviews
Authorization: Bearer <customer-token>
Content-Type: application/json

{
  "productId": 1,
  "rating": 5,
  "comment": "Excellent quality, true to size!"
}
```

### Get Reviews for Product
```
GET /api/customer/reviews/product/{productId}
```

---

## Warehouse Management (Admin Only)

> All warehouse endpoints require `ROLE_ADMIN` JWT.

---

### Create Warehouse
```
POST /api/adminProduct/warehouses
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "warehouseCode": "WH-MUM-01",
  "name": "Mumbai Central Warehouse",
  "address": "Plot 12, MIDC, Andheri East",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400093",
  "country": "India",
  "contactNumber": "9876543210",
  "email": "wh-mum@meradesh.com",
  "isDefault": true
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "warehouseCode": "WH-MUM-01",
  "name": "Mumbai Central Warehouse",
  "city": "Mumbai",
  "state": "Maharashtra",
  "isActive": true,
  "isDefault": true,
  "createdAt": "2026-03-01T10:00:00"
}
```

---

### Update Warehouse
```
PUT /api/adminProduct/warehouses/{id}
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "contactNumber": "9876543211",
  "email": "wh-mumbai@meradesh.com"
}
```
> Partial update — only send the fields you want to change.

---

### Get All Warehouses
```
GET /api/adminProduct/warehouses
Authorization: Bearer <admin-token>
```

### Get Active Warehouses Only
```
GET /api/adminProduct/warehouses/active
Authorization: Bearer <admin-token>
```

### Get Default Warehouse
```
GET /api/adminProduct/warehouses/default
Authorization: Bearer <admin-token>
```

### Get Warehouse by Code
```
GET /api/adminProduct/warehouses/code/WH-MUM-01
Authorization: Bearer <admin-token>
```

---

### Set Default Warehouse
```
PUT /api/adminProduct/warehouses/{id}/set-default
Authorization: Bearer <admin-token>
```

### Activate / Deactivate Warehouse
```
PUT /api/adminProduct/warehouses/{id}/activate
PUT /api/adminProduct/warehouses/{id}/deactivate
Authorization: Bearer <admin-token>
```

### Delete Warehouse (Soft Delete)
```
DELETE /api/adminProduct/warehouses/{id}
Authorization: Bearer <admin-token>
```
> Will fail if this is the default warehouse.

---

### Create Inventory Record (First time stock for a warehouse + size)
```
POST /api/adminProduct/warehouses/inventory
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "warehouseId": 1,
  "variantSizeId": 5,
  "quantity": 100
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "warehouseId": 1,
  "warehouseCode": "WH-MUM-01",
  "warehouseName": "Mumbai Central Warehouse",
  "warehouseCity": "Mumbai",
  "variantSizeId": 5,
  "sizeSku": "TSHIRT-NAVY-M",
  "quantity": 100,
  "reservedQuantity": 0,
  "availableQuantity": 100,
  "isActive": true
}
```

---

### Add Stock (New shipment arrived)
```
PUT /api/adminProduct/warehouses/inventory/{inventoryId}/add-stock?quantity=50
Authorization: Bearer <admin-token>
```
> Increases warehouse stock and syncs product_variant_sizes.quantity.

### Deduct Stock (Manual admin correction)
```
PUT /api/adminProduct/warehouses/inventory/{inventoryId}/deduct-stock?quantity=5
Authorization: Bearer <admin-token>
```
> Will fail if available stock < requested quantity.

### Overwrite Inventory Quantity (Stock Count Correction)
```
PUT /api/adminProduct/warehouses/inventory/{inventoryId}?quantity=90
Authorization: Bearer <admin-token>
```
> Sets the quantity to exactly 90. Cannot be less than reservedQuantity.

---

### Get Inventory for a Warehouse
```
GET /api/adminProduct/warehouses/{warehouseId}/inventory
Authorization: Bearer <admin-token>
```

### Get All Warehouses Holding a Specific Size
```
GET /api/adminProduct/warehouses/inventory/variant-size/{variantSizeId}
Authorization: Bearer <admin-token>
```

### Find Best Warehouse for a Size (Order Routing)
```
GET /api/adminProduct/warehouses/inventory/available?sizeSku=TSHIRT-NAVY-M
Authorization: Bearer <admin-token>
```
> Returns the single warehouse with the highest available stock for that size.

### Find ALL Warehouses with Stock for a Size
```
GET /api/adminProduct/warehouses/inventory/available/all?sizeSku=TSHIRT-NAVY-M
Authorization: Bearer <admin-token>
```

### Activate / Deactivate Inventory Record
```
PUT /api/adminProduct/warehouses/inventory/{inventoryId}/activate
PUT /api/adminProduct/warehouses/inventory/{inventoryId}/deactivate
Authorization: Bearer <admin-token>
```

### Delete Inventory Record
```
DELETE /api/adminProduct/warehouses/inventory/{inventoryId}
Authorization: Bearer <admin-token>
```
> Will fail if reservedQuantity > 0.

---

## Common Error Responses

| HTTP Status | When it happens |
|-------------|----------------|
| `400 Bad Request` | Missing required fields, invalid data |
| `401 Unauthorized` | No JWT token provided |
| `403 Forbidden` | JWT valid but role is not ADMIN |
| `404 Not Found` | Warehouse / inventory / product ID does not exist |
| `500 Internal Server Error` | Business rule violation (e.g., deleting default warehouse, insufficient stock) — check the message field |

---

## Recommended Testing Sequence (Warehouse)

Follow this order for a complete test run:

```
1. Create Warehouse (isDefault=true)         → POST /warehouses
2. Create Product + Variant + Size           → POST /products, /variants, /sizes
3. Note the variantSizeId from created size
4. Create Inventory Record                   → POST /warehouses/inventory
5. Verify inventory visible                  → GET /warehouses/{id}/inventory
6. Add more stock (new shipment)             → PUT /warehouses/inventory/{id}/add-stock?quantity=50
7. Deduct stock (simulating order)           → PUT /warehouses/inventory/{id}/deduct-stock?quantity=2
8. Find best warehouse for order             → GET /warehouses/inventory/available?sizeSku=...
9. Create second warehouse                   → POST /warehouses
10. Add inventory to second warehouse        → POST /warehouses/inventory
11. Switch default                           → PUT /warehouses/{id}/set-default
12. Delete first warehouse (should fail if default) → DELETE /warehouses/{id}
```

---

## Troubleshooting

| Problem | Likely Cause | Fix |
|---------|-------------|-----|
| 403 on all admin requests | Token does not have ROLE_ADMIN | Login as admin user, check roles in JWT |
| "Warehouse code already exists" | Duplicate warehouseCode | Use a unique code |
| "Cannot delete default warehouse" | Trying to delete the default | Set another warehouse as default first |
| "Inventory already exists" | Calling addInventory twice for same warehouse+size | Use addStock or updateInventory instead |
| "Insufficient stock" | Available stock < requested deduction | Check stock first via GET /inventory |
| Product size quantity not matching | Warehouse sync failed mid-transaction | Check logs; transactions are atomic |
