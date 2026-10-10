package com.smartserve.controller;

import com.smartserve.dto.request.ProfileUpdateRequest;
import com.smartserve.dto.response.ApiResponse;
import com.smartserve.dto.response.UserResponse;
import com.smartserve.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(principal.getName())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(Principal principal,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated.", userService.updateProfile(principal.getName(), request)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long id) {
        userService.toggleUserActive(id);
        return ResponseEntity.ok(ApiResponse.success("User status updated.", null));
    }
}
