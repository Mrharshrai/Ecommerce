# User Service - API Testing Guide

## Prerequisites

### Services Running:
1. Eureka Server (port 8761)
2. User Service (port 8081)

### Test User Credentials:
- Email: `test@example.com`
- Password: `Test@1234`

---

## 1. Authentication APIs

### 1.1 User Registration
```
POST http://localhost:8081/api/users/register/customer
Content-Type: application/json

{
  "name": "Test User",
  "email": "test@example.com",
  "mobile": "9876543210",
  "password": "Test@1234"
}
```

**Expected Response:** 201 Created
```json
{
  "id": 1,
  "name": "Test User",
  "email": "test@example.com",
  "mobile": "9876543210",
  "userCode": "CUST0001",
  "roles": ["CUSTOMER"]
}
```

---

### 1.2 Login
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "Test@1234"
}
```

**Expected Response:** 200 OK
```json
{
  "token": "eyJhbGci...",
  "refreshToken": "eyJhbGci...",
  "user": {
    "id": 1,
    "name": "Test User",
    "email": "test@example.com",
    "userCode": "CUST0001",
    "roles": ["CUSTOMER"]
  }
}
```

**Note:** Save both `token` and `refreshToken` for subsequent requests.

---

### 1.3 Logout
```
POST http://localhost:8081/api/auth/logout
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```
"Logged out successfully"
```

**Verification:** Try using the same token again - should get 401 Unauthorized.

---

### 1.4 Refresh Token
```
POST http://localhost:8081/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "YOUR_REFRESH_TOKEN"
}
```

**Expected Response:** 200 OK
```json
{
  "token": "NEW_ACCESS_TOKEN",
  "refreshToken": "SAME_REFRESH_TOKEN"
}
```

---

### 1.5 Forgot Password
```
POST http://localhost:8081/api/auth/forgot-password
Content-Type: application/json

{
  "email": "test@example.com"
}
```

**Expected Response:** 200 OK
```
"OTP sent to your email"
```

**Action Required:** Check email inbox for 6-digit OTP.

---

### 1.6 Reset Password
```
POST http://localhost:8081/api/auth/reset-password
Content-Type: application/json

{
  "email": "test@example.com",
  "otp": "123456",
  "newPassword": "NewPass@1234"
}
```

**Expected Response:** 200 OK
```
"Password reset successfully"
```

**Verification:** Login with new password.

---

### 1.7 Send Email Verification OTP (Customer Only)
```
POST http://localhost:8081/api/auth/verify-email/send-otp
Authorization: Bearer CUSTOMER_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```
"Verification OTP sent to your email"
```

**Note:** Only CUSTOMER role can access this endpoint. ADMIN and DRIVER will receive 403 Forbidden.

**Action Required:** Check email inbox for 6-digit OTP.

---

### 1.8 Verify Email (Customer Only)
```
POST http://localhost:8081/api/auth/verify-email/verify-otp
Authorization: Bearer CUSTOMER_ACCESS_TOKEN
Content-Type: application/json

{
  "otp": "123456"
}
```

**Expected Response:** 200 OK
```
"Email verified successfully"
```

**Note:** Only CUSTOMER role can access this endpoint. ADMIN and DRIVER will receive 403 Forbidden.

---

### 1.9 Generate Internal JWT Token (Admin Only)
```
POST http://localhost:8081/api/auth/generate-internal-token
Authorization: Bearer ADMIN_ACCESS_TOKEN
Content-Type: application/json

{
  "serviceName": "order-service"
}
```

**Expected Response:** 200 OK
```json
{
  "serviceName": "order-service",
  "internalToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "expiresIn": "1 hour",
  "usage": "Add this token in Authorization header: Bearer <token>",
  "allowedEndpoints": "/internal/**"
}
```

---

## 2. User Profile Management

### 2.1 View Own Profile (Customer)
```
GET http://localhost:8081/api/users/{userId}
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "id": 1,
  "name": "Test User",
  "email": "test@example.com",
  "mobile": "9876543210",
  "userCode": "CUST0001",
  "roles": ["CUSTOMER"]
}
```

---

### 2.2 Update Customer Profile
```
PUT http://localhost:8081/api/users/updateCustomer/1
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "name": "Updated Name",
  "email": "newemail@example.com",
  "mobile": "9999999999",
  "password": "NewPassword@123"
}
```

**Expected Response:** 200 OK
```json
{
  "id": 1,
  "name": "Updated Name",
  "email": "newemail@example.com",
  "mobile": "9999999999",
  "userCode": "CUST0001",
  "roleName": "CUSTOMER"
}
```

**Note:** All fields are optional. Only provided fields will be updated.

---

## 3. Admin Customer Management

### 3.1 Get Active Customers (Admin Only)
```
GET http://localhost:8081/api/users/customers/active?page=0&size=10
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "content": [
    {
      "id": 1,
      "name": "Test User",
      "email": "test@example.com",
      "mobile": "9876543210",
      "userCode": "CUST0001",
      "roleName": "CUSTOMER"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 3.2 Get Customer Details (Admin Only)
```
GET http://localhost:8081/api/users/customers/1
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "id": 1,
  "name": "Test User",
  "email": "test@example.com",
  "mobile": "9876543210",
  "userCode": "CUST0001",
  "roleName": "CUSTOMER"
}
```

---

### 3.3 Activate Customer (Admin Only)
```
PUT http://localhost:8081/api/users/customers/1/activate
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```
"User activated successfully"
```

---

### 3.4 Deactivate Customer (Admin Only)
```
PUT http://localhost:8081/api/users/customers/1/deactivate
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```
"User deactivated successfully"
```

**Verification:** Try logging in with deactivated user - should fail with "Account is deactivated" message.

---

### 3.5 Delete Customer Account (Admin Only)
```
DELETE http://localhost:8081/api/users/deleteAccount/1
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```
"User deleted successfully"
```

---

## 4. Customer Self-Service

### 4.1 Deactivate Own Account
```
PUT http://localhost:8081/api/users/customer/deactivate
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```
"Your account has been deactivated"
```

**Verification:** Try logging in - should fail with "Account is deactivated" message.

---

## 5. Address Management

**Note:** All address endpoints require authentication (Bearer token).

### 5.1 Add Address
```
POST http://localhost:8081/api/addresses
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "addressLine1": "123 Main Street",
  "addressLine2": "Apartment 4B",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001"
}
```

**Expected Response:** 201 Created
```json
{
  "id": 1,
  "addressLine1": "123 Main Street",
  "addressLine2": "Apartment 4B",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001",
  "isDefault": true,
  "createdAt": "2026-01-28T10:30:00",
  "updatedAt": "2026-01-28T10:30:00"
}
```

**Note:** First address is automatically set as default.

---

### 5.2 Get All Addresses
```
GET http://localhost:8081/api/addresses
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
[
  {
    "id": 1,
    "addressLine1": "123 Main Street",
    "addressLine2": "Apartment 4B",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001",
    "isDefault": true,
    "createdAt": "2026-01-28T10:30:00",
    "updatedAt": "2026-01-28T10:30:00"
  }
]
```

---

### 5.3 Update Address
```
PUT http://localhost:8081/api/addresses/1
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "addressLine1": "123 Main Street UPDATED",
  "addressLine2": "Apartment 5C",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400002"
}
```

**Expected Response:** 200 OK
```json
{
  "id": 1,
  "addressLine1": "123 Main Street UPDATED",
  "addressLine2": "Apartment 5C",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400002",
  "isDefault": true,
  "createdAt": "2026-01-28T10:30:00",
  "updatedAt": "2026-01-28T10:35:00"
}
```

---

### 5.4 Set Default Address
```
PUT http://localhost:8081/api/addresses/2/set-default
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "id": 2,
  "addressLine1": "456 Park Avenue",
  "city": "Delhi",
  "state": "Delhi",
  "pincode": "110001",
  "isDefault": true,
  "createdAt": "2026-01-28T10:32:00",
  "updatedAt": "2026-01-28T10:36:00"
}
```

**Note:** Previous default address is automatically unset.

---

### 5.5 Delete Address
```
DELETE http://localhost:8081/api/addresses/1
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```
"Address deleted successfully"
```

**Note:** If deleted address was default, another address becomes default automatically.

---

## 6. Analytics & Monitoring (Admin Only)

### 6.1 Get User Activity History
```
GET http://localhost:8081/api/analytics/users/1/activity?page=0&size=10
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "content": [
    {
      "id": 1,
      "userId": 1,
      "userEmail": "test@example.com",
      "userRole": "CUSTOMER",
      "activityType": "LOGIN",
      "description": "User logged in successfully",
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "timestamp": "2026-01-28T10:00:00"
    }
  ],
  "totalElements": 25
}
```

---

### 6.2 Get Last Login Time
```
GET http://localhost:8081/api/analytics/users/1/last-login
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "userId": 1,
  "lastLoginTime": "2026-01-28T10:00:00",
  "hasLoggedIn": true
}
```

---

### 6.3 Get Login Frequency
```
GET http://localhost:8081/api/analytics/users/1/login-frequency?hours=1
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "userId": 1,
  "loginCount": 3,
  "timeWindow": "1 hour(s)",
  "suspicious": false
}
```

**Note:** Flags as suspicious if more than 5 logins in time window.

---

### 6.4 Get System Statistics
```
GET http://localhost:8081/api/analytics/system/statistics
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "totalLogins": 1250,
  "totalRegistrations": 450,
  "loginsToday": 85,
  "registrationsToday": 12,
  "loginsThisWeek": 520,
  "registrationsThisWeek": 67
}
```

---

### 6.5 Get Recent Activities
```
GET http://localhost:8081/api/analytics/activities/recent?page=0&size=20
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "content": [
    {
      "id": 10,
      "userId": 5,
      "userEmail": "user@example.com",
      "userRole": "CUSTOMER",
      "activityType": "REGISTRATION",
      "description": "Customer registered: User Name",
      "timestamp": "2026-01-28T11:00:00"
    }
  ]
}
```

---

### 6.6 Get Admin Activities
```
GET http://localhost:8081/api/analytics/activities/admin?page=0&size=20
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "content": [
    {
      "id": 5,
      "userId": 3,
      "userEmail": "customer@example.com",
      "userRole": "CUSTOMER",
      "activityType": "ADMIN_ACTION",
      "description": "Account deactivated by admin",
      "timestamp": "2026-01-27T15:30:00"
    }
  ]
}
```

---

## 7. Internal APIs (For Microservices)

**Note:** These endpoints require Internal JWT token.

### 7.1 Get User by ID
```
GET http://localhost:8081/internal/users/1
Authorization: Bearer INTERNAL_JWT_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "id": 1,
  "name": "Test User",
  "email": "test@example.com",
  "userCode": "CUST0001",
  "roles": ["CUSTOMER"],
  "active": true,
  "deleted": false,
  "emailVerified": true,
  "mobileVerified": false
}
```

---

### 7.2 Validate Token
```
POST http://localhost:8081/internal/validate-token
Authorization: Bearer INTERNAL_JWT_TOKEN
Content-Type: application/json

{
  "token": "USER_JWT_TOKEN",
  "role": "CUSTOMER"
}
```

**Expected Response:** 200 OK
```json
{
  "valid": true,
  "userId": 1,
  "email": "test@example.com",
  "roles": ["ROLE_CUSTOMER"],
  "hasRequiredRole": true
}
```

**Invalid Token Response:**
```json
{
  "valid": false,
  "message": "Invalid token"
}
```

---

### 7.3 Check Order Eligibility
```
GET http://localhost:8081/internal/users/1/can-place-order
Authorization: Bearer INTERNAL_JWT_TOKEN
```

**Expected Response:** 200 OK
```json
{
  "userId": 1,
  "canPlaceOrder": true,
  "emailVerified": true,
  "active": true,
  "deleted": false,
  "reason": "Eligible to place order"
}
```

**Ineligible Response:**
```json
{
  "userId": 1,
  "canPlaceOrder": false,
  "emailVerified": false,
  "active": true,
  "deleted": false,
  "reason": "Email not verified"
}
```

---

## 8. Validation Test Cases

### 8.1 Duplicate Email Registration
```
POST http://localhost:8081/api/users/register/customer
Content-Type: application/json

{
  "name": "Another User",
  "email": "test@example.com",
  "mobile": "9999999999",
  "password": "Test@1234"
}
```

**Expected Response:** 400 Bad Request
```
"Email already exists: test@example.com"
```

---

### 8.2 Duplicate Mobile Registration
```
POST http://localhost:8081/api/users/register/customer
Content-Type: application/json

{
  "name": "Another User",
  "email": "another@example.com",
  "mobile": "9876543210",
  "password": "Test@1234"
}
```

**Expected Response:** 400 Bad Request
```
"Mobile number already in use: 9876543210"
```

---

### 8.3 Invalid Pincode (Address)
```
POST http://localhost:8081/api/addresses
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "addressLine1": "Test Address",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "12345"
}
```

**Expected Response:** 400 Bad Request
```
"Pincode must be 6 digits"
```

---

### 8.4 Missing Required Fields (Address)
```
POST http://localhost:8081/api/addresses
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "addressLine1": "Test Address"
}
```

**Expected Response:** 400 Bad Request
```
Validation errors for missing city, state, pincode
```

---

### 8.5 Invalid Password Length (Reset)
```
POST http://localhost:8081/api/auth/reset-password
Content-Type: application/json

{
  "email": "test@example.com",
  "otp": "123456",
  "newPassword": "short"
}
```

**Expected Response:** 400 Bad Request
```
"Password must be at least 8 characters"
```

---

### 8.6 Expired OTP
```
POST http://localhost:8081/api/auth/reset-password
Content-Type: application/json

{
  "email": "test@example.com",
  "otp": "EXPIRED_OTP",
  "newPassword": "NewPass@1234"
}
```

**Expected Response:** 400 Bad Request
```
"Invalid or expired OTP"
```

---

## 9. Security Test Cases

### 9.1 Unauthorized Access (No Token)
```
GET http://localhost:8081/api/addresses
```

**Expected Response:** 401 Unauthorized

---

### 9.2 Using Blacklisted Token
```
POST http://localhost:8081/api/auth/logout
Authorization: Bearer YOUR_TOKEN

# Then try using the same token
GET http://localhost:8081/api/addresses
Authorization: Bearer SAME_TOKEN
```

**Expected Response:** 401 Unauthorized
```
"Token has been invalidated"
```

---

### 9.3 Access Other User's Address
**Create User 2:**
```
POST http://localhost:8081/api/users/register/customer
Content-Type: application/json

{
  "name": "User Two",
  "email": "user2@example.com",
  "mobile": "8888888888",
  "password": "Test@1234"
}
```

**Login as User 2 and try to update User 1's address:**
```
PUT http://localhost:8081/api/addresses/1
Authorization: Bearer USER_TWO_TOKEN
Content-Type: application/json

{
  "addressLine1": "Hacker Address",
  "city": "Test",
  "state": "Test",
  "pincode": "123456"
}
```

**Expected Response:** 404 Not Found

---

### 9.4 Deactivated User Login Attempt
```
# Admin deactivates user
PUT http://localhost:8081/api/users/customers/1/deactivate
Authorization: Bearer ADMIN_TOKEN

# User tries to login
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "Test@1234"
}
```

**Expected Response:** 400 Bad Request
```
"Account is deactivated. Please contact support."
```

---

### 9.5 Internal Endpoint Without Internal JWT
```
GET http://localhost:8081/internal/users/1
```

**Expected Response:** 401 Unauthorized
```
"Missing or invalid internal token"
```

---

## 10. Complete Test Flow

### Recommended Testing Sequence:

1. **Registration & Login**
   - Register new user
   - Login and save tokens
   - Verify activity logged

2. **Email Verification**
   - Send verification OTP
   - Verify email with OTP
   - Verify activity logged

3. **Profile Management**
   - View own profile
   - Update profile (name, email, mobile)
   - Verify uniqueness validation
   - Verify activity logged

4. **Address Management**
   - Add first address (auto default)
   - Add second address
   - Get all addresses
   - Update address
   - Set default address
   - Delete address

5. **Password Reset**
   - Request forgot password OTP
   - Reset password with OTP
   - Login with new password
   - Verify activity logged

6. **Token Management**
   - Use refresh token to get new access token
   - Logout (blacklist token)
   - Verify token is blacklisted

7. **Admin Management** (Admin user required)
   - View active customers
   - View customer details
   - Deactivate customer
   - Verify customer cannot login
   - Activate customer
   - Verify activity logged

8. **Analytics** (Admin user required)
   - View user activity history
   - Check last login time
   - Check login frequency
   - View system statistics
   - View recent activities
   - View admin activities

9. **Internal APIs** (Admin user required)
   - Generate internal JWT token
   - Use internal token to get user info
   - Validate user token
   - Check order eligibility

---

## 11. Error Codes Reference

| Status Code | Meaning |
|-------------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (invalid/missing token) |
| 404 | Not Found (resource doesn't exist) |
| 500 | Internal Server Error |

---

## 11. Recent Changes & Improvements

### Email Verification Access Control
**Change:** Email verification endpoints now restricted to CUSTOMER role only.

**Affected Endpoints:**
- `POST /api/auth/verify-email/send-otp` - Customer only
- `POST /api/auth/verify-email/verify-otp` - Customer only

**Impact:** ADMIN and DRIVER users will receive 403 Forbidden when attempting to access these endpoints.

**Testing:**
```
# As ADMIN (will fail)
POST /api/auth/verify-email/send-otp
Authorization: Bearer ADMIN_TOKEN
Expected: 403 Forbidden

# As CUSTOMER (will work)
POST /api/auth/verify-email/send-otp
Authorization: Bearer CUSTOMER_TOKEN
Expected: 200 OK
```

### Address Validation for Orders
**Change:** Order eligibility now requires user to have at least one delivery address.

**Affected Endpoint:**
- `GET /internal/users/{userId}/can-place-order`

**New Response Fields:**
- `hasAddress` (boolean) - Whether user has any addresses
- `addressCount` (integer) - Number of addresses user has

**Order Requirements (Updated):**
1. User must be active
2. User must not be deleted
3. Email must be verified
4. User must have at least one address (NEW)

**Testing:**
```
# User with no address
GET /internal/users/1/can-place-order
Authorization: Bearer INTERNAL_TOKEN

Response:
{
  "canPlaceOrder": false,
  "hasAddress": false,
  "addressCount": 0,
  "reason": "No delivery address added"
}

# Add address then check again
POST /api/addresses
Authorization: Bearer CUSTOMER_TOKEN
{
  "addressLine1": "123 Main St",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001"
}

GET /internal/users/1/can-place-order
Response:
{
  "canPlaceOrder": true,
  "hasAddress": true,
  "addressCount": 1,
  "reason": "Eligible to place order"
}
```

---

## 12. Common Issues & Solutions

### Issue: Email not received
**Solution:** 
- Check spam folder
- Verify Gmail SMTP credentials in application.properties
- Ensure Gmail App Password is correct

### Issue: Token expired
**Solution:** 
- Use refresh token to get new access token
- Or login again

### Issue: 401 Unauthorized
**Solution:**
- Check if token is included in Authorization header
- Verify token format: `Bearer YOUR_TOKEN`
- Check if token is blacklisted (after logout)

### Issue: Address validation failed
**Solution:**
- Ensure pincode is exactly 6 digits
- Check all required fields are provided
- Verify field formats match validation rules

### Issue: Duplicate email/mobile error
**Solution:**
- Use unique email and mobile for each user
- Check if user already exists before registration

### Issue: Deactivated user cannot login
**Solution:**
- Admin must activate the account
- Use activate endpoint: `PUT /api/users/customers/{id}/activate`

### Issue: Internal endpoint returns 401
**Solution:**
- Generate internal JWT token using admin account
- Include token in Authorization header: `Bearer INTERNAL_TOKEN`
- Verify token has not expired (1 hour validity)

---

## 13. Unit Tests

### Running Unit Tests
The User Service includes unit tests to verify core functionality.

**Command:**
```bash
mvn test
```

**Test File:** `src/test/java/com/shop/userservice/user_service/UserServiceApplicationTests.java`

**Test Cases:**
1. Context Loads - Verifies Spring Boot application starts
2. User Entity Creation - Tests User entity properties
3. Role Enum Validation - Validates CUSTOMER, ADMIN, DRIVER roles
4. User Role Assignment - Tests multiple role assignment
5. User Default Values - Validates boolean flags

**Expected Result:** All 5 tests pass

---

## 14. Postman Collection

A complete Postman collection with all endpoints and test cases will be provided separately for easy import and testing.

---

**Document Version:** 2.1  
**Last Updated:** January 30, 2026  
**Total Endpoints Tested:** 40+

