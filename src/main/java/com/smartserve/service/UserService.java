package com.smartserve.service;

import com.smartserve.dto.request.ProfileUpdateRequest;
import com.smartserve.dto.response.UserResponse;
import com.smartserve.entity.User;
import com.smartserve.exception.ResourceNotFoundException;
import com.smartserve.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    public UserResponse getProfile(String email) {
        User user = getCurrentUser(email);
        return toResponse(user);
    }

    public UserResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = getCurrentUser(email);
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        return toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().getName());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
