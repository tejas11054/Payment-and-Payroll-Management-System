package com.paymentapp.service;

import com.paymentapp.dto.UserLoginDTO;
import com.paymentapp.dto.UserResponseDTO;
import com.paymentapp.entity.User;

public interface UserService {
	
	UserResponseDTO registerUser(UserLoginDTO dto, String roleName, User performingUser);

    void deleteUser(Long userId, User performingUser);

    void changePassword(Long userId, String oldPassword, String newPassword, User performingUser);
}
