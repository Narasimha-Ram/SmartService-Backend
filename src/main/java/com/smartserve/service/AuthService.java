package com.smartserve.service;

import com.smartserve.dto.request.LoginRequest;
import com.smartserve.dto.request.RegisterRequest;
import com.smartserve.dto.response.AuthResponse;
import com.smartserve.entity.Role;
import com.smartserve.entity.User;
import com.smartserve.exception.BadRequestException;
import com.smartserve.repository.RoleRepository;
import com.smartserve.repository.UserRepository;
import com.smartserve.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered.");
        }
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BadRequestException("Default role not found. Please run seed data."));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(userRole);
        user.setIsActive(true);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().getName());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found."));
        if (!user.getIsActive()) {
            throw new BadRequestException("Your account has been deactivated. Please contact the administrator.");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().getName());
    }
}
