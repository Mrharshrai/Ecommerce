# User Service - Delivery Package

## What to Share with Client

### Share the entire `user-service` folder containing:

1. **Source Code**
   - `/src/main/java` - All Java source files
   - `/src/main/resources` - Configuration files
   - `/pom.xml` - Maven dependencies

2. **Documentation** (in root folder)
   - `IMPLEMENTATION_SUMMARY.md` - What was implemented
   - `API_TESTING_GUIDE.md` - Complete testing instructions
   - `README.md` - This file

3. **Postman Collection** (you will share separately)
   - Your tested Postman collection with all endpoints

---

## Important Configuration Required

### Gmail SMTP Setup (Required for Email Features)

The client needs to configure Gmail credentials in `application.properties`:

```properties
spring.mail.username=their-email@gmail.com
spring.mail.password=their-gmail-app-password
```

**How to generate Gmail App Password:**
1. Go to Google Account Settings
2. Security > 2-Step Verification (enable if not enabled)
3. Security > App Passwords
4. Generate password for "Mail"
5. Use the 16-character password in application.properties

---

## Quick Start Guide for Client

### 1. Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL database
- Eureka Server running (port 8761)

### 2. Configuration
Update `src/main/resources/application.properties`:
- Database credentials
- Gmail SMTP credentials (for email features)
- Internal JWT secret (for microservice communication)

### 3. Run Application
```bash
cd user-service
mvn spring-boot:run
```

### 4. Access Swagger Documentation
```
http://localhost:8081/swagger-ui.html
```

### 5. Test APIs
Follow the `API_TESTING_GUIDE.md` for complete testing instructions.

---

## Features Implemented

### Authentication & Security
- User registration (Customer, Admin, Driver)
- Login with JWT token generation
- Logout with token blacklist
- Refresh token (24-hour expiry)
- Forgot password (email OTP)
- Reset password
- Email verification (OTP)
- Token expiration handling
- Role-based access control (ADMIN, CUSTOMER, DRIVER)

### User Profile Management
- View own profile (Customer/Admin)
- Update profile (name, email, mobile, password)
- Mobile number uniqueness validation
- Email uniqueness validation
- Prevent duplicate registrations

### Account Status & Control
- Admin view active customers (paginated)
- Admin view customer details
- Admin activate/deactivate customer accounts
- Admin delete customer accounts
- Customer self-deactivate
- Customer self-delete
- Prevent login for deactivated/deleted users
- Auto-reactivation for drivers on login

### Address Management
- Add multiple addresses
- View all addresses
- Update address
- Delete address
- Set default delivery address
- Automatic default handling

### Internal Microservice APIs
- Internal JWT for inter-service communication
- Generate internal JWT token (Admin only)
- Get user by ID (for microservices)
- Validate JWT token (for microservices)
- Check order eligibility (email verification required)

### Activity Logging & Analytics
- Track user registration
- Track user login (with IP address)
- Track profile updates
- Track email changes
- Track admin actions
- User activity history (paginated)
- Last login tracking
- Login frequency monitoring
- System-wide statistics
- Suspicious activity detection

**Total Endpoints:** 40+

---

## Database Changes

### New Tables
- `token_blacklist` - Invalidated tokens
- `otp` - OTP for verification and password reset
- `addresses` - User delivery addresses
- `user_activity_logs` - User activity tracking

### Modified Tables
- `users` - Added mobile, refresh token, verification fields, account status fields

**Note:** Tables will be auto-created by Hibernate on first run.

---

## Unit Tests

### Test Coverage
The User Service includes unit tests to verify core functionality:

**Test File:** `UserServiceApplicationTests.java`

**Test Cases:**
1. Context Loads - Verifies Spring Boot application starts successfully
2. User Entity Creation - Tests User entity property assignment
3. Role Enum Validation - Validates all three roles (CUSTOMER, ADMIN, DRIVER)
4. User Role Assignment - Tests multiple role assignment capability
5. User Default Values - Validates boolean flags (active, deleted, verified)

**Running Tests:**
```bash
mvn test
```

**Test Results:** All 5 tests pass successfully

---

## Pending Features

### Mobile Verification (Awaiting SMS Service Provider)
- Mobile OTP verification infrastructure is ready
- Requires SMS service provider integration
- Implementation can be completed once provider is selected

---

## Support

For any questions or issues:
1. Review `IMPLEMENTATION_SUMMARY.md` for technical details
2. Review `API_TESTING_GUIDE.md` for testing instructions
3. Check Postman collection for working examples

---

**Version:** 2.0  
**Date:** January 28, 2026  
**Status:** Production Ready
