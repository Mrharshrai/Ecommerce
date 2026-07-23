package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.dto.requestDTO.AddressDTO;
import com.shop.userservice.user_service.dto.responseDTO.AddressResponseDTO;
import com.shop.userservice.user_service.dto.updateDTO.UpdateAddressDTO;

import java.util.List;

public interface AddressService {
    
    AddressResponseDTO addAddress(Long userId, AddressDTO addressDTO);
    
    List<AddressResponseDTO> getAllAddresses(Long userId);
    
    AddressResponseDTO updateAddress(Long userId, Long addressId, UpdateAddressDTO addressDTO);
    
    void deleteAddress(Long userId, Long addressId);
    
    AddressResponseDTO setDefaultAddress(Long userId, Long addressId);
}
