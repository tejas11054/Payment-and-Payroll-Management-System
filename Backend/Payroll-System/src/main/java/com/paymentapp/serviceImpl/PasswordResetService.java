package com.paymentapp.serviceImpl;
import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentapp.dto.ForgotPasswordRequestDTO;
import com.paymentapp.dto.ResetPasswordRequestDTO;
import com.paymentapp.dto.VerifyOtpRequestDTO;
import com.paymentapp.entity.PasswordResetToken;
import com.paymentapp.entity.User;
import com.paymentapp.repository.PasswordResetTokenRepository;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    
    private static final int OTP_EXPIRY_MINUTES = 10;
    
    /**
     * Step 1: Generate and send OTP
     */
    @Transactional
    public String initiatePasswordReset(ForgotPasswordRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        
        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with this email does not exist"));
        
        // Delete any existing tokens for this email
        tokenRepository.deleteByEmail(email);
        
        // Generate 5-digit OTP
        String otp = generateOTP();
        
        // Save token
        PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        tokenRepository.save(token);
        
        // Send email with OTP
        notificationService.sendEmail(
            email,
            "Password Reset OTP",
            String.format(
                "Dear User,\n\n" +
                "Your password reset OTP is: %s\n\n" +
                "This OTP will expire in %d minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best regards,\nPayRoll Team",
                otp,
                OTP_EXPIRY_MINUTES
            )
        );
        
        return "OTP sent to your email successfully";
    }
    
    /**
     * Step 2: Verify OTP
     */
    public String verifyOTP(VerifyOtpRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();
        
        PasswordResetToken token = tokenRepository.findByEmailAndOtpAndUsedFalse(email, otp)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));
        
        // Check expiry
        if (LocalDateTime.now().isAfter(token.getExpiryTime())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }
        
        return "OTP verified successfully";
    }
    
    /**
     * Step 3: Reset Password
     */
    @Transactional
    public String resetPassword(ResetPasswordRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();
        
        // Verify OTP again
        PasswordResetToken token = tokenRepository.findByEmailAndOtpAndUsedFalse(email, otp)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));
        
        // Check expiry
        if (LocalDateTime.now().isAfter(token.getExpiryTime())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }
        
        // Get user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Mark token as used
        token.setUsed(true);
        tokenRepository.save(token);
        
        // Send confirmation email
        notificationService.sendEmail(
            email,
            "Password Reset Successful",
            "Your password has been reset successfully. You can now login with your new password."
        );
        
        return "Password reset successfully";
    }
    
    /**
     * Generate random 5-digit OTP
     */
    private String generateOTP() {
        Random random = new Random();
        int otp = 10000 + random.nextInt(90000); // 5-digit number
        return String.valueOf(otp);
    }
}