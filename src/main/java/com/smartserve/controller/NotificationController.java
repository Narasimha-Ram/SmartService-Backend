package com.smartserve.controller;

import com.smartserve.dto.response.ApiResponse;
import com.smartserve.dto.response.NotificationResponse;
import com.smartserve.entity.User;
import com.smartserve.service.NotificationService;
import com.smartserve.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationService notificationService;
    @Autowired private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotificationsForUser(user)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllRead(Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        notificationService.markAllRead(user);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read.", null));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id, Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        notificationService.markRead(id, user);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read.", null));
    }
}
