package com.coffeeshop.backend.dto.auth;

import com.coffeeshop.backend.enums.UserRole;
import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String fullname;
    private UserRole role;
    private String phone;
}
