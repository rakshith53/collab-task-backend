package com.collabtask.authservice.service;

import com.collabtask.authservice.dto.SignupRequest;
import com.collabtask.authservice.entity.Role;
import com.collabtask.authservice.entity.RoleType;
import com.collabtask.authservice.entity.User;
import com.collabtask.authservice.repository.RoleRepository;
import com.collabtask.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(SignupRequest request){

        if(userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(Collections.singleton(userRole))
                .build();

        userRepository.save(user);
    }
}
