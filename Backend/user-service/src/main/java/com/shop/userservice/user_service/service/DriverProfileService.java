package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.entity.DriverProfile;

import java.util.Optional;

public interface DriverProfileService {
    Optional<DriverProfile> getDriverProfileByUserId(Long userId);
    DriverProfile saveDriverProfile(DriverProfile profile);
}
