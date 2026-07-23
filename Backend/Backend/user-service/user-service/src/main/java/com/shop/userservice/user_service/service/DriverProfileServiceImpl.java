package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.entity.DriverProfile;
import com.shop.userservice.user_service.repository.DriverProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DriverProfileServiceImpl implements DriverProfileService {

    private final DriverProfileRepository repository;

    public DriverProfileServiceImpl(DriverProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<DriverProfile> getDriverProfileByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public DriverProfile saveDriverProfile(DriverProfile profile) {
        return repository.save(profile);
    }
}


