package com.smartserve.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private String complaintNumber;
    private Long relatedComplaintId;
    private LocalDateTime createdAt;
}
