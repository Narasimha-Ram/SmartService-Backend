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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role(1L, "USER");
    }

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("test@example.com");
        req.setPassword("Password@123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(jwtUtil.generateToken(any(), any())).thenReturn("mockJwtToken");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("test@example.com", resp.getEmail());
        assertEquals("USER", resp.getRole());
        assertEquals("mockJwtToken", resp.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("Password@123");

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setRole(userRole);
        user.setIsActive(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("test@example.com", "USER")).thenReturn("mockJwtToken");

        AuthResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("test@example.com", resp.getEmail());
        assertEquals("mockJwtToken", resp.getToken());
    }
}

