package com.coffeeshop.backend.service;

import org.springframework.stereotype.Service;

import com.coffeeshop.backend.dto.auth.LoginRequest;
import com.coffeeshop.backend.dto.auth.LoginResponse;
import com.coffeeshop.backend.dto.auth.RegisterRequest;
import com.coffeeshop.backend.dto.auth.RegisterResponse;

import com.coffeeshop.backend.dto.auth.UserProfileResponse;

@Service
public interface AuthService {
    RegisterResponse registerNewUser(RegisterRequest registerRequest);

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refreshToken(String refreshToken);

    UserProfileResponse getProfile(String username);

}
