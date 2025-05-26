package com.collabtask.authservice.controller;

import com.collabtask.authservice.dto.JwtResponse;
import com.collabtask.authservice.dto.LoginRequest;
import com.collabtask.authservice.dto.RefreshRequest;
import com.collabtask.authservice.dto.SignupRequest;
import com.collabtask.authservice.security.UserPrincipal;
import com.collabtask.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request){
        authService.registerUser(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.login(request);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        JwtResponse jwtResponse = authService.refreshToken(request);
        return ResponseEntity.ok(jwtResponse);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        authService.logout(principal.getId());
        return ResponseEntity.ok("Logged out successfully");
    }



}
