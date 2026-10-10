package com.smartserve.dto.response;

import com.smartserve.entity.ComplaintStatus;
import com.smartserve.entity.Priority;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ComplaintDetailResponse {
    private Long id;
    private String complaintNumber;
    private String title;
    private String description;
    private String categoryName;
    private Long categoryId;
    private String location;
    private Priority priority;
    private ComplaintStatus status;

    // User info
    private Long userId;
    private String userName;
    private String userEmail;

    // Assignment info
    private String departmentName;
    private String staffName;
    private String assignmentNotes;
    private LocalDateTime assignedAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    // Related data
    private List<StatusHistoryResponse> statusHistory;
    private List<CommentResponse> comments;
    private List<AttachmentResponse> attachments;
    private FeedbackResponse feedback;

    // Intelligent features
    private String recommendedPriority;
    private String estimatedResolutionTime;
}
