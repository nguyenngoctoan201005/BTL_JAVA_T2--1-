package com.coffeeshop.backend.dto.auth;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String email;
    private List<String> roles;
    private Long storeId; // Thêm ID cửa hàng
    private String storeName; // Thêm tên cửa hàng để hiển thị
}