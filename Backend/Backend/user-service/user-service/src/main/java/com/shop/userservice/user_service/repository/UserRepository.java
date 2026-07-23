package com.shop.userservice.user_service.repository;

import com.shop.userservice.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndDeletedFalse(String email);
    Optional<User> findByMobile(String mobile);
    boolean existsByMobile(String mobile);
    
    // Find active customers (not deleted, active=true, has CUSTOMER role)
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = com.shop.userservice.user_service.entity.Role.CUSTOMER AND u.deleted = false AND u.active = true")
    Page<User> findActiveCustomers(Pageable pageable);

    Optional<User> findByMobileAndDeletedFalse(String mobile);
}
