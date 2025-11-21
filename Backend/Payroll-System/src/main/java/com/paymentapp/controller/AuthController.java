package com.paymentapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentapp.dto.ForgotPasswordRequestDTO;
import com.paymentapp.dto.ResetPasswordRequestDTO;
import com.paymentapp.dto.UserLoginDTO;
import com.paymentapp.dto.UserResponseDTO;
import com.paymentapp.dto.VerifyOtpRequestDTO;
import com.paymentapp.security.JwtTokenProvider;
import com.paymentapp.serviceImpl.PasswordResetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
 

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody UserLoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        String token = jwtTokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new UserResponseDTO(token));
    }
    
    private final PasswordResetService passwordResetService;
   
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        String message = passwordResetService.initiatePasswordReset(request);
        return ResponseEntity.ok(message);
    }
    
  
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOTP(@RequestBody VerifyOtpRequestDTO request) {
        String message = passwordResetService.verifyOTP(request);
        return ResponseEntity.ok(message);
    }
    
    
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        String message = passwordResetService.resetPassword(request);
        return ResponseEntity.ok(message);
    }


}

