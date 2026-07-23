package com.shop.userservice.user_service.repository;

import com.shop.userservice.user_service.entity.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTP, Long> {
    
    Optional<OTP> findByIdentifierAndPurpose(
            String identifier, OTP.OTPPurpose purpose);
    
    Optional<OTP> findTopByIdentifierAndPurposeOrderByCreatedAtDesc(
            String identifier, OTP.OTPPurpose purpose);
    
    void deleteByExpiryTimeBefore(Instant now);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM OTP o WHERE o.identifier = :identifier AND o.purpose = :purpose")
    void deleteByIdentifierAndPurpose(
        @Param("identifier") String identifier,
        @Param("purpose") OTP.OTPPurpose purpose
    );

    // Checks if an OTP was created for this user/purpose within a specific timeframe
    boolean existsByIdentifierAndPurposeAndCreatedAtAfter(
            String identifier,
            OTP.OTPPurpose purpose,
            Instant createdAt
    );

    // Used by registration to confirm a pre-reg OTP was verified before account creation
    Optional<OTP> findByIdentifierAndPurposeAndVerifiedTrue(
            String identifier, OTP.OTPPurpose purpose);
}
