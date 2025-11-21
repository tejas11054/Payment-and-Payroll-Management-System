package com.paymentapp.repository;

import com.paymentapp.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByEmailAndOtpAndUsedFalse(String email, String otp);
    
    void deleteByEmail(String email);
    
    Optional<PasswordResetToken> findTopByEmailOrderByCreatedAtDesc(String email);
}
