package com.smartserve.service;

import com.smartserve.entity.Notification;
import com.smartserve.entity.User;
import com.smartserve.entity.Complaint;
import com.smartserve.dto.response.NotificationResponse;
import com.smartserve.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;

    public void createNotification(User user, String title, String message, String type, Complaint relatedComplaint) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setRelatedComplaint(relatedComplaint);
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public void markAllRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    public void markRead(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(user.getId())) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        });
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse response = new NotificationResponse();
        response.setId(n.getId());
        response.setTitle(n.getTitle());
        response.setMessage(n.getMessage());
        response.setType(n.getType());
        response.setIsRead(n.getIsRead());
        response.setCreatedAt(n.getCreatedAt());
        if (n.getRelatedComplaint() != null) {
            response.setRelatedComplaintId(n.getRelatedComplaint().getId());
            response.setComplaintNumber(n.getRelatedComplaint().getComplaintNumber());
        }
        return response;
    }
}
