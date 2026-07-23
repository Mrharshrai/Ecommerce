package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.dto.requestDTO.AddressDTO;
import com.shop.userservice.user_service.dto.responseDTO.AddressResponseDTO;
import com.shop.userservice.user_service.dto.updateDTO.UpdateAddressDTO;
import com.shop.userservice.user_service.entity.Address;
import com.shop.userservice.user_service.exception.BusinessException;
import com.shop.userservice.user_service.exception.ResourceNotFoundException;
import com.shop.userservice.user_service.repository.AddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressServiceImpl.class);

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional
    public AddressResponseDTO addAddress(Long userId, AddressDTO addressDTO) {
        // 0. Check the existing count
        long currentAddressCount = addressRepository.countByUserId(userId);

        if (currentAddressCount >= 5) {
            throw new BusinessException("Maximum limit of 5 addresses reached. Please delete an existing address first.");
        }

        // 1. Performance: Use exists check instead of loading all addresses
        boolean isFirstAddress = (currentAddressCount == 0);

        // 2. Map DTO to Entity using Builder
        Address address = Address.builder()
                .userId(userId)
                .addressLine1(addressDTO.getAddressLine1())
                .addressLine2(addressDTO.getAddressLine2())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .pincode(addressDTO.getPincode())
                .latitude(addressDTO.getLatitude())
                .longitude(addressDTO.getLongitude())
                .isDefault(isFirstAddress) // Logic: If none exist, this is default
                .build();

        // 3. Persist
        Address savedAddress = addressRepository.save(address);
        log.info("Address saved for userId: {}, ID: {}, Default: {}",
                userId, savedAddress.getId(), isFirstAddress);
        
        return toDTO(savedAddress);
    }

    @Override
    @Transactional
    public AddressResponseDTO updateAddress(Long userId, Long addressId, UpdateAddressDTO addressDTO) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        boolean isUpdated = false;

        if (addressDTO.getAddressLine1() != null &&
                !addressDTO.getAddressLine1().equals(address.getAddressLine1())) {
            address.setAddressLine1(addressDTO.getAddressLine1());
            isUpdated = true;
        }

        if (addressDTO.getAddressLine2() != null &&
                !addressDTO.getAddressLine2().equals(address.getAddressLine2())) {
            address.setAddressLine2(addressDTO.getAddressLine2());
            isUpdated = true;
        }

        if (addressDTO.getCity() != null &&
                !addressDTO.getCity().equals(address.getCity())) {
            address.setCity(addressDTO.getCity());
            isUpdated = true;
        }

        if (addressDTO.getState() != null &&
                !addressDTO.getState().equals(address.getState())) {
            address.setState(addressDTO.getState());
            isUpdated = true;
        }

        if (addressDTO.getPincode() != null &&
                !addressDTO.getPincode().equals(address.getPincode())) {
            address.setPincode(addressDTO.getPincode());
            isUpdated = true;
        }

        if (addressDTO.getLatitude() != null &&
                !addressDTO.getLatitude().equals(address.getLatitude())) {
            address.setLatitude(addressDTO.getLatitude());
            isUpdated = true;
        }

        if (addressDTO.getLongitude() != null &&
                !addressDTO.getLongitude().equals(address.getLongitude())) {
            address.setLongitude(addressDTO.getLongitude());
            isUpdated = true;
        }

        if (!isUpdated) {
            throw new IllegalArgumentException("No changes detected. Address not updated.");
        }

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated: {} for user: {}", addressId, userId);

        return toDTO(updatedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAllAddresses(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);

        if (addresses == null || addresses.isEmpty()) {
            throw new ResourceNotFoundException("No addresses found for user ID: " + userId);
        }
        return addresses.stream()
                // Sort: default (true) comes before non-default (false)
                .sorted((a1, a2) -> Boolean.compare(a2.isDefault(), a1.isDefault()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        boolean wasDefault = address.isDefault();

        // 1. Delete and Flush immediately to sync the persistence context
        addressRepository.delete(address);
        addressRepository.flush();

        log.info("Address deleted: {} for user: {}", addressId, userId);

        // 2. If the deleted one was default, we need a new king
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                // Pick the first available one (e.g., the oldest/newest)
                Address newDefault = remainingAddresses.get(0);
                newDefault.setDefault(true);
                addressRepository.save(newDefault);
                log.info("New default address set: {} for user: {}", newDefault.getId(), userId);
            }
        }
    }

    @Override
    @Transactional
    public AddressResponseDTO setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        // Already default
        if (Boolean.TRUE.equals(address.isDefault())) {
            return toDTO(address);
        }

        // Remove old default
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(currentDefault -> {
                    currentDefault.setDefault(false);
                    addressRepository.save(currentDefault);
                });

        // Set new default
        address.setDefault(true);

        Address updatedAddress = addressRepository.save(address);
        log.info("Default address set: {} for user: {}", addressId, userId);

        return toDTO(updatedAddress);
    }
    
    private AddressResponseDTO toDTO(Address address) {
        return AddressResponseDTO.builder()
                .id(address.getId())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
