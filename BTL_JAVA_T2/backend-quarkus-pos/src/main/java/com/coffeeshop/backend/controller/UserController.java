package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.StoreDTO;
import com.coffeeshop.backend.dto.user.UserDTO;
import com.coffeeshop.backend.entity.Store;
import com.coffeeshop.backend.entity.User;
import com.coffeeshop.backend.enums.UserRole;
import com.coffeeshop.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.List;
import com.coffeeshop.backend.dto.user.UserResponse;
import com.coffeeshop.backend.dto.user.UserRequest;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getProfile() {
        // TODO: replace with real authenticated user from Quarkus SecurityIdentity
        String email = "admin@gmail.com";
        User user = userService.getProfile(email);
        return ResponseEntity.ok(convertToDto(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(@RequestBody UserDTO userDTO) {
        // TODO: replace with real authenticated user from Quarkus SecurityIdentity
        String email = "admin@gmail.com";
        User updatedUser = userService.updateProfile(email, userDTO);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    @PutMapping("/me/store")
    public ResponseEntity<UserDTO> updateUserStore(@RequestBody Map<String, Long> body) {
        // TODO: replace with real authenticated user from Quarkus SecurityIdentity
        String email = "admin@gmail.com";
        Long storeId = body.get("storeId");
        User updatedUser = userService.updateUserStore(email, storeId);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        UserRole role = UserRole.valueOf(body.get("role"));
        User updatedUser = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    private UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setFullname(user.getFullname());
        userDTO.setPhone(user.getPhone());
        userDTO.setRole(user.getRole());
        if (user.getStore() != null) {
            StoreDTO storeDTO = new StoreDTO();
            storeDTO.setId(user.getStore().getId());
            storeDTO.setName(user.getStore().getName());
            storeDTO.setAddress(user.getStore().getAddress());
            userDTO.setStore(storeDTO);
        }
        return userDTO;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllStaffUsers() {
        return ResponseEntity.ok(userService.getAllStaffUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/lock")
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<UserResponse> resetPassword(@PathVariable Long id) {
        return ResponseEntity.ok(userService.resetPassword(id));
    }
}
