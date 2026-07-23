# USER SERVICE

**PORT: 8081**

## INTRODUCTION
User Service handles Customer, Admin, and Driver registration/login with comprehensive account management, address management, activity tracking, and inter-service communication capabilities.

## BACKEND STRUCTURE

**Data Base Storage:** MySQL  
**Data Base Name:** USERDB  
**Data Base Tables:** users, addresses, token_blacklist, otp, user_activity_logs

### CLASS PRINTS AND DB DATA

| S.No. | Class | Properties/Attributes | Table name | Column names in DB |
|-------|-------|----------------------|------------|-------------------|
| 1 | User | Long id<br>String name<br>String email<br>String mobile<br>String password<br>String roleName<br>String userCode<br>Set&lt;Role&gt; roles<br>String refreshToken<br>LocalDateTime refreshTokenExpiry<br>boolean emailVerified<br>boolean mobileVerified<br>boolean active<br>boolean deleted | users | id<br>name<br>email<br>mobile<br>password<br>role_name<br>user_code<br>refresh_token<br>refresh_token_expiry<br>email_verified<br>mobile_verified<br>active<br>deleted |
| 2 | Address | Long id<br>Long userId<br>String addressLine1<br>String addressLine2<br>String city<br>String state<br>String pincode<br>boolean isDefault<br>LocalDateTime createdAt<br>LocalDateTime updatedAt | addresses | id<br>user_id<br>address_line1<br>address_line2<br>city<br>state<br>pincode<br>is_default<br>created_at<br>updated_at |
| 3 | TokenBlacklist | Long id<br>String token<br>LocalDateTime expiryTime<br>LocalDateTime createdAt | token_blacklist | id<br>token<br>expiry_time<br>created_at |
| 4 | OTP | Long id<br>String identifier<br>String otp<br>OTPPurpose purpose<br>LocalDateTime expiryTime<br>boolean verified<br>LocalDateTime createdAt | otp | id<br>identifier<br>otp<br>purpose<br>expiry_time<br>verified<br>created_at |
| 5 | UserActivityLog | Long id<br>Long userId<br>String userEmail<br>String userRole<br>ActivityType activityType<br>String description<br>String ipAddress<br>String userAgent<br>LocalDateTime timestamp | user_activity_logs | id<br>user_id<br>user_email<br>user_role<br>activity_type<br>description<br>ip_address<br>user_agent<br>timestamp |

### ENUMS

| S.No. | Enum | Constants |
|-------|------|-----------|
| 1 | Role | CUSTOMER, ADMIN, DRIVER |
| 2 | OTPPurpose | EMAIL_VERIFICATION, MOBILE_VERIFICATION, FORGOT_PASSWORD |
| 3 | ActivityType | REGISTRATION, LOGIN, LOGOUT, PROFILE_UPDATE, EMAIL_CHANGE, PASSWORD_CHANGE, ADMIN_ACTION |

---

## SERVICE FUNCTIONALITIES BASED ON ROLES AND API END-POINTS

### 1. ADMIN FUNCTIONALITIES

| S.No. | Role | Functionality | Token Requirement | Details/Endpoints | Status |
|-------|------|--------------|-------------------|-------------------|--------|
| 1 | Admin | ADMIN can register DRIVERS | Yes | /api/users/register/driver | Done |
| 2 | Admin | ADMIN can view all users | Yes | /api/users/all | Done |
| 3 | Admin | ADMIN can view user by id | Yes | /api/users/{id} | Done |
| 4 | Admin | ADMIN can update its own password only | Yes | /api/users/updateEmployee/{id} | Done |
| 5 | Admin | ADMIN can update driver's name/email/vehicle number/phone number only | Yes | /api/users/updateDriverDetails/{id} | Done |
| 6 | Admin | Admin can delete any customer's account | Yes | /api/users/deleteAccount/{id} | Done |
| 7 | Admin | Admin can soft delete any driver's account | Yes | /api/users/softDeleteEmployee/{id} | Done |
| 8 | Admin | Admin can get list of active customers (paginated) | Yes | /api/users/customers/active | Done |
| 9 | Admin | Admin can get one customer details | Yes | /api/users/customers/{id} | Done |
| 10 | Admin | Admin can activate customer account | Yes | /api/users/customers/{id}/activate | Done |
| 11 | Admin | Admin can deactivate customer account | Yes | /api/users/customers/{id}/deactivate | Done |
| 12 | Admin | Admin can generate internal JWT token for microservices | Yes | /api/auth/generate-internal-token | Done |
| 13 | Admin | Admin can view user activity history | Yes | /api/analytics/users/{userId}/activity | Done |
| 14 | Admin | Admin can view last login time | Yes | /api/analytics/users/{userId}/last-login | Done |
| 15 | Admin | Admin can view login frequency | Yes | /api/analytics/users/{userId}/login-frequency | Done |
| 16 | Admin | Admin can view system statistics | Yes | /api/analytics/system/statistics | Done |
| 17 | Admin | Admin can view recent activities | Yes | /api/analytics/activities/recent | Done |
| 18 | Admin | Admin can view admin action logs | Yes | /api/analytics/activities/admin | Done |

### 2. CUSTOMER FUNCTIONALITIES

| S.No. | Role | Functionality | Token Requirement | Details/Endpoints | Status |
|-------|------|--------------|-------------------|-------------------|--------|
| 1 | Customer | Anyone can register as CUSTOMER | No | /api/users/register/customer | Done |
| 2 | Customer | LoggedIn customer can update name/email/mobile/password | Yes | /api/users/updateCustomer/{id} | Done |
| 3 | Customer | LoggedIn customer can delete its own account | Yes | /api/users/deleteAccount/{id} | Done |
| 4 | Customer | LoggedIn customer can deactivate its own account | Yes | /api/users/customer/deactivate | Done |
| 5 | Customer | Customer can add multiple addresses | Yes | /api/addresses | Done |
| 6 | Customer | Customer can view all saved addresses | Yes | /api/addresses | Done |
| 7 | Customer | Customer can update address | Yes | /api/addresses/{id} | Done |
| 8 | Customer | Customer can delete address | Yes | /api/addresses/{id} | Done |
| 9 | Customer | Customer can set default delivery address | Yes | /api/addresses/{id}/set-default | Done |
| 10 | Customer | Customer can send email verification OTP (Customer only) | Yes | /api/auth/verify-email/send-otp | Done |
| 11 | Customer | Customer can verify email with OTP (Customer only) | Yes | /api/auth/verify-email/verify-otp | Done |

### 3. DRIVER FUNCTIONALITIES

| S.No. | Role | Functionality | Token Requirement | Details/Endpoints | Status |
|-------|------|--------------|-------------------|-------------------|--------|
| 1 | Driver | Driver can update its own password only | Yes | /api/users/updateEmployee/{id} | Done |

### 4. COMMON FUNCTIONALITIES (Admin, Customer, Driver)

| S.No. | Role | Functionality | Token Requirement | Details/Endpoints | Status |
|-------|------|--------------|-------------------|-------------------|--------|
| 1 | All | Only registered user can log in, then only token will be generated | No | /api/auth/login | Done |
| 2 | All | User can logout (token blacklisting) | Yes | /api/auth/logout | Done |
| 3 | All | User can refresh access token | No | /api/auth/refresh | Done |
| 4 | All | User can request forgot password OTP | No | /api/auth/forgot-password | Done |
| 5 | All | User can reset password with OTP | No | /api/auth/reset-password | Done |

### 5. INTERNAL MICROSERVICE APIs

| S.No. | Functionality | Token Requirement | Details/Endpoints | Status |
|-------|--------------|-------------------|-------------------|--------|
| 1 | Get user basic info by userId | Yes (Internal JWT) | /internal/users/{userId} | Done |
| 2 | Validate token and role | Yes (Internal JWT) | /internal/validate-token | Done |
| 3 | Check if user can place order | Yes (Internal JWT) | /internal/users/{userId}/can-place-order | Done |

---

## TEST CASES

### 1. Register Customer

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 1 | **Register Customer** | | | | | |
| | name<br>email<br>mobile<br>password | id<br>name<br>email<br>mobile<br>userCode<br>roleName<br>roles | No | name, email, mobile, password present | User details, with status 201 CREATED | Done |
| | | | | name blank | message: "name is required" | Done |
| | | | | Email blank | message: "email is required" | Done |
| | | | | Email format wrong | message: "invalid email" | Done |
| | | | | Email should not contain @meradesh.com | message = "Customer email cannot use @meradesh.com domain" | Done |
| | | | | Password blank | message: "password is required" | Done |
| | | | | Password format wrong | message = "password must be at least 8 characters" | Done |
| | | | | provided email id must not already exist | "Customer already exists with email: " + dto.getEmail() | Done |
| | | | | provided mobile number must not already exist | "Mobile number already in use: " + dto.getMobile() | Done |

### 2. Login

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 2 | **Login** | | | | | |
| | email<br>password | token<br>refreshToken<br>user details | No | valid email and password | JWT token, refresh token, user details with status 200 OK | Done |
| | | | | invalid email or password | message: "Invalid Email or Password" | Done |
| | | | | user account is deactivated | message: "Account is deactivated. Please contact support." | Done |
| | | | | user account is deleted (non-driver) | message: "Account has been deleted. Please contact support." | Done |

### 3. Logout

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 3 | **Logout** | | | | | |
| | N/A | message | Yes | valid token provided | "Logged out successfully" with status 200 OK | Done |
| | | | | no token provided | "No token provided" | Done |
| | | | | token already blacklisted | "Token has been invalidated" | Done |

### 4. Refresh Token

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 4 | **Refresh Token** | | | | | |
| | refreshToken | token<br>refreshToken | No | valid refresh token | new access token with status 200 OK | Done |
| | | | | invalid or expired refresh token | message: "Invalid or expired refresh token" | Done |

### 5. Forgot Password

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 5 | **Forgot Password** | | | | | |
| | email | message | No | valid email exists in system | "OTP sent to your email" with status 200 OK | Done |
| | | | | email not found | "User not found with email: " + email | Done |

### 6. Reset Password

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 6 | **Reset Password** | | | | | |
| | email<br>otp<br>newPassword | message | No | valid email, OTP, and password | "Password reset successfully" with status 200 OK | Done |
| | | | | invalid or expired OTP | "Invalid or expired OTP" | Done |
| | | | | password too short | "password must be at least 8 characters" | Done |

### 7. Get List of All Users

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 7 | **Get list of all users** | | | | | |
| | N/A | List&lt;&gt;<br>[{},{}] | Yes | Only ADMIN can get list of all users | List of User details, with status 200 OK | Done |
| | | | | Not admin | Access Denied | Done |
| | | | | If no data present in DB | message="No users found in the system" | Done |

### 8. Get User by ID

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 8 | **Get user by ID** | | | | | |
| | id | user details | Yes | Only ADMIN can get user by id | User details, with status 200 OK | Done |
| | | | | Not admin | Access Denied | Done |
| | | | | If no user exist with given user id in DB | "User not found with ID: " + id | Done |

### 9. Customer Updating Themselves

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 9 | **Customer updating themselves (can update name, email, mobile and password)** | | | | | |
| | name<br>email<br>mobile<br>password | id<br>name<br>email<br>mobile<br>userCode<br>roleName<br>roles | Yes | Only LOGGED In CUSTOMER can change his/her name, email, mobile or password | Updated user details, with status 200 OK | Done |
| | | | | Other Logged in trying to update others field | "You can update only your own account." | Done |
| | | | | Email format wrong | message: "invalid email" | Done |
| | | | | Email should not contain @meradesh.com | message = "Customer email cannot use @meradesh.com domain" | Done |
| | | | | Password format wrong | message = "password must be at least 8 characters" | Done |
| | | | | If no user exist with given user id in DB | "User not found with ID: " + id | Done |
| | | | | Newly requested email id must not be in use with other user | "Email already in use: " + dto.getEmail() | Done |
| | | | | Newly requested mobile must not be in use with other user | "Mobile number already in use: " + dto.getMobile() | Done |

### 10. Get Active Customers (Admin Only)

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 10 | **Get Active Customers** | | | | | |
| | page<br>size | Page&lt;User&gt; | Yes | Only ADMIN can get active customers | Paginated list of active customers with status 200 OK | Done |
| | | | | Not admin | Access Denied | Done |

### 11. Activate Customer (Admin Only)

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 11 | **Activate Customer** | | | | | |
| | customerId | message | Yes | Only ADMIN can activate customer | "User activated successfully" with status 200 OK | Done |
| | | | | Not admin | Access Denied | Done |
| | | | | User not found | "User not found with ID: " + id | Done |
| | | | | User is not a customer | "Only customer accounts can be activated via this endpoint" | Done |

### 12. Deactivate Customer (Admin Only)

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 12 | **Deactivate Customer** | | | | | |
| | customerId | message | Yes | Only ADMIN can deactivate customer | "User deactivated successfully" with status 200 OK | Done |
| | | | | Not admin | Access Denied | Done |
| | | | | User not found | "User not found with ID: " + id | Done |
| | | | | User is not a customer | "Only customer accounts can be deactivated via this endpoint" | Done |

### 13. Customer Self-Deactivate

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 13 | **Customer Self-Deactivate** | | | | | |
| | N/A | message | Yes | Logged in customer deactivates own account | "Your account has been deactivated" with status 200 OK | Done |
| | | | | Not logged in | Access Denied | Done |

### 14. Add Address

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 14 | **Add Address** | | | | | |
| | addressLine1<br>addressLine2<br>city<br>state<br>pincode | id<br>addressLine1<br>addressLine2<br>city<br>state<br>pincode<br>isDefault<br>createdAt<br>updatedAt | Yes | All required fields present | Address details with status 201 CREATED | Done |
| | | | | addressLine1 blank | message: "addressLine1 is required" | Done |
| | | | | city blank | message: "city is required" | Done |
| | | | | state blank | message: "state is required" | Done |
| | | | | pincode blank | message: "pincode is required" | Done |
| | | | | pincode not 6 digits | message: "Pincode must be 6 digits" | Done |
| | | | | First address added | isDefault automatically set to true | Done |

### 15. Update Address

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 15 | **Update Address** | | | | | |
| | addressId<br>addressLine1<br>addressLine2<br>city<br>state<br>pincode | updated address details | Yes | User updates own address | Updated address details with status 200 OK | Done |
| | | | | User tries to update another user's address | "Address not found" with status 404 | Done |
| | | | | Address not found | "Address not found" with status 404 | Done |

### 16. Delete Address

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 16 | **Delete Address** | | | | | |
| | addressId | message | Yes | User deletes own address | "Address deleted successfully" with status 200 OK | Done |
| | | | | User tries to delete another user's address | "Address not found" with status 404 | Done |
| | | | | Deleted address was default | Another address automatically becomes default | Done |

### 17. Set Default Address

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 17 | **Set Default Address** | | | | | |
| | addressId | updated address details | Yes | User sets own address as default | Address with isDefault=true, status 200 OK | Done |
| | | | | Previous default address | Previous default automatically unset | Done |
| | | | | User tries to set another user's address | "Address not found" with status 404 | Done |

### 18. Email Verification

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 18 | **Send Email Verification OTP** | | | | | |
| | N/A | message | Yes (CUSTOMER only) | Customer requests email verification OTP | "Verification OTP sent to your email" with status 200 OK | Done |
| | | | | Email already verified | "Email already verified" | Done |
| | | | | Non-customer role (ADMIN/DRIVER) | 403 Forbidden | Done |
| 19 | **Verify Email with OTP** | | | | | |
| | otp | message | Yes (CUSTOMER only) | Valid OTP provided | "Email verified successfully" with status 200 OK | Done |
| | | | | Invalid or expired OTP | "Invalid or expired OTP" | Done |
| | | | | Email already verified | "Email already verified" | Done |
| | | | | Non-customer role (ADMIN/DRIVER) | 403 Forbidden | Done |

### 19. Generate Internal JWT Token (Admin Only)

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 20 | **Generate Internal JWT Token** | | | | | |
| | serviceName | serviceName<br>internalToken<br>tokenType<br>expiresIn<br>usage<br>allowedEndpoints | Yes | Only ADMIN can generate internal token | Internal JWT token details with status 200 OK | Done |
| | | | | Not admin | Access Denied | Done |
| | | | | serviceName blank | "Service name is required" | Done |

### 20. Get User Activity History (Admin Only)

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 21 | **Get User Activity History** | | | | | |
| | userId<br>page<br>size | Page&lt;UserActivityLog&gt; | Yes | Only ADMIN can view activity history | Paginated activity logs with status 200 OK | Done |
| | | | | Not admin | Access Denied | Done |

### 21. Get System Statistics (Admin Only)

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 22 | **Get System Statistics** | | | | | |
| | N/A | totalLogins<br>totalRegistrations<br>loginsToday<br>registrationsToday<br>loginsThisWeek<br>registrationsThisWeek | Yes | Only ADMIN can view statistics | System statistics with status 200 OK | Done |
| | | | | Not admin | Access Denied | Done |

### 22. Internal API - Get User Info

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 23 | **Get User Info (Internal)** | | | | | |
| | userId | id<br>name<br>email<br>userCode<br>roles<br>active<br>deleted<br>emailVerified<br>mobileVerified | Yes (Internal JWT) | Valid internal JWT token | User info with status 200 OK | Done |
| | | | | No internal JWT token | "Missing or invalid internal token" with status 401 | Done |
| | | | | User not found | "User not found" with status 404 | Done |

### 23. Internal API - Check Order Eligibility

| S.No. | Request DTO | Response DTO | Token | Case Input | Case Output | Status |
|-------|------------|--------------|-------|------------|-------------|--------|
| 24 | **Check Order Eligibility (Internal)** | | | | | |
| | userId | userId<br>canPlaceOrder<br>emailVerified<br>active<br>deleted<br>hasAddress<br>addressCount<br>reason | Yes (Internal JWT) | User is eligible (email verified, active, not deleted, has address) | canPlaceOrder=true with status 200 OK | Done |
| | | | | Email not verified | canPlaceOrder=false, reason="Email not verified" | Done |
| | | | | Account deactivated | canPlaceOrder=false, reason="Account is deactivated" | Done |
| | | | | Account deleted | canPlaceOrder=false, reason="Account is deleted" | Done |
| | | | | No delivery address | canPlaceOrder=false, reason="No delivery address added" | Done |

---

## CONFIGURATION REQUIREMENTS

### Gmail SMTP Configuration
For email functionality (OTP, verification), configure Gmail credentials in `application.properties`:

```
spring.mail.username=your-email@gmail.com
spring.mail.password=your-gmail-app-password
```

**How to generate Gmail App Password:**
1. Go to Google Account Settings
2. Security > 2-Step Verification (enable if not enabled)
3. Security > App Passwords
4. Generate password for "Mail"
5. Use the 16-character password in application.properties

### Internal JWT Configuration
For microservice communication, configure internal JWT secret:

```
internal.jwt.secret=your-secure-secret-key
internal.jwt.expiration=3600000
```

---

## PENDING FEATURES

| S.No. | Feature | Status | Notes |
|-------|---------|--------|-------|
| 1 | Mobile OTP Verification | Pending | Infrastructure ready, awaiting SMS service provider integration |

---

**Document Version:** 2.0  
**Last Updated:** January 28, 2026  
**Total Endpoints:** 40+  
**Status:** Production Ready
