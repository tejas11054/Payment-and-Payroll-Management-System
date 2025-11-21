package com.paymentapp.serviceImpl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.paymentapp.dto.UserLoginDTO;
import com.paymentapp.dto.UserResponseDTO;
import com.paymentapp.entity.Role;
import com.paymentapp.entity.User;
import com.paymentapp.repository.RoleRepository;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.service.AuditLogService;
import com.paymentapp.service.NotificationService;
import com.paymentapp.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    public UserResponseDTO registerUser(UserLoginDTO dto, String roleName, User performingUser) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        Role role = roleRepository.findByRoleName(roleName).orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.getRoles().add(role);
        user.setStatus("ACTIVE");

        userRepository.save(user);

        notificationService.sendEmail(
                dto.getEmail(),
                "Welcome to PaymentApp!",
                "Dear User,\n\n" +
                        "Thank you for registering with PaymentApp. Your account has been created successfully with the role of '"
                        + roleName + "'.\n\n" +
                        "You can now log in and start using our services.\n\n" +
                        "Best regards,\n" +
                        "PaymentApp Team"
        );

        auditLogService.log(
                "CREATE_USER",
                "USER",
                user.getUserId(),
                performingUser.getUserId(),
                performingUser.getEmail(),
                roleName
        );

        return new UserResponseDTO("User registered successfully");
    }

    @Override
    public void deleteUser(Long userId, User performingUser) {
        User user = userRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeleted(true);
        user.setStatus("INACTIVE");
        userRepository.save(user);

        notificationService.sendEmail(
                user.getEmail(),
                "Account Deletion Confirmation",
                "Dear User,\n\n" +
                        "We would like to inform you that your account with PaymentApp has been successfully deleted as per your request.\n\n" +
                        "If you did not initiate this action or believe this was a mistake, please contact our support team immediately.\n\n" +
                        "Thank you for using PaymentApp.\n\n" +
                        "Best regards,\n" +
                        "PaymentApp Support Team"
        );

        auditLogService.log(
                "DELETE_USER",
                "USER",
                user.getUserId(),
                performingUser.getUserId(),
                performingUser.getEmail(),
                user.getRoles().stream().findFirst().map(role -> role.getRoleName()).orElse("UNKNOWN")
        );
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword, User performingUser) {
        User user = userRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found or inactive"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        notificationService.sendEmail(
                user.getEmail(),
                "Password Change Confirmation",
                """
                Dear User,

                Your PaymentApp account password has been changed successfully.

                If you did not authorize this change, please contact our support team immediately.

                Best regards,
                PaymentApp Security Team
                """
        );

        auditLogService.log(
                "CHANGE_PASSWORD",
                "USER",
                user.getUserId(),
                performingUser.getUserId(),
                performingUser.getEmail(),
                user.getRoles().stream().findFirst().map(role -> role.getRoleName()).orElse("UNKNOWN")
        );
    }

}
