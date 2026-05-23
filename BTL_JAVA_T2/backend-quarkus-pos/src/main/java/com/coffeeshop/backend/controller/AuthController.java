package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.auth.*;
import com.coffeeshop.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${spring.security.jwt.expiration:3600000}")
    private int jwtExpiration;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = authService.registerNewUser(registerRequest);
        return new ResponseEntity<>(registerResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);

        // Set cookies via response headers (Quarkus-compatible approach)
        HttpHeaders headers = new HttpHeaders();
        String jwtCookie = String.format("jwt=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=Lax",
                loginResponse.getToken(), jwtExpiration / 1000);
        String refreshCookie = String.format("refreshToken=%s; HttpOnly; Path=/; Max-Age=604800; SameSite=Lax",
                loginResponse.getRefreshToken());
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie);
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie);
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Frame-Options", "DENY");
        headers.add("X-XSS-Protection", "1; mode=block");

        loginResponse.setToken(null);
        loginResponse.setRefreshToken(null);

        return new ResponseEntity<>(loginResponse, headers, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @CookieValue(name = "refreshToken", defaultValue = "") String refreshToken) {
        LoginResponse loginResponse = authService.refreshToken(refreshToken);
        if (loginResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        HttpHeaders headers = new HttpHeaders();
        String jwtCookie = String.format("jwt=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=Lax",
                loginResponse.getToken(), jwtExpiration / 1000);
        String refreshCookie = String.format("refreshToken=%s; HttpOnly; Path=/; Max-Age=604800; SameSite=Lax",
                loginResponse.getRefreshToken());
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie);
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie);

        loginResponse.setToken(null);
        loginResponse.setRefreshToken(null);

        return new ResponseEntity<>(loginResponse, headers, HttpStatus.OK);
    }

    // @GetMapping("/profile")
    // public ResponseEntity<UserProfileResponse> getProfile(Principal principal) {
    // UserProfileResponse userProfile =
    // authService.getProfile(principal.getName());
    // return ResponseEntity.ok(userProfile);
    // }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "jwt=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax");
        headers.add(HttpHeaders.SET_COOKIE, "refreshToken=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax");
        return ResponseEntity.ok().headers(headers).build();
    }
}
