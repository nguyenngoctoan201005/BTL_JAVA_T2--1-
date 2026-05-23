package com.coffeeshop.backend.service.implement;

import java.util.List;
import java.util.Optional;

// import org.springframework.security.core.Authentication;
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.coffeeshop.backend.dto.auth.LoginRequest;
import com.coffeeshop.backend.dto.auth.LoginResponse;
import com.coffeeshop.backend.dto.auth.RegisterRequest;
import com.coffeeshop.backend.dto.auth.RegisterResponse;
import com.coffeeshop.backend.entity.User;
import com.coffeeshop.backend.enums.UserRole;
import com.coffeeshop.backend.exception.AuthenticationException;
import com.coffeeshop.backend.mapper.AuthMapper;
import com.coffeeshop.backend.repository.UserRepository;
// import com.coffeeshop.backend.security.JwtTokenProvider;
import com.coffeeshop.backend.service.AuthService;
import com.coffeeshop.backend.exception.EmailAlreadyExistsException;

import com.coffeeshop.backend.dto.auth.UserProfileResponse;
import com.coffeeshop.backend.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    // private final JwtTokenProvider jwtTokenProvider;
    // private final AuthenticationManager authenticationManager;

    @Override
    public RegisterResponse registerNewUser(RegisterRequest registerRequest) {
        log.info("Registering new user with email: {}", registerRequest.getEmail());
        log.info("Password from request: {}", registerRequest.getPassword());
        Optional<User> userOptional = userRepository.findByEmail(registerRequest.getEmail());
        if (userOptional.isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = authMapper.toUser(registerRequest);
        // String encodedPassword =
        // passwordEncoder.encode(registerRequest.getPassword());
        // log.info("Encoded password: {}", encodedPassword);
        user.setPassword(registerRequest.getPassword()); // Temporarily plain text or mocked
        user.setRole(UserRole.CUSTOMER);
        userRepository.save(user);
        return authMapper.toRegisterResponse(user);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for email: {}", loginRequest.getEmail());

        // 1. Tìm user trong database
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AuthenticationException("User not found: " + loginRequest.getEmail()));

        // 2. Kiểm tra mật khẩu (Bỏ qua nếu là tài khoản staff1 để demo)
        if (loginRequest.getEmail().equals("staff1@gmail.com")) {
            log.info("Bypass password check for demo account: staff1@gmail.com");
        } else {
            // So sánh chuỗi trực tiếp vì mật khẩu đang được lưu dạng plain text
            if (!loginRequest.getPassword().equals(user.getPassword())) {
                throw new AuthenticationException("Invalid password");
            }
        }

        // 3. Tạo Token (Hiện tại đang dùng mock-token cho đơn giản)
        String token = "mock-token-" + System.currentTimeMillis();
        String refreshToken = "mock-refresh-token";

        // 4. Ánh xạ dữ liệu và trả về LoginResponse đầy đủ thông tin chuỗi quán
        return LoginResponse.builder()
                .token(token)
                .username(user.getFullname())
                .email(user.getEmail())
                .roles(java.util.List.of(user.getRole().name()))
                .storeId(user.getStore() != null ? user.getStore().getId() : null)
                .storeName(user.getStore() != null ? user.getStore().getName() : "Hệ thống tổng")
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // if (jwtTokenProvider.validateToken(refreshToken)) {
        // String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        // User user = userRepository.findByEmail(email).orElseThrow(() -> new
        // ResourceNotFoundException("User not found"));
        // Authentication authentication = new
        // UsernamePasswordAuthenticationToken(user.getEmail(), null,
        // java.util.Collections.singletonList(new
        // SimpleGrantedAuthority(user.getRole().name())));
        // String newAccessToken = jwtTokenProvider.generateToken(authentication);
        // String newRefreshToken =
        // jwtTokenProvider.generateRefreshToken(authentication);
        // return authMapper.toLoginResponse(user, newAccessToken, newRefreshToken);
        // } else {
        // throw new AuthenticationException("Invalid refresh token");
        // }
        return null; // Temporarily commented out
    }

    @Override
    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
        return authMapper.toUserProfileResponse(user);
    }

}
