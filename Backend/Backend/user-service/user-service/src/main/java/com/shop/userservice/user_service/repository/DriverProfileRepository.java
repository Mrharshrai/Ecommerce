package com.shop.userservice.user_service.repository;

import com.shop.userservice.user_service.entity.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {
    Optional<DriverProfile> findByUserId(Long userId);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByVehicleNumber(String vehicleNumber);
    Optional<DriverProfile> findByPhoneNumber(String phoneNumber);
    Optional<DriverProfile> findByVehicleNumber(String vehicleNumber);
}

