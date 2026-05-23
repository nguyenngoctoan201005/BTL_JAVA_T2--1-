package com.coffeeshop.backend.service.implement;

import com.coffeeshop.backend.entity.Store;
import com.coffeeshop.backend.enums.UserRole;
import com.coffeeshop.backend.exception.ResourceNotFoundException;
import com.coffeeshop.backend.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.coffeeshop.backend.entity.User;
import com.coffeeshop.backend.repository.UserRepository;
import com.coffeeshop.backend.service.UserService;
import com.coffeeshop.backend.dto.user.UserResponse;
import com.coffeeshop.backend.dto.user.UserRequest;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Page<User> getAllUsers(String search, Pageable pageable) {
        return userRepository.findByEmailContainingIgnoreCaseOrFullnameContainingIgnoreCase(search, search, pageable);
    }

    @Override
    public User updateUserRole(Long userId, UserRole role) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String currentUsername = "admin@example.com";
        User currentUser = userRepository.findByEmail(currentUsername).orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (currentUser.getId().equals(userToUpdate.getId())) {
            throw new IllegalArgumentException("Admins cannot change their own role.");
        }

        userToUpdate.setRole(role);
        return userRepository.save(userToUpdate);
    }

    @Override
    public User getProfile(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public User updateProfile(String email, com.coffeeshop.backend.dto.user.UserDTO userDTO) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFullname(userDTO.getFullname());
        user.setPhone(userDTO.getPhone());
        return userRepository.save(user);
    }

    @Override
    public User updateUserStore(String email, Long storeId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        user.setStore(store);
        return userRepository.save(user);
    }

    @Override
    public java.util.List<UserResponse> getAllStaffUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        User user = new User();
        user.setFullname(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword("123456");
        user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        user.setIsActive(true);
        user.setPhone(""); // Provide default empty string as phone is not null
        
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFullname(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(user.getIsActive() == null || !user.getIsActive());
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse resetPassword(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword("123456");
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullname());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole() != null ? user.getRole().name() : null);
        response.setIsActive(user.getIsActive() != null ? user.getIsActive() : true);
        return response;
    }
}

