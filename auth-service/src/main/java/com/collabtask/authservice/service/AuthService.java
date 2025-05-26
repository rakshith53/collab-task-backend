package com.collabtask.authservice.service;

import com.collabtask.authservice.dto.JwtResponse;
import com.collabtask.authservice.dto.LoginRequest;
import com.collabtask.authservice.dto.RefreshRequest;
import com.collabtask.authservice.dto.SignupRequest;
import com.collabtask.authservice.entity.RefreshToken;
import com.collabtask.authservice.entity.Role;
import com.collabtask.authservice.entity.RoleType;
import com.collabtask.authservice.entity.User;
import com.collabtask.authservice.exception.*;
import com.collabtask.authservice.repository.RoleRepository;
import com.collabtask.authservice.repository.UserRepository;
import com.collabtask.authservice.security.JwtProvider;
import com.collabtask.authservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;


    public void registerUser(SignupRequest request){

        if(userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("username");
        }

        if(userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists");
        }

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new RoleNotFoundException());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(Collections.singleton(userRole))
                .build();

        userRepository.save(user);
    }

    public JwtResponse login(LoginRequest loginRequest) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword())
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String token = jwtProvider.generateToken(authentication);

            return JwtResponse.builder()
                    .token(token)
                    .id(userPrincipal.getId())
                    .username(userPrincipal.getUsername())
                    .email(userPrincipal.getEmail())
                    .roles(userPrincipal.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList())
                    .build();
        } catch (Exception ex){
            throw new InvalidCredentialsException();
        }

    }

    public JwtResponse refreshToken(RefreshRequest refreshRequest){
        RefreshToken token = refreshTokenService.findByToken(refreshRequest.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if(refreshTokenService.isExpired(token)) {
            refreshTokenService.deleteByUserId(token.getUser().getId());
            throw new RefreshTokenExpiredException();
        }

        refreshTokenService.deleteByUserId(token.getUser().getId());

        User user = token.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        String newAccessToken = jwtProvider.generateToken(
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities())
        );

        return JwtResponse.builder()
                .token(newAccessToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(userPrincipal.getAuthorities().stream().map(r -> r.getAuthority()).toList())
                .build();
    }

    public void logout(UUID userId) {
        refreshTokenService.deleteByUserId(userId);
    }

}
