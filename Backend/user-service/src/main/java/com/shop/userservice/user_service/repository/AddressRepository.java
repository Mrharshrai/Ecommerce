package com.shop.userservice.user_service.repository;

import com.shop.userservice.user_service.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUserId(Long userId);
    
    Optional<Address> findByIdAndUserId(Long id, Long userId);

    // Better for performance than loading a full list
    boolean existsByUserId(Long userId);

    long countByUserId(Long userId);

    // Essential for later when you add "Set as Default" functionality
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}
