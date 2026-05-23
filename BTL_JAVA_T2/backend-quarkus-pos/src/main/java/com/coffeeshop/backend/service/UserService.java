package com.coffeeshop.backend.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.coffeeshop.backend.dto.user.UserDTO;
import com.coffeeshop.backend.dto.user.UserResponse;
import com.coffeeshop.backend.dto.user.UserRequest;
import com.coffeeshop.backend.entity.User;
import com.coffeeshop.backend.enums.UserRole;

@Service
public interface UserService {
    Optional<User> findByEmail(String email);

    User saveUser(User user);

    Page<User> getAllUsers(String search, Pageable pageable);

    User updateUserRole(Long userId, UserRole role);

    User getProfile(String email);

    User updateProfile(String email, UserDTO userDTO);

    User updateUserStore(String name, Long storeId);

    java.util.List<UserResponse> getAllStaffUsers();
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(Long id, UserRequest request);
    UserResponse toggleUserStatus(Long id);
    UserResponse resetPassword(Long id);

}
