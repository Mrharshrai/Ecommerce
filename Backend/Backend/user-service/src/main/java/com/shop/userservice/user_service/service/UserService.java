package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.dto.requestDTO.CustomerRegistrationDTO;
import com.shop.userservice.user_service.dto.requestDTO.DriverRegistrationDTO;
import com.shop.userservice.user_service.dto.responseDTO.DriverRegistrationResponseDTO;
import com.shop.userservice.user_service.dto.responseDTO.UserOwnProfileResponseDTO;
import com.shop.userservice.user_service.dto.responseDTO.UserRegistrationResponseDTO;
import com.shop.userservice.user_service.dto.updateDTO.CustomerUpdateDTO;
import com.shop.userservice.user_service.dto.updateDTO.DriverDetailsUpdateDTO;
import com.shop.userservice.user_service.dto.updateDTO.EmployeeUpdateDTO;
import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.exception.ResourceNotFoundException;
import com.shop.userservice.user_service.exception.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface  UserService {
//    User createCustomer(CustomerRegistrationDTO user) throws UserAlreadyExistsException;
    
    UserRegistrationResponseDTO createCustomerAndGetDTO(CustomerRegistrationDTO dto) throws UserAlreadyExistsException;

//    User createAdmin(User user) throws UserAlreadyExistsException;

    User createDriver(DriverRegistrationDTO dto, HttpServletRequest httpRequest) throws UserAlreadyExistsException;

    Page<UserRegistrationResponseDTO> getAllUsers(Pageable pageable);

    UserRegistrationResponseDTO getUserById(Long id) throws ResourceNotFoundException;

    //    User updateUser(Long id, User updatedUser) throws ResourceNotFoundException;
    UserRegistrationResponseDTO updateCustomer(Long id, CustomerUpdateDTO dto);

    UserRegistrationResponseDTO updatePassword(Long id, EmployeeUpdateDTO dto);

    DriverRegistrationResponseDTO updateDriverDetails(Long id, DriverDetailsUpdateDTO dto, HttpServletRequest httpRequest);

    void deleteUser(Long id) throws ResourceNotFoundException;

    public void ensureTargetIsCustomer(Long id);

    void softDeleteUser(Long id,HttpServletRequest httpRequest);

    void restoreUser(Long id, HttpServletRequest httpRequest);

    User findByEmail(String email);
    
    UserRegistrationResponseDTO getUserByEmail(String email);

    UserOwnProfileResponseDTO myProfile(String email);
    
    User findById(Long id);
    
//    User saveUser(User user);
    
    // Admin customer management
    Page<UserRegistrationResponseDTO> getActiveCustomers(Pageable pageable);
    
    UserRegistrationResponseDTO getCustomerById(Long id);
    
    void activateUser(Long id, HttpServletRequest httpRequest);
    
    void deactivateUser(Long id, HttpServletRequest httpRequest);
    
    void handleInactiveUser(User user);
    
    void deactivateOwnAccount(String email);
    
    void saveRefreshToken(User user, String refreshToken, java.time.Instant expiry);
    
    void resetPassword(String email, String newPassword);

    User findActiveUserByEmail(String email);

    User validateAndRotateRefreshToken(String email, String refreshToken);

    void clearRefreshToken(String email);

    User findActiveUserByMobile(String mobile);

    User findNonDeletedUserByEmail(String email);
}
