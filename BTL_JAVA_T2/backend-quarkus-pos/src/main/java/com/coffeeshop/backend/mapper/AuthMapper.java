package com.coffeeshop.backend.mapper;

import org.mapstruct.Mapper;

import com.coffeeshop.backend.dto.auth.RegisterRequest;
import com.coffeeshop.backend.dto.auth.RegisterResponse;
import com.coffeeshop.backend.entity.User;
import com.coffeeshop.backend.dto.auth.LoginResponse;

import com.coffeeshop.backend.dto.auth.UserProfileResponse;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    User toUser(RegisterRequest registerRequest);

    RegisterResponse toRegisterResponse(User user);

    LoginResponse toLoginResponse(User user, String token, String refreshToken);

    UserProfileResponse toUserProfileResponse(User user);
}