package com.paymentapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paymentapp.dto.ChangePasswordDTO;
import com.paymentapp.dto.UserLoginDTO;
import com.paymentapp.dto.UserResponseDTO;
import com.paymentapp.entity.User;
import com.paymentapp.security.CustomUserDetails;
import com.paymentapp.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody UserLoginDTO dto,
            @RequestParam String roleName,
            @AuthenticationPrincipal CustomUserDetails userDetails) {  

        User performingUser = userDetails.getUser();
        UserResponseDTO responseDTO = userService.registerUser(dto, roleName, performingUser);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) { 

        User performingUser = userDetails.getUser();
        userService.deleteUser(userId, performingUser);
        return ResponseEntity.ok("User deleted (soft delete)");
    }

    @PutMapping("/{userId}/change-password")
    public ResponseEntity<String> changePassword(
            @PathVariable Long userId,
            @RequestBody ChangePasswordDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {  

        User performingUser = userDetails.getUser();
        userService.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword(), performingUser);
        return ResponseEntity.ok("Password changed successfully");
    }

}
