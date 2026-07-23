package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.dto.requestDTO.CustomerRegistrationDTO;
import com.shop.userservice.user_service.dto.requestDTO.DriverRegistrationDTO;
import com.shop.userservice.user_service.dto.responseDTO.DriverRegistrationResponseDTO;
import com.shop.userservice.user_service.dto.responseDTO.UserOwnProfileResponseDTO;
import com.shop.userservice.user_service.dto.responseDTO.UserRegistrationResponseDTO;
import com.shop.userservice.user_service.dto.updateDTO.CustomerUpdateDTO;
import com.shop.userservice.user_service.dto.updateDTO.DriverDetailsUpdateDTO;
import com.shop.userservice.user_service.dto.updateDTO.EmployeeUpdateDTO;
import com.shop.userservice.user_service.entity.*;
import com.shop.userservice.user_service.exception.InvalidCredentialsException;
import com.shop.userservice.user_service.exception.OtpVerificationException;
import com.shop.userservice.user_service.exception.ResourceNotFoundException;
import com.shop.userservice.user_service.exception.UserAlreadyExistsException;
import com.shop.userservice.user_service.mapper.UserMapper;
import com.shop.userservice.user_service.repository.DriverProfileRepository;
import com.shop.userservice.user_service.repository.OTPRepository;
import com.shop.userservice.user_service.repository.UserRepository;
import com.shop.userservice.user_service.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Validated
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${jwt.refresh-expiration-ms}")
    private int refreshTokenExpiryMs;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DriverProfileRepository driverProfileRepository;
    private final UserMapper userMapper;
    private final UserActivityService activityService;
    private final JwtUtil jwtUtil;
    private final OTPRepository otpRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                          DriverProfileRepository driverProfileRepository, UserMapper userMapper,
                          UserActivityService activityService, JwtUtil jwtUtil, OTPRepository otpRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.driverProfileRepository = driverProfileRepository;
        this.userMapper = userMapper;
        this.activityService = activityService;
        this.jwtUtil = jwtUtil;
        this.otpRepository = otpRepository;
    }


    @Override
    public UserRegistrationResponseDTO createCustomerAndGetDTO(CustomerRegistrationDTO dto) {
        User user = createCustomer(dto);
        return userMapper.toDTO(user);
    }

    public User createCustomer(CustomerRegistrationDTO dto) {
        log.info("Registering new customer: {}", dto.getEmail());

        // --- PRE-REGISTRATION VERIFICATION GATE ---
        // Email OTP must be pre-verified before registration is allowed
        otpRepository.findByIdentifierAndPurposeAndVerifiedTrue(
                dto.getEmail(), OTP.OTPPurpose.PRE_REG_EMAIL)
                .orElseThrow(() -> new OtpVerificationException(
                        "Email not pre-verified. Please complete email OTP verification first."));

        // Mobile OTP must be pre-verified if mobile is provided
        if (dto.getMobile() != null && !dto.getMobile().isEmpty()) {
            otpRepository.findByIdentifierAndPurposeAndVerifiedTrue(
                    dto.getMobile(), OTP.OTPPurpose.PRE_REG_MOBILE)
                    .orElseThrow(() -> new OtpVerificationException(
                            "Mobile not pre-verified. Please complete mobile OTP verification first."));
        }

        // Check email uniqueness
        Optional<User> existingUserOpt = userRepository.findByEmail(dto.getEmail());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.getRoles().contains(Role.CUSTOMER)) {
                throw new UserAlreadyExistsException("Customer already exists with email: " + dto.getEmail());
            }
        }

        // Check mobile uniqueness (if provided)
        if (dto.getMobile() != null && !dto.getMobile().isEmpty()) {
            if (userRepository.existsByMobile(dto.getMobile())) {
                throw new UserAlreadyExistsException("Mobile number already registered: " + dto.getMobile());
            }
        }

        // New customer
        User user = new User();
        user.setName(dto.getName().trim().toUpperCase());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // If user doesn't exist → register normally as CUSTOMER
        Set<Role> roles = new HashSet<>();
        roles.add(Role.CUSTOMER);
        user.setRoles(roles);
        // Mark email/mobile as verified since we enforced OTP pre-verification
        user.setEmailVerified(true);
        if (dto.getMobile() != null && !dto.getMobile().isEmpty()) {
            user.setMobileVerified(true);
        }

        User savedUser = userRepository.save(user);
        savedUser.setUserCode("CUST" + String.format("%04d", savedUser.getId()));
        User finalUser = userRepository.save(savedUser);

        // Cleanup: delete the verified pre-reg OTP records now that the account exists
        otpRepository.deleteByIdentifierAndPurpose(dto.getEmail(), OTP.OTPPurpose.PRE_REG_EMAIL);
        if (dto.getMobile() != null && !dto.getMobile().isEmpty()) {
            otpRepository.deleteByIdentifierAndPurpose(dto.getMobile(), OTP.OTPPurpose.PRE_REG_MOBILE);
        }

        log.info("Customer registered successfully with ID: {}", finalUser.getId());
        return finalUser;
    }

    @Override
    public User createDriver(DriverRegistrationDTO dto, HttpServletRequest httpRequest) {
        log.info("Registering new driver: {}", dto.getEmail());
        // check if driver already exist as active/inactive/deleted
        Optional<User> existingUserOpt = userRepository.findByEmail(dto.getEmail());

        if (existingUserOpt.isPresent()) {
            throw new UserAlreadyExistsException("Company employee already exists with email: " + dto.getEmail());
        }
        // Check for existing phone number and vehicle number
        validatePhoneAndVehicle(dto.getPhoneNumber(), dto.getVehicleNumber());

        // If user doesn't exist → register normally as DRIVER
        Set<Role> roles = new HashSet<>();
        roles.add(Role.DRIVER);
        User user = new User();
        user.setName(dto.getName().trim().toUpperCase());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(roles);

        // First save to get the generated ID
        User savedUser = userRepository.save(user);

        // Now set the user code using the generated ID
        savedUser.setUserCode("DRVR" + String.format("%04d", savedUser.getId()));
        savedUser = userRepository.save(savedUser); // second save

        // Save driver profile
        DriverProfile profile = new DriverProfile();
        profile.setUser(savedUser);
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setVehicleNumber(dto.getVehicleNumber().trim().toUpperCase());
        driverProfileRepository.save(profile);

        // Log Driver Registration: Updated to match your exact format with httpRequest context
        try {
            activityService.logActivity(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    Role.DRIVER.name(),
                    com.shop.userservice.user_service.entity.ActivityType.REGISTRATION,
                    "Driver registered: " + savedUser.getName() + savedUser.getEmail() + profile.getVehicleNumber() + profile.getPhoneNumber(),
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log driver registration activity: {}", e.getMessage());
        }
        
        log.info("Driver registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    private void validatePhoneAndVehicle(String phoneNumber, String vehicleNumber) {
        if (driverProfileRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserAlreadyExistsException("Driver already exists with phone number: " + phoneNumber);
        }
        if (driverProfileRepository.existsByVehicleNumber(vehicleNumber)) {
            throw new UserAlreadyExistsException("Driver already exists with vehicle number: " + vehicleNumber);
        }
    }

    @Override
    public Page<UserRegistrationResponseDTO> getAllUsers(Pageable pageable) {
        // will get active/inactive/deleted users
        Page<User> users = userRepository.findAll(pageable);
        
        return users.map(user -> {
            if (user.getRoles().contains(Role.DRIVER)) {
                DriverProfile profile = driverProfileRepository.findById(user.getId()).orElse(null);
                return userMapper.toDriverDTO(user, profile);
            }
            return userMapper.toCustomerDTO(user);
        });
    }

    @Override
    public UserRegistrationResponseDTO getUserById(Long id) {
        // will get active/inactive user only, not deleted
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // If the user is a DRIVER → return driver DTO
        if (user.getRoles().contains(Role.DRIVER)) {

            DriverProfile profile = driverProfileRepository.findById(user.getId()).orElse(null);

            return userMapper.toDriverDTO(user, profile);
        }
        // If CUSTOMER → return customer DTO
        return userMapper.toCustomerDTO(user);
    }

    @Override
    @Transactional
    public UserRegistrationResponseDTO updateCustomer(Long id, CustomerUpdateDTO dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // name update
        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName().trim().toUpperCase());
        }
        // email (if provided) - ensure uniqueness
        if (dto.getEmail() != null) {
            Optional<User> byEmail = userRepository.findByEmail(dto.getEmail());
            if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
                throw new UserAlreadyExistsException("Email already in use: " + dto.getEmail());
            }
            existing.setEmail(dto.getEmail());
            existing.setEmailVerified(true);
        }
        
        // mobile (if provided) - ensure uniqueness
        if (dto.getMobile() != null && !dto.getMobile().isBlank()) {
            Optional<User> byMobile = userRepository.findByMobile(dto.getMobile());
            if (byMobile.isPresent() && !byMobile.get().getId().equals(id)) {
                throw new UserAlreadyExistsException("Mobile number already in use: " + dto.getMobile());
            }
            existing.setMobile(dto.getMobile());
            existing.setMobileVerified(true);
        }
        User saved = userRepository.save(existing);
        return userMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public UserRegistrationResponseDTO updatePassword(Long id, EmployeeUpdateDTO dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Only password allowed
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
            log.info("Password updated for employee ID: {}", id);
        }
        User saved = userRepository.save(existing);
        return userMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public DriverRegistrationResponseDTO updateDriverDetails(Long id, DriverDetailsUpdateDTO dto, HttpServletRequest httpRequest) {

        // 1️⃣ Find user
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + id));

        // 2️⃣ Check user is DRIVER
        if (!user.getRoles().contains(Role.DRIVER)) {
            throw new IllegalArgumentException("This user is not a driver");
        }

        // 3️⃣ Load driver profile
        DriverProfile profile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        // Track fields changed for driver-specific logging description
        java.util.StringJoiner changedFields = new java.util.StringJoiner(", ");

        // 4️⃣ Update name
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName().trim().toUpperCase());
            changedFields.add("Name");
        }

        // 5️⃣ Update email (with duplicate check)
        if (dto.getEmail() != null) {
            Optional<User> emailUser = userRepository.findByEmail(dto.getEmail());
            if (emailUser.isPresent() && !emailUser.get().getId().equals(id)) {
                throw new UserAlreadyExistsException("Email already in use: " + dto.getEmail());
            }
            user.setEmail(dto.getEmail());
            changedFields.add("Email");
        }

        // 6️⃣ Update phone number (duplicate check)
        if (dto.getPhoneNumber() != null) {
            Optional<DriverProfile> phoneExists =
                    driverProfileRepository.findByPhoneNumber(dto.getPhoneNumber());

            if (phoneExists.isPresent() && !phoneExists.get().getId().equals(id)) {
                throw new IllegalArgumentException("Phone number already in use with other driver");
            }

            profile.setPhoneNumber(dto.getPhoneNumber());
            changedFields.add("PhoneNumber");
        }

        // 7️⃣ Update vehicle number (duplicate check)
        if (dto.getVehicleNumber() != null) {
            Optional<DriverProfile> vehicleExists =
                    driverProfileRepository.findByVehicleNumber(dto.getVehicleNumber().trim().toUpperCase());

            if (vehicleExists.isPresent() && !vehicleExists.get().getId().equals(id)) {
                throw new IllegalArgumentException("Vehicle number already in use with other driver");
            }
            profile.setVehicleNumber(dto.getVehicleNumber().trim().toUpperCase());
            changedFields.add("Vehicle Number");
        }

        // Save changes
        userRepository.save(user);
        driverProfileRepository.save(profile);

        // Activity Logging for DRIVER Record
        try {
            String driverDescription = String.format("Driver profile details were updated by administrator. Fields modified: [%s]",
                    changedFields.length() > 0 ? changedFields.toString() : "None");

            activityService.logActivity(
                    user.getId(),
                    user.getEmail(),
                    Role.DRIVER.name(),
                    ActivityType.UPDATE_DRIVER,
                    driverDescription,
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log driver-side profile update activity: {}", e.getMessage());
        }

        // 8️⃣ Return DTO
        return userMapper.toDriverDTO(user,profile);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        log.info("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }

    @Override
    public void ensureTargetIsCustomer(Long id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (!target.getRoles().contains(Role.CUSTOMER)) {
            throw new AccessDeniedException("Admins can delete only CUSTOMER accounts.");
        }
    }

    @Override
    public void softDeleteUser(Long id, HttpServletRequest httpRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Only drivers can be soft deleted
        if (!user.getRoles().contains(Role.DRIVER)) {
            throw new IllegalStateException("Only drivers can be soft deleted, not applicable for others.");
        }

        // Check if already soft deleted
        if (user.isDeleted()) {
            throw new IllegalStateException("User is already soft deleted.");
        }

        // Extract primitive details into local variables BEFORE saving modifications
        Long driverId = user.getId();
        String driverEmail = user.getEmail();

        // Perform soft delete
        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);

        // Activity Logging for DRIVER Record
        try {
            activityService.logActivity(
                    driverId,
                    driverEmail,
                    Role.DRIVER.name(),
                    ActivityType.SOFT_DELETE_USER,
                    "Driver account was soft-deleted and deactivated by an administrator",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log driver-side soft delete activity: {}", e.getMessage());
        }
    }

    @Override
    public void restoreUser(Long id, HttpServletRequest httpRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Only drivers can be restored
        if (!user.getRoles().contains(Role.DRIVER)) {
            throw new IllegalStateException("Only drivers can be restored, not applicable for others.");
        }

        // Check if already restored
        if (!user.isDeleted()) {
            throw new IllegalStateException("User is already restored.");
        }

        // Perform soft delete
        user.setDeleted(false);
        user.setActive(false);
        userRepository.save(user);

        // Activity Logging for DRIVER Record
        try {
            activityService.logActivity(
                    user.getId(),
                    user.getEmail(),
                    Role.DRIVER.name(),
                    ActivityType.RESTORE_USER,
                    "Driver account status was changed from soft-deleted back to restored by an administrator",
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log driver-side restoration activity: {}", e.getMessage());
        }
    }


    @Override
    public User findByEmail(String email) {
        // will check for active/inactive/deleted
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        // Prevent access if account is deleted
//        validateUserNotDeleted(user);
        return user;
    }


    @Override
    public User findActiveUserByEmail(String email) {
        User user= userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new InvalidCredentialsException("No account found with this email. Please register."));
        if(!user.isActive()){
            throw new LockedException("Account is deactivated. Please contact support.");
        }
        return user;
    }

    @Override
    public User findNonDeletedUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new InvalidCredentialsException("No account found with this email. Please register."));
    }

    @Override
    @Transactional
    public void handleInactiveUser(User user) {
        // Check if they have ANY "privileged" roles that should be blocked when inactive
        boolean isStaff = user.getRoles().stream()
                .anyMatch(r -> r == Role.ADMIN || r == Role.DRIVER);

        if (isStaff) {
            throw new LockedException("Account is deactivated. Please contact support.");
        }
        // If they aren't staff, and they are here, they must be a customer

        user.setActive(true);
        user.setDeleted(false);
        userRepository.save(user);
        log.info("Inactive customer {} has been reactivated upon login.", user.getEmail());
    }


    @Override
    public UserRegistrationResponseDTO getUserByEmail(String email) {
        User user = findByEmail(email);
        return userMapper.toDTO(user);
    }

    @Override
    public UserOwnProfileResponseDTO myProfile(String email) {
        User user = findByEmail(email);

        // 1. Initialize the builder with common user fields
        UserOwnProfileResponseDTO.UserOwnProfileResponseDTOBuilder builder = UserOwnProfileResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .mobileVerified(user.isMobileVerified())
                .emailVerified(user.isEmailVerified());

        // 2. If the user is a DRIVER, fetch their profile and add vehicle/mobile details
        if (user.getRoles().contains(Role.DRIVER)) {
            DriverProfile profile = driverProfileRepository.findById(user.getId()).orElse(null);

            if (profile != null) {
                // Set the driver's specific mobile number if it exists
                if (profile.getPhoneNumber() != null) {
                    builder.mobile(profile.getPhoneNumber());
                }
                // Set the vehicle number safely
                builder.vehicleNumber(profile.getVehicleNumber());
            }
        }

        // 3. Build and return the final DTO
        return builder.build();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

//    @Override
//    public User saveUser(User user) {
//        return userRepository.save(user);
//    }
    
    // ==================== ADMIN CUSTOMER MANAGEMENT ====================
    
    @Override
    public Page<UserRegistrationResponseDTO> getActiveCustomers(Pageable pageable) {
        Page<User> activeCustomers = userRepository.findActiveCustomers(pageable);
        return activeCustomers.map(userMapper::toCustomerDTO);
    }
    
    @Override
    public UserRegistrationResponseDTO getCustomerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        
        // Ensure user is a customer
        if (!user.getRoles().contains(Role.CUSTOMER)) {
            throw new IllegalArgumentException("User with ID " + id + " is not a customer");
        }
        
        return userMapper.toCustomerDTO(user);
    }
    
    @Override
    @Transactional
    public void activateUser(Long id, HttpServletRequest httpRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if(user.isDeleted()){
            throw new ResourceNotFoundException("User is deleted ,Restore them first.");
        }

        // Only drivers/customers can be activated not ADMINS
        if (user.getRoles().contains(Role.ADMIN)) {
            throw new IllegalStateException("Can't activate an Admin's account.");
        }
        
        // Ensure user is a customer
//        if (!user.getRoles().contains(Role.CUSTOMER)) {
//            throw new IllegalArgumentException("Only customer accounts can be activated via this endpoint");
//        }

        if(user.isActive()){
            throw new ResourceNotFoundException("User is already active.");
        }

        user.setActive(true);
        userRepository.save(user);
        
        log.info("User activated: ID {}", id);
        // Activity Logging for TARGET USER Record
        try {
            // Safely grab the exact primary role of the user being activated
            String targetUserRole = user.getRoles().iterator().next().name();
            activityService.logActivity(
                    user.getId(),
                    user.getEmail(),
                    targetUserRole,
                    ActivityType.ACTIVATE_USER,
                    String.format("%s account was activated by an administrator", targetUserRole),
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log user-side activation activity: {}", e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void deactivateUser(Long id, HttpServletRequest httpRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if(user.isDeleted()){
            throw new ResourceNotFoundException("User is deleted ,Restore them first.");
        }

        // Only drivers/customers can be deactiavted not ADMINS
        if (user.getRoles().contains(Role.ADMIN)) {
            throw new IllegalStateException("Can't deactivate an Admin's account.");
        }
        
        // Ensure user is a customer
//        if (!user.getRoles().contains(Role.CUSTOMER)) {
//            throw new IllegalArgumentException("Only customer accounts can be deactivated via this endpoint");
//        }

        if(!user.isActive()){
            throw new ResourceNotFoundException("User is already inactive.");
        }
        
        user.setActive(false);
        userRepository.save(user);
        
        log.info("User deactivated: ID {}", id);

        // Activity Logging for TARGET USER Record
        try {
            // Safely extract the primary role of the target user being deactivated
            String targetUserRole = user.getRoles().iterator().next().name();

            activityService.logActivity(
                    user.getId(),
                    user.getEmail(),
                    targetUserRole,
                    ActivityType.DEACTIVATE_USER,
                    String.format("%s account was deactivated by an administrator", targetUserRole),
                    httpRequest
            );
        } catch (Exception e) {
            log.warn("Could not log user-side deactivation activity: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deactivateOwnAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        user.setActive(false);
        userRepository.save(user);
        log.info("Customer deactivated own account: {}", email);
    }

    // --- Issue 2 Fix: Per-user mutex to prevent concurrent refresh race conditions ---
    private final ConcurrentHashMap<String, ReentrantLock> refreshLocks = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public User validateAndRotateRefreshToken(String email, String refreshToken) {
        // Issue 2 Fix: Acquire a per-user lock so that concurrent refresh requests
        // from the same user are serialized. The first one wins and rotates the token;
        // subsequent ones (with the now-old token) are rejected gracefully instead of
        // falsely triggering the replay-attack revocation.
        ReentrantLock lock = refreshLocks.computeIfAbsent(email, k -> new ReentrantLock());
        lock.lock();
        try {
            // 1. Check if user exists and is not deleted
            User user = userRepository.findByEmailAndDeletedFalse(email)
                    .orElseThrow(() -> new InvalidCredentialsException("User no longer exists"));

            // Issue 1 Fix: Reject refresh for deactivated ADMIN or DRIVER accounts.
            // CUSTOMER accounts are deliberately excluded because login auto-reactivates them;
            // applying this check here would block their silent token refresh as well.
            boolean isStaff = user.getRoles().stream()
                    .anyMatch(r -> r == Role.ADMIN || r == Role.DRIVER);
            if (isStaff && !user.isActive()) {
                // Revoke the token so they can't keep retrying
                user.setRefreshToken(null);
                user.setRefreshTokenExpiry(null);
                userRepository.save(user);
                throw new InvalidCredentialsException("Account is deactivated. Please contact support.");
            }

            // REPLAY ATTACK DETECTION
            // If the token provided doesn't match the one in DB, someone might be using an old stolen token
            if (!refreshToken.equals(user.getRefreshToken())) {
                // SECURITY BREACH: Revoke everything!
                user.setRefreshToken(null);
                userRepository.save(user);
                throw new InvalidCredentialsException("Security alert: Token reuse detected. Please log in again.");
            }

            if (user.getRefreshTokenExpiry().isBefore(Instant.now())) {
                throw new InvalidCredentialsException("Session expired");
            }

            // ROTATION: Generate a brand-new token for the next refresh
            String newRefreshToken = jwtUtil.generateRefreshToken(email);
            user.setRefreshToken(newRefreshToken);
            user.setRefreshTokenExpiry(Instant.now().plusMillis(refreshTokenExpiryMs));
                    //, java.time.temporal.ChronoUnit.MILLIS));
            return userRepository.save(user);
        } finally {
            lock.unlock();
            // Clean up the lock entry if it's no longer held to prevent unbounded map growth
            refreshLocks.remove(email, lock);
        }
    }

    @Transactional
    public void clearRefreshToken(String email) {
        userRepository.findByEmailAndDeletedFalse(email).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            log.info("Refresh token cleared for user: {}", email);
        });
    }

    @Override
    public User findActiveUserByMobile(String mobile) {
        log.debug("Searching for active user with mobile: {}", mobile);
        
        User user = (User) userRepository.findByMobileAndDeletedFalse(mobile)
                .orElseThrow(() -> new InvalidCredentialsException("No account found with this mobile number. Please register."));
        
        if (!user.isActive()) {
            log.warn("User account is deactivated for mobile: {}", mobile);
            throw new LockedException("Account is deactivated. Please contact support.");
        }
        
        log.debug("Active user found for mobile: {}", mobile);
        return user;
    }

    @Override
    @Transactional
    public void saveRefreshToken(User user, String refreshToken, Instant expiry) {
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(expiry);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));

        // 2. SECURITY UPGRADE: Invalidate all active sessions
        // This forces any existing hackers or old devices to re-login
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);

        userRepository.save(user);

        // 3. Cleanup: Delete the used OTP so it's gone from the DB forever
        otpRepository.deleteByIdentifierAndPurpose(email, OTP.OTPPurpose.FORGOT_PASSWORD);
        
        activityService.logActivity(
            user.getId(),
            user.getEmail(),
            user.getRoles().iterator().next().name(),
            com.shop.userservice.user_service.entity.ActivityType.PASSWORD_CHANGE,
            "Password reset via OTP"
        );
        log.info("Password reset for user: {}", email);
    }
}
